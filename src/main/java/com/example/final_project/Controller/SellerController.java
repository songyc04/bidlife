package com.example.final_project.Controller;

import com.example.final_project.Entity.ItemEntity;
import com.example.final_project.Entity.SignupEntity;
import com.example.final_project.Repository.SignupRepository;
import com.example.final_project.Service.ItemService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
public class SellerController {

    private final ItemService itemService;
    private final SignupRepository signupRepository;

    @GetMapping("/sellers/{sellerId}")
    public String sellerProfile(@PathVariable Long sellerId, HttpSession session, Model model) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            String nickname = (String) session.getAttribute("nickname");

            if (userId != null) {
                model.addAttribute("isLoggedIn", true);
                model.addAttribute("nickname", nickname);
            } else {
                model.addAttribute("isLoggedIn", false);
            }

            SignupEntity seller = signupRepository.findById(sellerId).orElse(null);
            if (seller == null) {
                return "redirect:/items";
            }

            model.addAttribute("seller", seller);

            List<ItemEntity> allItems = itemService.getItemsBySeller(sellerId);
            if (allItems == null) {
                allItems = new ArrayList<>();
            }

            List<ItemEntity> activeItems = allItems.stream()
                    .filter(item -> "bidding".equals(item.getStatus()) || "upcoming".equals(item.getStatus()))
                    .collect(Collectors.toList());

            List<ItemEntity> soldItems = allItems.stream()
                    .filter(item -> "ended".equals(item.getStatus()))
                    .collect(Collectors.toList());

            model.addAttribute("activeItems", activeItems);
            model.addAttribute("soldItems", soldItems);
            model.addAttribute("activeItemCount", activeItems.size());
            model.addAttribute("soldItemCount", soldItems.size());
            model.addAttribute("categoryNames", getCategoryNames());

            return "seller-profile";
        } catch (Exception e) {
            log.error("Error loading seller profile", e);
            return "redirect:/items";
        }
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
