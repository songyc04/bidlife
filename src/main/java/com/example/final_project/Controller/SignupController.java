package com.example.final_project.Controller;

import com.example.final_project.DTO.JoinDTO;
import com.example.final_project.Entity.SignupEntity;
import com.example.final_project.Repository.SignupRepository;
import com.example.final_project.Service.EmailService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/signup")
public class SignupController {
    private final SignupRepository signupRepository;
    private final EmailService emailService;

    @GetMapping("")
    public String signup() {
        return "signup";
    }

    @PostMapping("/send-code")
    @ResponseBody
    public String sendVerificationCode(@RequestParam String email, Model model) {
        try {
            emailService.sendVerificationEmail(email);
            return "SUCCESS";
        } catch (Exception e) {
            return "이메일 발송에 실패했습니다: " + e.getMessage();
        }
    }

    @PostMapping("/verify-code")
    @ResponseBody
    public String verifyCode(@RequestParam String email, @RequestParam String code, HttpSession session) {
        boolean verified = emailService.verifyCode(email, code);
        
        if (verified) {
            session.setAttribute("verifiedEmail", email);
            return "SUCCESS";
        }
        
        return "인증번호가 올바르지 않거나 만료되었습니다.";
    }

    @GetMapping("/register")
    public String registerForm(HttpSession session, Model model) {
        String verifiedEmail = (String) session.getAttribute("verifiedEmail");
        
        if (verifiedEmail == null) {
            return "redirect:/signup";
        }
        
        model.addAttribute("email", verifiedEmail);
        return "register";
    }

    @PostMapping("/register")
    public String registerSubmit(JoinDTO joinDTO, HttpSession session, RedirectAttributes redirectAttributes) {
        String verifiedEmail = (String) session.getAttribute("verifiedEmail");
        
        if (verifiedEmail == null || !verifiedEmail.equals(joinDTO.getEmail())) {
            redirectAttributes.addFlashAttribute("error", "이메일 인증이 필요합니다.");
            return "redirect:/signup";
        }
        
        if (signupRepository.existsByEmail(joinDTO.getEmail())) {
            redirectAttributes.addFlashAttribute("error", "이미 등록된 이메일입니다.");
            return "redirect:/signup";
        }
        
        if (signupRepository.existsByNickname(joinDTO.getNickname())) {
            redirectAttributes.addFlashAttribute("error", "이미 사용 중인 닉네임입니다.");
            return "redirect:/signup";
        }
        
        SignupEntity signupEntity = new SignupEntity();
        signupEntity.setEmail(joinDTO.getEmail());
        signupEntity.setPassword(joinDTO.getPassword());
        signupEntity.setNickname(joinDTO.getNickname());
        
        signupRepository.save(signupEntity);
        
        session.removeAttribute("verifiedEmail");
        
        return "redirect:/";
    }

    @GetMapping("/complete")
    public String complete() {
        return "join_complete";
    }
}
