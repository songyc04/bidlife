package com.example.final_project.Controller;

import com.example.final_project.DTO.LoginDTO;
import com.example.final_project.Entity.SignupEntity;
import com.example.final_project.Repository.SignupRepository;
import com.example.final_project.Service.EmailService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/login")
public class LoginController {
    private final SignupRepository signupRepository;
    private final EmailService emailService;

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

            // 임시비밀번호로 로그인한 경우 비밀번호 변경 페이지로 리다이렉트
            if (user.get().isTemporaryPassword()) {
                return "redirect:/login/change-password";
            }

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

    @GetMapping("/find-password")
    public String findPassword() {
        return "find-password";
    }

    @PostMapping("/find-password")
    public String findPasswordSubmit(@RequestParam String email, RedirectAttributes redirectAttributes) {
        Optional<SignupEntity> user = signupRepository.findByEmail(email);

        if (user.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "등록되지 않은 이메일입니다.");
            return "redirect:/login/find-password";
        }

        try {
            String tempPassword = emailService.sendTemporaryPassword(email);
            
            // 임시 비밀번호 저장
            SignupEntity signupEntity = user.get();
            signupEntity.setPassword(tempPassword);
            signupEntity.setTemporaryPassword(true);
            signupRepository.save(signupEntity);

            // test.com인 경우 팝업으로 표시
            if (email.endsWith("@test.com")) {
                redirectAttributes.addFlashAttribute("tempPassword", tempPassword);
                redirectAttributes.addFlashAttribute("isTestMode", true);
            } else {
                redirectAttributes.addFlashAttribute("success", "임시 비밀번호가 이메일로 발송되었습니다.");
            }
            
            return "redirect:/login/find-password";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "비밀번호 재설정 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/login/find-password";
        }
    }

    @GetMapping("/change-password")
    public String changePassword(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        return "change-password";
    }

    @PostMapping("/change-password")
    public String changePasswordSubmit(@RequestParam String newPassword, 
                                       @RequestParam String confirmPassword,
                                       HttpSession session, 
                                       RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "비밀번호가 일치하지 않습니다.");
            return "redirect:/login/change-password";
        }

        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "비밀번호는 6자 이상이어야 합니다.");
            return "redirect:/login/change-password";
        }

        Optional<SignupEntity> user = signupRepository.findById(userId);
        if (user.isPresent()) {
            SignupEntity signupEntity = user.get();
            signupEntity.setPassword(newPassword);
            signupEntity.setTemporaryPassword(false);
            signupRepository.save(signupEntity);

            redirectAttributes.addFlashAttribute("success", "비밀번호가 변경되었습니다.");
            return "redirect:/";
        }

        return "redirect:/login";
    }
}