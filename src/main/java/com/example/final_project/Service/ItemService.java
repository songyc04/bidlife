package com.example.final_project.Service;

import com.example.final_project.Entity.BidEntity;
import com.example.final_project.Entity.ItemEntity;
import com.example.final_project.Entity.NotificationEntity;
import com.example.final_project.Entity.SignupEntity;
import com.example.final_project.Repository.BidRepository;
import com.example.final_project.Repository.FavoriteRepository;
import com.example.final_project.Repository.ItemRepository;
import com.example.final_project.Repository.NotificationRepository;
import com.example.final_project.Repository.SignupRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final BidRepository bidRepository;
    private final FavoriteRepository favoriteRepository;
    private final NotificationRepository notificationRepository;
    private final SignupRepository signupRepository;

    @Value("${file.upload-dir:uploads/items}")
    private String uploadDir;

    private String absoluteUploadDir;

    @PostConstruct
    public void init() {
        Path path = Paths.get(uploadDir).toAbsolutePath();
        absoluteUploadDir = path.toString();
        File dir = new File(absoluteUploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    @Transactional
    public ItemEntity saveItem(String title, String category, String description,
                               Integer startPrice, Integer buyNowPrice, Integer bidUnit,
                               LocalDateTime startTime, LocalDateTime endTime,
                               Long sellerId, MultipartFile[] images) throws IOException {

        ItemEntity item = new ItemEntity();
        item.setTitle(title);
        item.setCategory(category);
        item.setDescription(description);
        item.setStartPrice(startPrice);
        item.setBuyNowPrice(buyNowPrice);
        item.setBidUnit(bidUnit);
        item.setStartTime(startTime);
        item.setEndTime(endTime);
        item.setSellerId(sellerId);

        if (images != null && images.length > 0) {
            List<String> imagePaths = saveImages(images);
            item.setImagePaths(String.join(",", imagePaths));
        }

        item.updateTimeBasedStatus();

        return itemRepository.save(item);
    }

    private List<String> saveImages(MultipartFile[] images) throws IOException {
        List<String> paths = new ArrayList<>();

        for (MultipartFile image : images) {
            if (image.isEmpty()) continue;

            String originalName = image.getOriginalFilename();
            String extension = "";
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }

            String fileName = UUID.randomUUID().toString() + extension;
            File dest = new File(absoluteUploadDir, fileName);
            image.transferTo(dest);

            paths.add("/uploads/items/" + fileName);
        }

        return paths;
    }

    public List<ItemEntity> getAllItems() {
        try {
            List<ItemEntity> items = itemRepository.findAllByOrderByCreatedAtDesc();
            if (items == null) {
                return new ArrayList<>();
            }
            items.forEach(item -> {
                try {
                    item.updateTimeBasedStatus();
                } catch (Exception e) {
                    // Skip status update for problematic items
                }
            });
            return items;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<ItemEntity> getAllItemsExcludingSeller(Long sellerId) {
        try {
            List<ItemEntity> items = itemRepository.findAllBySellerIdNotOrderByCreatedAtDesc(sellerId);
            if (items == null) {
                return new ArrayList<>();
            }
            items.forEach(item -> {
                try {
                    item.updateTimeBasedStatus();
                } catch (Exception e) {
                }
            });
            return items;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<ItemEntity> getItemsBySeller(Long sellerId) {
        try {
            List<ItemEntity> items = itemRepository.findBySellerIdOrderByCreatedAtDesc(sellerId);
            if (items == null) {
                return new ArrayList<>();
            }
            items.forEach(item -> {
                try {
                    item.updateTimeBasedStatus();
                } catch (Exception e) {
                    // Skip status update for problematic items
                }
            });
            return items;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public ItemEntity getItemById(Long id) {
        ItemEntity item = itemRepository.findById(id).orElse(null);
        if (item != null) {
            item.updateTimeBasedStatus();
        }
        return item;
    }

    @Transactional
    public void deleteItem(Long itemId, Long userId) {
        ItemEntity item = itemRepository.findById(itemId).orElse(null);
        if (item == null) {
            throw new IllegalArgumentException("존재하지 않는 경매입니다.");
        }
        if (!item.getSellerId().equals(userId)) {
            throw new SecurityException("삭제 권한이 없습니다.");
        }

        notificationRepository.deleteByItemId(itemId);
        favoriteRepository.deleteByItemId(itemId);
        bidRepository.deleteByItemId(itemId);
        itemRepository.delete(item);
    }

    @Transactional
    public void endItemEarly(Long itemId, Long userId) {
        ItemEntity item = itemRepository.findById(itemId).orElse(null);
        if (item == null) {
            throw new IllegalArgumentException("존재하지 않는 경매입니다.");
        }
        if (!item.getSellerId().equals(userId)) {
            throw new SecurityException("종료 권한이 없습니다.");
        }
        if (item.getStatus().equals("ended")) {
            throw new IllegalStateException("이미 종료된 경매입니다.");
        }

        item.setEndTime(LocalDateTime.now());
        item.setStatus("ended");
        itemRepository.save(item);
        
        // 경매 종료 처리 (낙찰자 결정 및 알림)
        finalizeAuction(item);
    }

    @Transactional
    public void finalizeAuction(ItemEntity item) {
        // 최고가 입찰 찾기
        List<BidEntity> allBids = bidRepository.findByItemIdAndStatusOrderByBidAmountDesc(item.getId(), "active");
        
        if (allBids.isEmpty()) {
            // 유찰 처리 - 판매자에게만 알림
            createNotification(
                item.getSellerId(),
                "경매 \"" + item.getTitle() + "\"이(가) 유찰되었습니다. 입찰자가 없었습니다.",
                item.getId()
            );
        } else {
            // 낙찰자 결정
            BidEntity winningBid = allBids.get(0);
            Long winnerId = winningBid.getBidderId();
            Integer winningAmount = winningBid.getBidAmount();
            
            // 낙찰자에게 알림
            createNotification(
                winnerId,
                "축하합니다! 경매 \"" + item.getTitle() + "\"에서 " + winningAmount + "원으로 낙찰되셨습니다.",
                item.getId()
            );
            
            // 판매자에게 알림
            SignupEntity winner = signupRepository.findById(winnerId).orElse(null);
            String winnerNickname = winner != null ? winner.getNickname() : "알 수 없음";
            
            createNotification(
                item.getSellerId(),
                "경매 \"" + item.getTitle() + "\"이(가) " + winnerNickname + "님에게 " + winningAmount + "원에 낙찰되었습니다.",
                item.getId()
            );
            
            // 다른 입찰자들에게 낙찰 실패 알림
            List<BidEntity> allItemBids = bidRepository.findByItemId(item.getId());
            for (BidEntity bid : allItemBids) {
                if (!bid.getBidderId().equals(winnerId)) {
                    createNotification(
                        bid.getBidderId(),
                        "경매 \"" + item.getTitle() + "\"이(가) 종료되었습니다. 낙찰되지 않았습니다.",
                        item.getId()
                    );
                }
            }
        }
    }

    @Transactional
    public void buyNow(Long itemId, Long buyerId) {
        ItemEntity item = itemRepository.findById(itemId).orElse(null);
        if (item == null) {
            throw new IllegalArgumentException("존재하지 않는 경매입니다.");
        }
        if (item.getBuyNowPrice() == null || item.getBuyNowPrice() <= 0) {
            throw new IllegalStateException("즉시 구매가 설정되지 않은 경매입니다.");
        }
        if (!item.getStatus().equals("bidding")) {
            throw new IllegalStateException("현재 입찰이 가능한 경매가 아닙니다.");
        }
        if (item.getSellerId().equals(buyerId)) {
            throw new IllegalStateException("자신의 경매는 구매할 수 없습니다.");
        }

        // 기존 입찰자 목록 가져오기 (알림 발송용)
        List<BidEntity> existingBids = bidRepository.findByItemId(itemId);

        // 즉시 구매 처리
        item.setStatus("ended");
        item.setEndTime(LocalDateTime.now());
        item.setCurrentPrice(item.getBuyNowPrice());
        itemRepository.save(item);

        // 즉시 구매 입찰 생성
        BidEntity bid = new BidEntity();
        bid.setItemId(itemId);
        bid.setBidderId(buyerId);
        bid.setBidAmount(item.getBuyNowPrice());
        bid.setStatus("active");
        bidRepository.save(bid);

        // 구매자에게 알림
        createNotification(
            buyerId,
            "즉시 구매 완료! 경매 \"" + item.getTitle() + "\"을(를) " + item.getBuyNowPrice() + "원에 구매했습니다.",
            itemId
        );

        // 판매자에게 알림
        SignupEntity buyer = signupRepository.findById(buyerId).orElse(null);
        String buyerNickname = buyer != null ? buyer.getNickname() : "알 수 없음";
        
        createNotification(
            item.getSellerId(),
            "경매 \"" + item.getTitle() + "\"이(가) " + buyerNickname + "님에게 " + item.getBuyNowPrice() + "원에 즉시 구매되었습니다.",
            itemId
        );

        // 기존 입찰자들에게 낙찰 실패 알림
        for (BidEntity existingBid : existingBids) {
            if (!existingBid.getBidderId().equals(buyerId)) {
                createNotification(
                    existingBid.getBidderId(),
                    "경매 \"" + item.getTitle() + "\"이(가) 즉시 구매되어 종료되었습니다.",
                    itemId
                );
            }
        }
    }

    private void createNotification(Long userId, String message, Long itemId) {
        NotificationEntity notification = new NotificationEntity();
        notification.setUserId(userId);
        notification.setMessage(message);
        notification.setItemId(itemId);
        notification.setType("auction");
        notification.setIsRead(false);
        notificationRepository.save(notification);
    }
}
