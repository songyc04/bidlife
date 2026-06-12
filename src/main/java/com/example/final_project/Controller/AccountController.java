package com.example.final_project.Controller;

import com.example.final_project.Entity.SignupEntity;
import com.example.final_project.Repository.SignupRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/account")
public class AccountController {
    private final SignupRepository signupRepository;

    @GetMapping("")
    public String account(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login?redirect=/account";
        }

        Optional<SignupEntity> user = signupRepository.findById(userId);

        if (user.isPresent()) {
            model.addAttribute("email", user.get().getEmail());
            model.addAttribute("nickname", user.get().getNickname());
        }

        return "account";
    }

    @GetMapping("/bids")
    public String bids(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login?redirect=/account/bids";
        }

        Optional<SignupEntity> user = signupRepository.findById(userId);

        if (user.isPresent()) {
            model.addAttribute("nickname", user.get().getNickname());
        }

        return "account/bids";
    }

    @GetMapping("/items")
    public String items(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login?redirect=/account/items";
        }

        Optional<SignupEntity> user = signupRepository.findById(userId);

        if (user.isPresent()) {
            model.addAttribute("nickname", user.get().getNickname());
        }

        return "account/items";
    }

    @GetMapping("/picklist")
    public String picklist(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login?redirect=/account/picklist";
        }

        Optional<SignupEntity> user = signupRepository.findById(userId);

        if (user.isPresent()) {
            model.addAttribute("nickname", user.get().getNickname());
        }

        return "account/picklist";
    }

    @GetMapping("/items/new")
    public String itemsNew(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login?redirect=/account/items/new";
        }

        Optional<SignupEntity> user = signupRepository.findById(userId);

        if (user.isPresent()) {
            model.addAttribute("nickname", user.get().getNickname());
        }

        return "account/items-new";
    }
}
