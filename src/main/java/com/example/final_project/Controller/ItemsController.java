package com.example.final_project.Controller;

import com.example.final_project.Entity.ItemEntity;
import com.example.final_project.Service.ItemService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ItemsController {

    private final ItemService itemService;

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

            return "items";
        } catch (Exception e) {
            log.error("Error loading items page", e);
            model.addAttribute("isLoggedIn", false);
            model.addAttribute("items", new ArrayList<>());
            return "items";
        }
    }
}
