package com.example.final_project.Service;

import com.example.final_project.Entity.BidEntity;
import com.example.final_project.Entity.ItemEntity;
import com.example.final_project.Entity.SignupEntity;
import com.example.final_project.Repository.BidRepository;
import com.example.final_project.Repository.ItemRepository;
import com.example.final_project.Repository.SignupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BidService {

    private final BidRepository bidRepository;
    private final ItemRepository itemRepository;
    private final SignupRepository signupRepository;
    private final NotificationService notificationService;

    @Transactional
    public BidEntity placeBid(Long itemId, Long bidderId, Integer bidAmount) {
        ItemEntity item = itemRepository.findById(itemId).orElse(null);
        if (item == null) {
            throw new IllegalArgumentException("존재하지 않는 경매입니다.");
        }

        item.updateTimeBasedStatus();
        if (!"bidding".equals(item.getStatus())) {
            throw new IllegalStateException("현재 입찰이 가능한 경매가 아닙니다.");
        }

        if (item.getSellerId().equals(bidderId)) {
            throw new IllegalStateException("자신의 경매에는 입찰할 수 없습니다.");
        }

        Integer currentPrice = item.getCurrentPrice() != null ? item.getCurrentPrice() : item.getStartPrice();
        Integer minBid = currentPrice + item.getBidUnit();
        if (bidAmount < minBid) {
            throw new IllegalArgumentException("최소 입찰 금액은 " + String.format("%,d", minBid) + "원입니다.");
        }

        List<BidEntity> existingBids = bidRepository.findByItemIdAndStatusOrderByBidAmountDesc(itemId, "active");
        for (BidEntity existingBid : existingBids) {
            existingBid.setStatus("outbid");
            bidRepository.save(existingBid);
        }

        BidEntity bid = new BidEntity();
        bid.setItemId(itemId);
        bid.setBidderId(bidderId);
        bid.setBidAmount(bidAmount);
        bid.setStatus("active");
        bidRepository.save(bid);

        item.setCurrentPrice(bidAmount);
        itemRepository.save(item);

        SignupEntity bidder = signupRepository.findById(bidderId).orElse(null);
        String bidderName = bidder != null ? bidder.getNickname() : "입찰자";

        notificationService.createNotification(
                item.getSellerId(),
                bidderName + "님이 [" + item.getTitle() + "]에 " + String.format("%,d", bidAmount) + "원으로 입찰했습니다.",
                itemId,
                "new_bid"
        );

        return bid;
    }

    public List<BidEntity> getBidsByItemId(Long itemId) {
        return bidRepository.findByItemIdOrderByCreatedAtDesc(itemId);
    }

    public List<BidEntity> getBidsByBidderId(Long bidderId) {
        return bidRepository.findByBidderIdOrderByCreatedAtDesc(bidderId);
    }

    public Integer getHighestBidAmount(Long itemId) {
        List<BidEntity> activeBids = bidRepository.findByItemIdAndStatusOrderByBidAmountDesc(itemId, "active");
        if (activeBids.isEmpty()) {
            return null;
        }
        return activeBids.get(0).getBidAmount();
    }

    public long getBidderCount(Long itemId) {
        return bidRepository.countDistinctBidderByItemId(itemId);
    }
}
