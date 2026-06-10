package com.example.final_project.Controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {
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

        return "index";
    }
}
