package com.example.final_project.Controller;

import com.example.final_project.Entity.ItemEntity;
import com.example.final_project.Entity.SignupEntity;
import com.example.final_project.Repository.ItemRepository;
import com.example.final_project.Repository.SignupRepository;
import com.example.final_project.Service.BidService;
import com.example.final_project.Service.FavoriteService;
import com.example.final_project.Service.ItemService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class IndexController {

    private final ItemService itemService;
    private final BidService bidService;
    private final FavoriteService favoriteService;
    private final SignupRepository signupRepository;
    private final ItemRepository itemRepository;

    @GetMapping("/")
    public String index(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        String nickname = (String) session.getAttribute("nickname");

        if (userId != null) {
            model.addAttribute("isLoggedIn", true);
            model.addAttribute("nickname", nickname);
        } else {
            model.addAttribute("isLoggedIn", false);
        }

        List<ItemEntity> hotItems = itemService.getAllItems().stream()
                .filter(item -> "bidding".equals(item.getStatus()))
                .sorted(Comparator
                        .comparingLong((ItemEntity item) -> bidService.getBidderCount(item.getId())).reversed()
                        .thenComparing(Comparator.comparingInt((ItemEntity item) -> 
                                item.getCurrentPrice() != null ? item.getCurrentPrice() : item.getStartPrice()).reversed())
                        .thenComparing(ItemEntity::getEndTime))
                .limit(6)
                .collect(Collectors.toList());
        model.addAttribute("hotItems", hotItems);

        Map<Long, String> sellerNicknames = new HashMap<>();
        Map<Long, Long> bidderCounts = new HashMap<>();
        Set<Long> favoriteItemIds = new HashSet<>();
        
        for (ItemEntity item : hotItems) {
            if (!sellerNicknames.containsKey(item.getSellerId())) {
                SignupEntity seller = signupRepository.findById(item.getSellerId()).orElse(null);
                sellerNicknames.put(item.getSellerId(), seller != null ? seller.getNickname() : "알 수 없음");
            }
            bidderCounts.put(item.getId(), bidService.getBidderCount(item.getId()));
            if (userId != null && favoriteService.isFavorite(userId, item.getId())) {
                favoriteItemIds.add(item.getId());
            }
        }
        
        model.addAttribute("sellerNicknames", sellerNicknames);
        model.addAttribute("bidderCounts", bidderCounts);
        model.addAttribute("favoriteItemIds", favoriteItemIds);
        model.addAttribute("categoryNames", getCategoryNames());

        // 통계 데이터
        long totalMembers = signupRepository.count();
        long totalItems = itemRepository.count();
        long activeAuctions = itemRepository.countByStatus("bidding");
        
        // 최고가 낙찰 찾기 (ended 상태 중 currentPrice가 가장 높은 것)
        Integer highestBid = itemService.getAllItems().stream()
                .filter(item -> "ended".equals(item.getStatus()))
                .map(item -> item.getCurrentPrice() != null ? item.getCurrentPrice() : item.getStartPrice())
                .max(Integer::compareTo)
                .orElse(0);
        
        model.addAttribute("totalMembers", totalMembers);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("activeAuctions", activeAuctions);
        model.addAttribute("highestBid", highestBid);

        return "index";
    }

    private Map<String, String> getCategoryNames() {
        Map<String, String> categoryNames = new HashMap<>();
        categoryNames.put("digital", "디지털/가전");
        categoryNames.put("computer", "컴퓨터/주변기기");
        categoryNames.put("fashion", "패션의류/잡화");
        categoryNames.put("beauty", "뷰티/미용");
        categoryNames.put("furniture", "가구/인테리어");
        categoryNames.put("living", "주방/생활용품");
        categoryNames.put("sports", "스포츠/레저");
        categoryNames.put("books", "도서/티켓/굿즈");
        categoryNames.put("collectibles", "컬렉터블/수집품");
        categoryNames.put("hobby", "악기/취미");
        categoryNames.put("vehicle", "차량/오토바이 용품");
        categoryNames.put("etc", "기타/반려동물");
        return categoryNames;
    }
}
