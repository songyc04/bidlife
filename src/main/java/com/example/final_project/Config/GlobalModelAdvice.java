package com.example.final_project.Config;

import com.example.final_project.Entity.SignupEntity;
import com.example.final_project.Repository.SignupRepository;
import com.example.final_project.Service.NotificationService;
import com.example.final_project.Service.ProfileGradientService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Optional;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAdvice {

    private final SignupRepository signupRepository;
    private final NotificationService notificationService;
    private final ProfileGradientService profileGradientService;

    @ModelAttribute
    public void addGlobalAttributes(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId != null) {
            try {
                model.addAttribute("isLoggedIn", true);
                model.addAttribute("userId", userId);

                String nickname = (String) session.getAttribute("nickname");
                Optional<SignupEntity> userOpt = signupRepository.findById(userId);
                if (userOpt.isPresent()) {
                    SignupEntity user = userOpt.get();
                    if (nickname == null) {
                        nickname = user.getNickname();
                        session.setAttribute("nickname", nickname);
                    }
                    model.addAttribute("profileImage", user.getProfileImage());
                    String gradient = user.getProfileGradient();
                    if (gradient == null || gradient.isEmpty()) {
                        gradient = profileGradientService.generateGradientFromSeed(user.getNickname());
                    }
                    model.addAttribute("profileGradient", gradient);
                    model.addAttribute("profileGradientCss", profileGradientService.toCssGradient(gradient));
                } else {
                    model.addAttribute("profileImage", null);
                    model.addAttribute("profileGradient", profileGradientService.getDefaultGradient());
                    model.addAttribute("profileGradientCss", profileGradientService.toCssGradient(null));
                }
                model.addAttribute("nickname", nickname);

                long unreadCount = notificationService.getUnreadCount(userId);
                model.addAttribute("unreadCount", unreadCount);
            } catch (Exception e) {
                log.error("Error in GlobalModelAdvice for userId={}", userId, e);
                model.addAttribute("nickname", "");
                model.addAttribute("profileImage", null);
                model.addAttribute("profileGradient", profileGradientService.getDefaultGradient());
                model.addAttribute("profileGradientCss", profileGradientService.toCssGradient(null));
                model.addAttribute("unreadCount", 0L);
            }
        } else {
            model.addAttribute("isLoggedIn", false);
        }
    }
}
