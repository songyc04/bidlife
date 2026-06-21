package com.example.final_project.Controller;

import com.example.final_project.Entity.NoticeEntity;
import com.example.final_project.Repository.NoticeRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/notices")
public class NoticeController {

    private final NoticeRepository noticeRepository;

    @GetMapping("")
    public String list(HttpSession session, Model model) {
        addLoginStatus(session, model);
        List<NoticeEntity> notices = noticeRepository.findAllByOrderByIsImportantDescCreatedAtDesc();
        model.addAttribute("notices", notices);
        return "notice/list";
    }

    @GetMapping("/{id}")
    @Transactional
    public String detail(@PathVariable Long id, HttpSession session, Model model) {
        addLoginStatus(session, model);
        Optional<NoticeEntity> noticeOpt = noticeRepository.findById(id);
        if (noticeOpt.isEmpty()) {
            return "redirect:/notices";
        }
        noticeRepository.incrementViewCount(id);
        NoticeEntity notice = noticeOpt.get();
        notice.setViewCount(notice.getViewCount() + 1);
        model.addAttribute("notice", notice);
        model.addAttribute("recentNotices", noticeRepository.findTop5ByOrderByIsImportantDescCreatedAtDesc());
        return "notice/detail";
    }

    private void addLoginStatus(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        model.addAttribute("isLoggedIn", userId != null);
    }
}
