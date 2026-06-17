package com.example.final_project.Scheduler;

import com.example.final_project.Entity.ItemEntity;
import com.example.final_project.Service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionScheduler {

    private final ItemService itemService;

    @Scheduled(fixedRate = 60000) // 1분마다 실행
    @Transactional
    public void checkEndedAuctions() {
        try {
            List<ItemEntity> allItems = itemService.getAllItems();
            
            for (ItemEntity item : allItems) {
                // bidding 상태인데 endTime이 지난 경우
                if ("bidding".equals(item.getStatus())) {
                    item.updateTimeBasedStatus();
                    if ("ended".equals(item.getStatus())) {
                        log.info("경매 종료 감지: {} (ID: {})", item.getTitle(), item.getId());
                        itemService.finalizeAuction(item);
                    }
                }
            }
        } catch (Exception e) {
            log.error("경매 종료 체크 중 오류 발생", e);
        }
    }
}
