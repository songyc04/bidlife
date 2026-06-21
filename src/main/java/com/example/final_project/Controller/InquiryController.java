package com.example.final_project.Controller;

import com.example.final_project.Entity.InquiryEntity;
import com.example.final_project.Entity.SignupEntity;
import com.example.final_project.Repository.InquiryRepository;
import com.example.final_project.Repository.SignupRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/inquiry")
public class InquiryController {

    private final InquiryRepository inquiryRepository;
    private final SignupRepository signupRepository;

    @GetMapping("")
    public String form(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login?redirect=/inquiry";
        }

        List<InquiryEntity> myInquiries = inquiryRepository.findByUserIdOrderByCreatedAtDesc(userId);
        model.addAttribute("myInquiries", myInquiries);
        model.addAttribute("inquiryCount", myInquiries.size());
        return "inquiry";
    }

    @PostMapping("")
    public String submit(@RequestParam String category,
                         @RequestParam String title,
                         @RequestParam String content,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login?redirect=/inquiry";
        }

        if (title == null || title.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "제목을 입력해주세요.");
            return "redirect:/inquiry";
        }
        if (content == null || content.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "내용을 입력해주세요.");
            return "redirect:/inquiry";
        }
        if (title.length() > 200) {
            redirectAttributes.addFlashAttribute("error", "제목은 200자 이내로 입력해주세요.");
            return "redirect:/inquiry";
        }
        if (content.length() > 2000) {
            redirectAttributes.addFlashAttribute("error", "내용은 2000자 이내로 입력해주세요.");
            return "redirect:/inquiry";
        }

        Optional<SignupEntity> userOpt = signupRepository.findById(userId);
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "사용자 정보를 찾을 수 없습니다.");
            return "redirect:/inquiry";
        }
        SignupEntity user = userOpt.get();

        InquiryEntity inquiry = new InquiryEntity();
        inquiry.setUserId(userId);
        inquiry.setUserNickname(user.getNickname());
        inquiry.setUserEmail(user.getEmail());
        inquiry.setCategory(category);
        inquiry.setTitle(title.trim());
        inquiry.setContent(content.trim());
        inquiry.setStatus("WAITING");
        inquiryRepository.save(inquiry);

        log.info("문의 등록: userId={}, category={}, title={}", userId, category, title);

        redirectAttributes.addFlashAttribute("success", "문의가 성공적으로 등록되었습니다. 빠른 시일 내에 답변 드리겠습니다.");
        return "redirect:/inquiry";
    }
}
