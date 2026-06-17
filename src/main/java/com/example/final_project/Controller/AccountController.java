package com.example.final_project.Controller;

import com.example.final_project.Entity.BidEntity;
import com.example.final_project.Entity.FavoriteEntity;
import com.example.final_project.Entity.ItemEntity;
import com.example.final_project.Entity.NotificationEntity;
import com.example.final_project.Entity.SignupEntity;
import com.example.final_project.Repository.SignupRepository;
import com.example.final_project.Service.BidService;
import com.example.final_project.Service.FavoriteService;
import com.example.final_project.Service.ItemService;
import com.example.final_project.Service.NotificationService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/account")
public class AccountController {
    private final SignupRepository signupRepository;
    private final ItemService itemService;
    private final NotificationService notificationService;
    private final BidService bidService;
    private final FavoriteService favoriteService;

    @Value("${file.upload-dir:uploads/profile}")
    private String uploadDir;

    private String absoluteUploadDir;

    @PostConstruct
    public void init() {
        Path path = Paths.get(uploadDir).toAbsolutePath();
        absoluteUploadDir = path.toString();
        File dir = new File(absoluteUploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

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
            model.addAttribute("profileImage", user.get().getProfileImage());
        }

        // 읽지 않은 알림 개수
        long unreadCount = notificationService.getUnreadCount(userId);
        model.addAttribute("unreadCount", unreadCount);

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
            model.addAttribute("profileImage", user.get().getProfileImage());
        }

        // 읽지 않은 알림 개수
        long unreadCount = notificationService.getUnreadCount(userId);
        model.addAttribute("unreadCount", unreadCount);

        try {
            List<BidEntity> myBids = bidService.getBidsByBidderId(userId);
            List<Map<String, Object>> bidDetails = new ArrayList<>();

            if (myBids != null) {
                for (BidEntity bid : myBids) {
                    Map<String, Object> bidDetail = new HashMap<>();
                    bidDetail.put("bid", bid);

                    ItemEntity item = itemService.getItemById(bid.getItemId());
                    if (item != null) {
                        bidDetail.put("item", item);
                    }

                    bidDetails.add(bidDetail);
                }
            }

            model.addAttribute("bidDetails", bidDetails);
        } catch (Exception e) {
            log.error("Error loading bid history", e);
            model.addAttribute("bidDetails", new ArrayList<>());
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
                model.addAttribute("profileImage", user.get().getProfileImage());
            }

            // 읽지 않은 알림 개수
            long unreadCount = notificationService.getUnreadCount(userId);
            model.addAttribute("unreadCount", unreadCount);

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
            model.addAttribute("profileImage", user.get().getProfileImage());
        }

        // 읽지 않은 알림 개수
        long unreadCount = notificationService.getUnreadCount(userId);
        model.addAttribute("unreadCount", unreadCount);

        try {
            List<FavoriteEntity> favorites = favoriteService.getFavoritesByUserId(userId);
            List<Map<String, Object>> favoriteDetails = new ArrayList<>();

            if (favorites != null) {
                for (FavoriteEntity favorite : favorites) {
                    Map<String, Object> favoriteDetail = new HashMap<>();
                    favoriteDetail.put("favorite", favorite);

                    ItemEntity item = itemService.getItemById(favorite.getItemId());
                    if (item != null) {
                        favoriteDetail.put("item", item);
                    }

                    favoriteDetails.add(favoriteDetail);
                }
            }

            model.addAttribute("favoriteDetails", favoriteDetails);
        } catch (Exception e) {
            log.error("Error loading favorites", e);
            model.addAttribute("favoriteDetails", new ArrayList<>());
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
            model.addAttribute("profileImage", user.get().getProfileImage());
        }

        // 읽지 않은 알림 개수
        long unreadCount = notificationService.getUnreadCount(userId);
        model.addAttribute("unreadCount", unreadCount);

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

    @PostMapping("/items/{id}/delete")
    public String deleteItem(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login?redirect=/account/items";
        }

        try {
            itemService.deleteItem(id, userId);
            redirectAttributes.addFlashAttribute("successMessage", "경매가 성공적으로 삭제되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting item", e);
            redirectAttributes.addFlashAttribute("errorMessage", "경매 삭제 중 오류가 발생했습니다.");
        }

        return "redirect:/account/items";
    }

    @GetMapping("/items/{id}/manage")
    public String manageItem(@PathVariable Long id, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login?redirect=/account/items/" + id + "/manage";
        }

        try {
            Optional<SignupEntity> user = signupRepository.findById(userId);
            if (user.isPresent()) {
                model.addAttribute("nickname", user.get().getNickname());
                model.addAttribute("profileImage", user.get().getProfileImage());
            }

            // 읽지 않은 알림 개수
            long unreadCount = notificationService.getUnreadCount(userId);
            model.addAttribute("unreadCount", unreadCount);

            ItemEntity item = itemService.getItemById(id);
            if (item == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "존재하지 않는 경매입니다.");
                return "redirect:/account/items";
            }

            if (!item.getSellerId().equals(userId)) {
                redirectAttributes.addFlashAttribute("errorMessage", "관리 권한이 없습니다.");
                return "redirect:/account/items";
            }

            model.addAttribute("item", item);
            return "account/items-manage";
        } catch (Exception e) {
            log.error("Error loading manage page", e);
            redirectAttributes.addFlashAttribute("errorMessage", "관리 페이지를 불러오는 중 오류가 발생했습니다.");
            return "redirect:/account/items";
        }
    }

    @PostMapping("/items/{id}/end")
    public String endItemEarly(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login?redirect=/account/items";
        }

        try {
            itemService.endItemEarly(id, userId);
            redirectAttributes.addFlashAttribute("successMessage", "경매가 조기 종료되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("Error ending item", e);
            redirectAttributes.addFlashAttribute("errorMessage", "경매 종료 중 오류가 발생했습니다.");
        }

        return "redirect:/account/items/" + id + "/manage";
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

    @PostMapping("/update-profile-image")
    public String updateProfileImage(HttpSession session,
                                      @RequestParam("profileImage") MultipartFile profileImage,
                                      RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login?redirect=/account";
        }

        try {
            if (profileImage == null || profileImage.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "이미지를 선택해주세요.");
                return "redirect:/account";
            }

            String originalName = profileImage.getOriginalFilename();
            String extension = "";
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }

            String fileName = UUID.randomUUID().toString() + extension;
            File dest = new File(absoluteUploadDir, fileName);
            profileImage.transferTo(dest);

            String imagePath = "/uploads/profile/" + fileName;

            Optional<SignupEntity> user = signupRepository.findById(userId);
            if (user.isPresent()) {
                String oldImage = user.get().getProfileImage();
                if (oldImage != null && !oldImage.isEmpty()) {
                    File oldFile = new File(Paths.get(oldImage).toString());
                    if (oldFile.exists()) {
                        oldFile.delete();
                    }
                }
                user.get().setProfileImage(imagePath);
                signupRepository.save(user.get());
                redirectAttributes.addFlashAttribute("successMessage", "프로필 이미지가 변경되었습니다.");
            }

            return "redirect:/account";
        } catch (IOException e) {
            log.error("프로필 이미지 업로드 실패: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "프로필 이미지 업로드 중 오류가 발생했습니다.");
            return "redirect:/account";
        }
    }

    @GetMapping("/notifications")
    public String notifications(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login?redirect=/account/notifications";
        }

        Optional<SignupEntity> user = signupRepository.findById(userId);

        if (user.isPresent()) {
            model.addAttribute("nickname", user.get().getNickname());
            model.addAttribute("profileImage", user.get().getProfileImage());
        }

        // 읽지 않은 알림 개수
        long unreadCount = notificationService.getUnreadCount(userId);
        model.addAttribute("unreadCount", unreadCount);

        List<NotificationEntity> notifications = notificationService.getNotificationsByUserId(userId);
        model.addAttribute("notifications", notifications);

        return "account/notifications";
    }

    @PostMapping("/notifications/{id}/read")
    public String markNotificationAsRead(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login?redirect=/account/notifications";
        }

        notificationService.markAsRead(id);
        return "redirect:/account/notifications";
    }

    @PostMapping("/notifications/read-all")
    public String markAllNotificationsAsRead(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login?redirect=/account/notifications";
        }

        notificationService.markAllAsRead(userId);
        return "redirect:/account/notifications";
    }

    @PostMapping("/favorites/toggle")
    public ResponseEntity<Map<String, Object>> toggleFavorite(@RequestParam Long itemId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        Map<String, Object> response = new HashMap<>();

        if (userId == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return ResponseEntity.status(401).body(response);
        }

        try {
            boolean isFavorite = favoriteService.toggleFavorite(userId, itemId);
            response.put("success", true);
            response.put("isFavorite", isFavorite);
            response.put("message", isFavorite ? "찜 목록에 추가되었습니다." : "찜 목록에서 제거되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error toggling favorite", e);
            response.put("success", false);
            response.put("message", "처리 중 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(response);
        }
    }
}
