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

@Slf4j
@Controller
@RequiredArgsConstructor
public class ItemsController {

    private final ItemService itemService;
    private final SignupRepository signupRepository;

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

            List<ItemEntity> items = itemService.getAllItems();
            model.addAttribute("items", items != null ? items : new ArrayList<>());

            Map<Long, String> sellerNicknames = new HashMap<>();
            if (items != null) {
                for (ItemEntity item : items) {
                    if (!sellerNicknames.containsKey(item.getSellerId())) {
                        SignupEntity seller = signupRepository.findById(item.getSellerId()).orElse(null);
                        sellerNicknames.put(item.getSellerId(), seller != null ? seller.getNickname() : "알 수 없음");
                    }
                }
            }
            model.addAttribute("sellerNicknames", sellerNicknames);

            return "items";
        } catch (Exception e) {
            log.error("Error loading items page", e);
            model.addAttribute("isLoggedIn", false);
            model.addAttribute("items", new ArrayList<>());
            model.addAttribute("sellerNicknames", new HashMap<>());
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
            } else {
                model.addAttribute("isLoggedIn", false);
            }

            ItemEntity item = itemService.getItemById(id);
            if (item == null) {
                return "redirect:/items";
            }

            model.addAttribute("item", item);

            SignupEntity seller = signupRepository.findById(item.getSellerId()).orElse(null);
            model.addAttribute("sellerNickname", seller != null ? seller.getNickname() : "알 수 없음");

            return "items-detail";
        } catch (Exception e) {
            log.error("Error loading item detail page", e);
            return "redirect:/items";
        }
    }
}
