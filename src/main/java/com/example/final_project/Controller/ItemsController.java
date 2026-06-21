package com.example.final_project.Controller;

import com.example.final_project.Entity.ItemEntity;
import com.example.final_project.Entity.SignupEntity;
import com.example.final_project.Repository.SignupRepository;
import com.example.final_project.Service.BidService;
import com.example.final_project.Service.FavoriteService;
import com.example.final_project.Service.ItemService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ItemsController {

    private final ItemService itemService;
    private final SignupRepository signupRepository;
    private final BidService bidService;
    private final FavoriteService favoriteService;

    @GetMapping("/items")
    public String items(HttpSession session, Model model) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            String nickname = (String) session.getAttribute("nickname");

            if (userId != null) {
                model.addAttribute("isLoggedIn", true);
                model.addAttribute("nickname", nickname);
            } else {
                model.addAttribute("isLoggedIn", false);
            }

            List<ItemEntity> items;
            if (userId != null) {
                items = itemService.getAllItemsExcludingSeller(userId);
            } else {
                items = itemService.getAllItems();
            }
            model.addAttribute("items", items != null ? items : new ArrayList<>());

            Map<Long, String> sellerNicknames = new HashMap<>();
            Set<Long> favoriteItemIds = new HashSet<>();
            Map<Long, Long> bidderCounts = new HashMap<>();
            if (items != null) {
                for (ItemEntity item : items) {
                    if (!sellerNicknames.containsKey(item.getSellerId())) {
                        SignupEntity seller = signupRepository.findById(item.getSellerId()).orElse(null);
                        sellerNicknames.put(item.getSellerId(), seller != null ? seller.getNickname() : "알 수 없음");
                    }
                    if (userId != null && favoriteService.isFavorite(userId, item.getId())) {
                        favoriteItemIds.add(item.getId());
                    }
                    bidderCounts.put(item.getId(), bidService.getBidderCount(item.getId()));
                }
            }
            model.addAttribute("sellerNicknames", sellerNicknames);
            model.addAttribute("favoriteItemIds", favoriteItemIds);
            model.addAttribute("bidderCounts", bidderCounts);
            model.addAttribute("categoryNames", getCategoryNames());

            return "items";
        } catch (Exception e) {
            log.error("Error loading items page", e);
            model.addAttribute("isLoggedIn", false);
            model.addAttribute("items", new ArrayList<>());
            model.addAttribute("sellerNicknames", new HashMap<>());
            model.addAttribute("favoriteItemIds", new HashSet<>());
            return "items";
        }
    }

    @GetMapping("/items/{id}")
    public String itemDetail(@PathVariable Long id, HttpSession session, Model model) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            String nickname = (String) session.getAttribute("nickname");

            if (userId != null) {
                model.addAttribute("isLoggedIn", true);
                model.addAttribute("nickname", nickname);
                model.addAttribute("userId", userId);
            } else {
                model.addAttribute("isLoggedIn", false);
                model.addAttribute("userId", null);
            }

            ItemEntity item = itemService.getItemById(id);
            if (item == null) {
                return "redirect:/items";
            }

            model.addAttribute("item", item);

            boolean isOwner = userId != null && userId.equals(item.getSellerId());
            model.addAttribute("isOwner", isOwner);

            boolean isWinner = userId != null && item.getWinnerId() != null && userId.equals(item.getWinnerId());
            model.addAttribute("isWinner", isWinner);

            boolean isFavorite = userId != null && favoriteService.isFavorite(userId, id);
            model.addAttribute("isFavorite", isFavorite);

            long bidderCount = bidService.getBidderCount(id);
            model.addAttribute("bidderCount", bidderCount);

            SignupEntity seller = signupRepository.findById(item.getSellerId()).orElse(null);
            model.addAttribute("sellerNickname", seller != null ? seller.getNickname() : "알 수 없음");
            model.addAttribute("categoryNames", getCategoryNames());

            if (item.getStatus().equals("ended") && item.getWinnerId() != null) {
                SignupEntity winner = signupRepository.findById(item.getWinnerId()).orElse(null);
                model.addAttribute("winnerNickname", winner != null ? winner.getNickname() : "알 수 없음");
            } else {
                model.addAttribute("winnerNickname", null);
            }

            return "items-detail";
        } catch (Exception e) {
            log.error("Error loading item detail page", e);
            return "redirect:/items";
        }
    }

    @PostMapping("/items/{id}/bid")
    public String placeBid(@PathVariable Long id,
                           @RequestParam(name = "bidAmount") Integer bidAmount,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login?redirect=/items/" + id;
        }

        try {
            bidService.placeBid(id, userId, bidAmount);
            redirectAttributes.addFlashAttribute("bidSuccessMessage", "입찰이 완료되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("bidErrorMessage", e.getMessage());
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("bidErrorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("입찰 실패: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("bidErrorMessage", "입찰 중 오류가 발생했습니다.");
        }

        return "redirect:/items/" + id;
    }

    @PostMapping("/items/{id}/buy-now")
    public String buyNow(@PathVariable Long id,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login?redirect=/items/" + id;
        }

        try {
            itemService.buyNow(id, userId);
            redirectAttributes.addFlashAttribute("successMessage", "즉시 구매가 완료되었습니다. 결제를 진행해주세요.");
            return "redirect:/payment/" + id;
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("bidErrorMessage", e.getMessage());
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("bidErrorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("즉시 구매 실패: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("bidErrorMessage", "즉시 구매 중 오류가 발생했습니다.");
        }

        return "redirect:/items/" + id;
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
