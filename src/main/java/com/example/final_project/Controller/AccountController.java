package com.example.final_project.Controller;

import com.example.final_project.Entity.SignupEntity;
import com.example.final_project.Repository.SignupRepository;
import com.example.final_project.Service.ItemService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/account")
public class AccountController {
    private final SignupRepository signupRepository;
    private final ItemService itemService;

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
        try {
            Long userId = (Long) session.getAttribute("userId");

            if (userId == null) {
                return "redirect:/login?redirect=/account/items";
            }

            Optional<SignupEntity> user = signupRepository.findById(userId);

            if (user.isPresent()) {
                model.addAttribute("nickname", user.get().getNickname());
            }

            List<com.example.final_project.Entity.ItemEntity> myItems = itemService.getItemsBySeller(userId);
            model.addAttribute("myItems", myItems != null ? myItems : new java.util.ArrayList<>());

            return "account/items";
        } catch (Exception e) {
            log.error("Error loading my items page", e);
            model.addAttribute("myItems", new java.util.ArrayList<>());
            return "account/items";
        }
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

    @PostMapping("/items/new")
    public String itemsNewPost(HttpSession session,
                               @RequestParam String title,
                               @RequestParam String category,
                               @RequestParam String description,
                               @RequestParam Integer startPrice,
                               @RequestParam(required = false) Integer buyNowPrice,
                               @RequestParam Integer bidUnit,
                               @RequestParam String startTime,
                               @RequestParam String endTime,
                               @RequestParam(required = false) MultipartFile[] images,
                               RedirectAttributes redirectAttributes) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login?redirect=/account/items/new";
        }

        try {
            LocalDateTime startDt = LocalDateTime.parse(startTime);
            LocalDateTime endDt = LocalDateTime.parse(endTime);

            itemService.saveItem(title, category, description, startPrice, buyNowPrice, bidUnit, startDt, endDt, userId, images);

            redirectAttributes.addFlashAttribute("successMessage", "경매가 성공적으로 등록되었습니다.");
            return "redirect:/account/items";
        } catch (Exception e) {
            log.error("경매 등록 실패: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "경매 등록 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/account/items/new";
        }
    }

    @PostMapping("/update-nickname")
    public String updateNickname(HttpSession session,
                                  @RequestParam String newNickname,
                                  RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login?redirect=/account";
        }

        try {
            if (newNickname == null || newNickname.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "닉네임을 입력해주세요.");
                return "redirect:/account";
            }

            if (newNickname.length() > 50) {
                redirectAttributes.addFlashAttribute("errorMessage", "닉네임은 50자 이하여야 합니다.");
                return "redirect:/account";
            }

            if (signupRepository.existsByNickname(newNickname)) {
                redirectAttributes.addFlashAttribute("errorMessage", "이미 사용 중인 닉네임입니다.");
                return "redirect:/account";
            }

            Optional<SignupEntity> user = signupRepository.findById(userId);
            if (user.isPresent()) {
                user.get().setNickname(newNickname);
                signupRepository.save(user.get());
                session.setAttribute("nickname", newNickname);
                redirectAttributes.addFlashAttribute("successMessage", "닉네임이 변경되었습니다.");
            }

            return "redirect:/account";
        } catch (Exception e) {
            log.error("닉네임 변경 실패: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "닉네임 변경 중 오류가 발생했습니다.");
            return "redirect:/account";
        }
    }

    @PostMapping("/update-password")
    public String updatePassword(HttpSession session,
                                  @RequestParam String currentPassword,
                                  @RequestParam String newPassword,
                                  @RequestParam String confirmPassword,
                                  RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login?redirect=/account";
        }

        try {
            if (currentPassword == null || currentPassword.isEmpty()) {
                redirectAttributes.addFlashAttribute("passwordErrorMessage", "현재 비밀번호를 입력해주세요.");
                return "redirect:/account";
            }

            if (newPassword == null || newPassword.length() < 6) {
                redirectAttributes.addFlashAttribute("passwordErrorMessage", "새 비밀번호는 6자 이상이어야 합니다.");
                return "redirect:/account";
            }

            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("passwordErrorMessage", "새 비밀번호가 일치하지 않습니다.");
                return "redirect:/account";
            }

            Optional<SignupEntity> user = signupRepository.findById(userId);
            if (user.isPresent()) {
                if (!user.get().getPassword().equals(currentPassword)) {
                    redirectAttributes.addFlashAttribute("passwordErrorMessage", "현재 비밀번호가 일치하지 않습니다.");
                    return "redirect:/account";
                }

                user.get().setPassword(newPassword);
                signupRepository.save(user.get());
                redirectAttributes.addFlashAttribute("passwordSuccessMessage", "비밀번호가 변경되었습니다.");
            }

            return "redirect:/account";
        } catch (Exception e) {
            log.error("비밀번호 변경 실패: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("passwordErrorMessage", "비밀번호 변경 중 오류가 발생했습니다.");
            return "redirect:/account";
        }
    }
}
