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
import java.util.List;
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

            return "seller-profile";
        } catch (Exception e) {
            log.error("Error loading seller profile", e);
            return "redirect:/items";
        }
    }
}
