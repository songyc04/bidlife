package com.example.final_project.Service;

import com.example.final_project.Entity.BidEntity;
import com.example.final_project.Entity.ItemEntity;
import com.example.final_project.Entity.SignupEntity;
import com.example.final_project.Repository.BidRepository;
import com.example.final_project.Repository.FavoriteRepository;
import com.example.final_project.Repository.ItemRepository;
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
    private final SignupRepository signupRepository;
    private final NotificationService notificationService;

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

    public List<ItemEntity> getPurchasesByUser(Long userId) {
        try {
            List<ItemEntity> items = itemRepository.findByWinnerIdOrderByTransactionDateDesc(userId);
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

    public List<ItemEntity> getSalesByUser(Long userId) {
        try {
            List<ItemEntity> items = itemRepository.findBySellerIdAndStatusOrderByTransactionDateDesc(userId, "ended");
            if (items == null) {
                return new ArrayList<>();
            }
            return items;
        } catch (Exception e) {
            return new ArrayList<>();
        }
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

        notificationService.deleteByItemId(itemId);
        favoriteRepository.deleteByItemId(itemId);
        bidRepository.deleteByItemId(itemId);
        itemRepository.delete(item);
    }

    @Transactional
    public void updateImages(Long itemId, Long userId, MultipartFile[] newImages, String[] removeImages) throws IOException {
        ItemEntity item = itemRepository.findById(itemId).orElse(null);
        if (item == null) {
            throw new IllegalArgumentException("존재하지 않는 경매입니다.");
        }
        if (!item.getSellerId().equals(userId)) {
            throw new SecurityException("수정 권한이 없습니다.");
        }

        List<String> currentPaths = new ArrayList<>();
        if (item.getImagePaths() != null && !item.getImagePaths().isEmpty()) {
            for (String path : item.getImagePaths().split(",")) {
                currentPaths.add(path.trim());
            }
        }

        // Remove selected images
        if (removeImages != null && removeImages.length > 0) {
            List<String> removeList = new ArrayList<>();
            for (String r : removeImages) {
                removeList.add(r.trim());
            }
            for (String removePath : removeList) {
                // Delete file from disk
                String fileName = removePath.substring(removePath.lastIndexOf("/") + 1);
                File file = new File(absoluteUploadDir, fileName);
                if (file.exists()) {
                    file.delete();
                }
                currentPaths.remove(removePath);
            }
        }

        // Add new images
        if (newImages != null && newImages.length > 0) {
            List<String> newPaths = saveImages(newImages);
            currentPaths.addAll(newPaths);
        }

        item.setImagePaths(String.join(",", currentPaths));
        itemRepository.save(item);
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
        List<BidEntity> activeBids = bidRepository.findByItemIdAndStatusOrderByBidAmountDesc(item.getId(), "active");

        if (activeBids.isEmpty()) {
            // 유찰 처리
            completeTransaction(item, null, null, true);

            notificationService.createNotification(
                item.getSellerId(),
                "경매 \"" + item.getTitle() + "\"이(가) 유찰되었습니다. 입찰자가 없었습니다.",
                item.getId(),
                "auction_failed"
            );
        } else {
            // 낙찰자 결정
            BidEntity winningBid = activeBids.get(0);
            Long winnerId = winningBid.getBidderId();
            Integer winningAmount = winningBid.getBidAmount();

            completeTransaction(item, winnerId, winningAmount, false);
            markWinningBid(item.getId(), winnerId, winningAmount);
            markBidsOutbid(item.getId(), winnerId);

            // 낙찰자 닉네임 조회
            SignupEntity winner = signupRepository.findById(winnerId).orElse(null);
            String winnerNickname = winner != null ? winner.getNickname() : "알 수 없음";

            // 낙찰자에게 알림
            notificationService.createNotification(
                winnerId,
                "🎉 축하합니다! 경매 \"" + item.getTitle() + "\"에서 " + winningAmount + "원에 낙찰되셨습니다. 결제를 진행해주세요.",
                item.getId(),
                "auction_won"
            );

            // 판매자에게 알림
            notificationService.createNotification(
                item.getSellerId(),
                "경매 \"" + item.getTitle() + "\"이(가) " + winnerNickname + "님에게 " + winningAmount + "원에 낙찰되었습니다. 구매자의 결제를 기다립니다.",
                item.getId(),
                "auction_seller_won"
            );

            // 다른 입찰자들에게 낙찰 실패 알림
            List<BidEntity> allItemBids = bidRepository.findByItemId(item.getId());
            for (BidEntity bid : allItemBids) {
                if (!bid.getBidderId().equals(winnerId)) {
                    notificationService.createNotification(
                        bid.getBidderId(),
                        "경매 \"" + item.getTitle() + "\"이(가) 종료되었습니다. 낙찰되지 않았습니다.",
                        item.getId(),
                        "auction_lost"
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

        Integer buyNowPrice = item.getBuyNowPrice();

        // 거래 확정 처리
        completeTransaction(item, buyerId, buyNowPrice, false);

        // 기존 입찰자 모두 outbid 처리 (구매자 제외)
        markBidsOutbid(item.getId(), buyerId);

        // 즉시 구매 입찰 생성 (status = "won")
        BidEntity bid = new BidEntity();
        bid.setItemId(itemId);
        bid.setBidderId(buyerId);
        bid.setBidAmount(buyNowPrice);
        bid.setStatus("won");
        bidRepository.save(bid);

        // 구매자 닉네임 조회
        SignupEntity buyer = signupRepository.findById(buyerId).orElse(null);
        String buyerNickname = buyer != null ? buyer.getNickname() : "알 수 없음";

        // 구매자에게 알림
        notificationService.createNotification(
            buyerId,
            "💳 즉시 구매 완료! 경매 \"" + item.getTitle() + "\"을(를) " + buyNowPrice + "원에 구매했습니다. 결제를 진행해주세요.",
            itemId,
            "buy_now_complete"
        );

        // 판매자에게 알림
        notificationService.createNotification(
            item.getSellerId(),
            "경매 \"" + item.getTitle() + "\"이(가) " + buyerNickname + "님에게 " + buyNowPrice + "원에 즉시 구매되었습니다. 구매자의 결제를 기다립니다.",
            itemId,
            "buy_now_ended"
        );

        // 기존 입찰자들에게 종료 알림
        List<BidEntity> existingBids = bidRepository.findByItemId(itemId);
        for (BidEntity existingBid : existingBids) {
            if (!existingBid.getBidderId().equals(buyerId)) {
                notificationService.createNotification(
                    existingBid.getBidderId(),
                    "⚡ 경매 \"" + item.getTitle() + "\"이(가) 즉시 구매로 종료되었습니다.",
                    itemId,
                    "buy_now_ended"
                );
            }
        }
    }

    @Transactional
    public void completeTransaction(ItemEntity item, Long winnerId, Integer finalPrice, boolean isNoBid) {
        LocalDateTime now = LocalDateTime.now();
        item.setStatus("ended");
        item.setEndTime(now);
        item.setTransactionDate(now);

        if (isNoBid) {
            item.setWinnerId(null);
            item.setFinalPrice(null);
            item.setPaymentStatus("cancelled");
            item.setShippingStatus("cancelled");
        } else {
            item.setWinnerId(winnerId);
            item.setFinalPrice(finalPrice);
            item.setCurrentPrice(finalPrice);
            item.setPaymentStatus("pending");
            item.setShippingStatus("pending");
        }

        itemRepository.save(item);
    }

    @Transactional
    public void markBidsOutbid(Long itemId, Long excludeBidderId) {
        bidRepository.markAllAsOutbidExcluding(itemId, excludeBidderId);
    }

    @Transactional
    public boolean confirmPaymentBySeller(Long itemId, Long sellerId) {
        ItemEntity item = itemRepository.findById(itemId).orElse(null);
        if (item == null) {
            throw new IllegalArgumentException("존재하지 않는 경매입니다.");
        }
        if (!item.getSellerId().equals(sellerId)) {
            throw new SecurityException("본인 판매 물품만 확인할 수 있습니다.");
        }
        if (!"buyer_paid".equals(item.getPaymentStatus())) {
            throw new IllegalStateException("구매자 결제가 완료되지 않았습니다. (현재 상태: " + item.getPaymentStatus() + ")");
        }

        item.setPaymentStatus("completed");
        item.setSellerConfirmedAt(LocalDateTime.now());
        itemRepository.save(item);

        notificationService.createNotification(
                item.getWinnerId(),
                "🎉 \"" + item.getTitle() + "\" 거래가 최종 완료되었습니다. 판매자가 입금을 확인했습니다.",
                item.getId(),
                "transaction_completed"
        );

        notificationService.createNotification(
                sellerId,
                "✅ \"" + item.getTitle() + "\" 거래 확정이 완료되었습니다.",
                item.getId(),
                "transaction_completed"
        );

        return true;
    }

    @Transactional
    public void markWinningBid(Long itemId, Long winnerId, Integer finalPrice) {
        // 가장 높은 금액의 활성 입찰을 찾아 won으로 변경
        List<BidEntity> activeBids = bidRepository.findByItemIdAndStatusOrderByBidAmountDesc(itemId, "active");
        for (BidEntity bid : activeBids) {
            if (bid.getBidderId().equals(winnerId) && bid.getBidAmount().equals(finalPrice)) {
                bid.setStatus("won");
                bidRepository.save(bid);
                return;
            }
        }
    }
}
