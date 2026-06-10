package com.example.final_project.Controller;

import com.example.final_project.DTO.LoginDTO;
import com.example.final_project.Entity.SignupEntity;
import com.example.final_project.Repository.SignupRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/login")
public class LoginController {
    private final SignupRepository signupRepository;

    @GetMapping("")
    public String login(@RequestParam(value = "redirect", required = false) String redirectUrl, Model model) {
        if (redirectUrl != null && !redirectUrl.isEmpty()) {
            String decodedUrl = URLDecoder.decode(redirectUrl, StandardCharsets.UTF_8);
            model.addAttribute("redirect", decodedUrl);
        }
        return "login";
    }

    @PostMapping("")
    public String loginSubmit(LoginDTO loginDTO, HttpSession session, Model model,
                              @RequestParam(value = "redirect", required = false) String redirectUrl) {
        Optional<SignupEntity> user = signupRepository.findByEmail(loginDTO.getEmail());

        if (user.isPresent() && user.get().getPassword().equals(loginDTO.getPassword())) {
            session.setAttribute("userId", user.get().getId());
            session.setAttribute("email", user.get().getEmail());
            session.setAttribute("nickname", user.get().getNickname());

            if (redirectUrl != null && !redirectUrl.isEmpty()) {
                return "redirect:" + redirectUrl;
            }
            return "redirect:/";
        }

        model.addAttribute("error", "이메일 또는 비밀번호가 올바르지 않습니다.");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}