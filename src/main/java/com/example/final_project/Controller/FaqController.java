package com.example.final_project.Controller;

import com.example.final_project.Entity.FaqEntity;
import com.example.final_project.Repository.FaqRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/faq")
public class FaqController {

    private final FaqRepository faqRepository;

    @GetMapping("")
    public String list(@RequestParam(required = false) String category, HttpSession session, Model model) {
        addLoginStatus(session, model);
        List<FaqEntity> faqs;
        if (category != null && !category.isEmpty() && !"전체".equals(category)) {
            faqs = faqRepository.findByCategoryOrderByDisplayOrderAsc(category);
        } else {
            faqs = faqRepository.findAllByOrderByDisplayOrderAsc();
        }
        Map<String, List<FaqEntity>> grouped = new LinkedHashMap<>();
        for (FaqEntity faq : faqs) {
            grouped.computeIfAbsent(faq.getCategory(), k -> new java.util.ArrayList<>()).add(faq);
        }
        model.addAttribute("groupedFaqs", grouped);
        model.addAttribute("selectedCategory", category == null ? "전체" : category);
        return "faq";
    }

    private void addLoginStatus(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        model.addAttribute("isLoggedIn", userId != null);
    }
}
