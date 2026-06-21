package com.example.final_project.Controller;

import com.example.final_project.Entity.BidEntity;
import com.example.final_project.Entity.FavoriteEntity;
import com.example.final_project.Entity.ItemEntity;
import com.example.final_project.Entity.NotificationEntity;
import com.example.final_project.Entity.SignupEntity;
import com.example.final_project.Repository.BidRepository;
import com.example.final_project.Repository.FavoriteRepository;
import com.example.final_project.Repository.ItemRepository;
import com.example.final_project.Repository.NotificationRepository;
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
import org.springframework.web.bind.annotation.ResponseBody;
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
    private final ItemRepository itemRepository;
    private final BidRepository bidRepository;
    private final NotificationRepository notificationRepository;
    private final FavoriteRepository favoriteRepository;

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
    public String account(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login?redirect=/account";
        }

        try {
            model.addAttribute("isLoggedIn", true);
            if (!model.containsAttribute("errorMessage")) model.addAttribute("errorMessage", null);
            if (!model.containsAttribute("successMessage")) model.addAttribute("successMessage", null);
            if (!model.containsAttribute("passwordErrorMessage")) model.addAttribute("passwordErrorMessage", null);
            if (!model.containsAttribute("passwordSuccessMessage")) model.addAttribute("passwordSuccessMessage", null);
            model.addAttribute("email", "");
            model.addAttribute("nickname", "");
            model.addAttribute("profileImage", null);
            if (!model.containsAttribute("profileGradient")) {
                model.addAttribute("profileGradient", "");
                model.addAttribute("profileGradientCss", "linear-gradient(135deg, #ff5a00, #ff7c33)");
            }
            model.addAttribute("unreadCount", 0L);

            Optional<SignupEntity> user = signupRepository.findById(userId);

            if (user.isPresent()) {
                model.addAttribute("email", user.get().getEmail());
                model.addAttribute("nickname", user.get().getNickname());
                model.addAttribute("profileImage", user.get().getProfileImage());
            }

            try {
                long unreadCount = notificationService.getUnreadCount(userId);
                model.addAttribute("unreadCount", unreadCount);
            } catch (Exception e) {
                log.error("Error getting unread count for userId={}", userId, e);
            }

            return "account";
        } catch (Exception e) {
            log.error("Error loading account page for userId={}", userId, e);
            redirectAttributes.addFlashAttribute("errorMessage", "?섏씠吏瑜?遺덈윭?ㅻ뒗 以??ㅻ쪟媛 諛쒖깮?덉뒿?덈떎: " + e.getClass().getSimpleName());
            return "redirect:/";
        }
    }

    @GetMapping("/bids")
    public String bids(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login?redirect=/account/bids";
        }

        model.addAttribute("isLoggedIn", true);

        Optional<SignupEntity> user = signupRepository.findById(userId);

        if (user.isPresent()) {
            model.addAttribute("nickname", user.get().getNickname());
        }

        // ?쎌? ?딆? ?뚮┝ 媛쒖닔
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

            model.addAttribute("isLoggedIn", true);

            Optional<SignupEntity> user = signupRepository.findById(userId);

            if (user.isPresent()) {
                model.addAttribute("nickname", user.get().getNickname());
            }

            // ?쎌? ?딆? ?뚮┝ 媛쒖닔
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

        model.addAttribute("isLoggedIn", true);

        Optional<SignupEntity> user = signupRepository.findById(userId);

        if (user.isPresent()) {
            model.addAttribute("nickname", user.get().getNickname());
        }

        // ?쎌? ?딆? ?뚮┝ 媛쒖닔
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

        model.addAttribute("isLoggedIn", true);

        Optional<SignupEntity> user = signupRepository.findById(userId);

        if (user.isPresent()) {
            model.addAttribute("nickname", user.get().getNickname());
        }

        // ?쎌? ?딆? ?뚮┝ 媛쒖닔
        long unreadCount = notificationService.getUnreadCount(userId);
        model.addAttribute("unreadCount", unreadCount);

        return "account/items-new";
    }

    @PostMapping("/items/new")
    public String itemsNewPost(HttpSession session,
                               @RequestParam(name = "title") String title,
                               @RequestParam(name = "category") String category,
                               @RequestParam(name = "description") String description,
                               @RequestParam(name = "startPrice") Integer startPrice,
                               @RequestParam(name = "buyNowPrice", required = false) Integer buyNowPrice,
                               @RequestParam(name = "bidUnit") Integer bidUnit,
                               @RequestParam(name = "startTime") String startTime,
                               @RequestParam(name = "endTime") String endTime,
                               @RequestParam(required = false) MultipartFile[] images,
                               RedirectAttributes redirectAttributes) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login?redirect=/account/items/new";
        }

        // ?대?吏 寃利? 理쒖냼 1???댁긽 ?꾩닔
        if (images == null || images.length == 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "理쒖냼 1???댁긽???대?吏瑜??낅줈?쒗빐???⑸땲??");
            return "redirect:/account/items/new";
        }

        // 利됱떆 援щℓ媛 ?뺢퇋?? 0 ?먮뒗 null?대㈃ null濡?泥섎━ (?쇰컲 寃쎈ℓ濡?吏꾪뻾)
        Integer normalizedBuyNowPrice = (buyNowPrice != null && buyNowPrice > 0) ? buyNowPrice : null;

        // 利됱떆 援щℓ媛媛 ?쒖옉媛蹂대떎 ?묒쑝硫?臾댄슚
        if (normalizedBuyNowPrice != null && normalizedBuyNowPrice <= startPrice) {
            redirectAttributes.addFlashAttribute("errorMessage", "利됱떆 援щℓ媛???쒖옉媛蹂대떎 而ㅼ빞 ?⑸땲??");
            return "redirect:/account/items/new";
        }

        try {
            LocalDateTime startDt = LocalDateTime.parse(startTime);
            LocalDateTime endDt = LocalDateTime.parse(endTime);

            itemService.saveItem(title, category, description, startPrice, normalizedBuyNowPrice, bidUnit, startDt, endDt, userId, images);

            String message = (normalizedBuyNowPrice != null)
                    ? "寃쎈ℓ媛 ?깃났?곸쑝濡??깅줉?섏뿀?듬땲??"
                    : "寃쎈ℓ媛 ?깃났?곸쑝濡??깅줉?섏뿀?듬땲?? (?쇰컲 寃쎈ℓ濡?吏꾪뻾?⑸땲??";
            redirectAttributes.addFlashAttribute("successMessage", message);
            return "redirect:/account/items";
        } catch (Exception e) {
            log.error("寃쎈ℓ ?깅줉 ?ㅽ뙣: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "寃쎈ℓ ?깅줉 以??ㅻ쪟媛 諛쒖깮?덉뒿?덈떎: " + e.getMessage());
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
            redirectAttributes.addFlashAttribute("successMessage", "寃쎈ℓ媛 ?깃났?곸쑝濡???젣?섏뿀?듬땲??");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting item", e);
            redirectAttributes.addFlashAttribute("errorMessage", "寃쎈ℓ ??젣 以??ㅻ쪟媛 諛쒖깮?덉뒿?덈떎.");
        }

        return "redirect:/account/items";
    }

    @GetMapping("/items/{id}/manage")
    public String manageItem(@PathVariable Long id, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login?redirect=/account/items/" + id + "/manage";
        }

        model.addAttribute("isLoggedIn", true);

        try {
            Optional<SignupEntity> user = signupRepository.findById(userId);
            if (user.isPresent()) {
                model.addAttribute("nickname", user.get().getNickname());
            }

            // ?쎌? ?딆? ?뚮┝ 媛쒖닔
            long unreadCount = notificationService.getUnreadCount(userId);
            model.addAttribute("unreadCount", unreadCount);

            ItemEntity item = itemService.getItemById(id);
            if (item == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "議댁옱?섏? ?딅뒗 寃쎈ℓ?낅땲??");
                return "redirect:/account/items";
            }

            if (!item.getSellerId().equals(userId)) {
                redirectAttributes.addFlashAttribute("errorMessage", "愿由?沅뚰븳???놁뒿?덈떎.");
                return "redirect:/account/items";
            }

            model.addAttribute("item", item);

            if (item.getStatus().equals("ended") && item.getWinnerId() != null) {
                SignupEntity winner = signupRepository.findById(item.getWinnerId()).orElse(null);
                model.addAttribute("winnerNickname", winner != null ? winner.getNickname() : "?????놁쓬");
            } else {
                model.addAttribute("winnerNickname", null);
            }

            return "account/items-manage";
        } catch (Exception e) {
            log.error("Error loading manage page", e);
            redirectAttributes.addFlashAttribute("errorMessage", "愿由??섏씠吏瑜?遺덈윭?ㅻ뒗 以??ㅻ쪟媛 諛쒖깮?덉뒿?덈떎.");
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
            redirectAttributes.addFlashAttribute("successMessage", "寃쎈ℓ媛 議곌린 醫낅즺?섏뿀?듬땲??");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("Error ending item", e);
            redirectAttributes.addFlashAttribute("errorMessage", "寃쎈ℓ 醫낅즺 以??ㅻ쪟媛 諛쒖깮?덉뒿?덈떎.");
        }

        return "redirect:/account/items/" + id + "/manage";
    }

    @PostMapping("/items/{id}/seller-confirm")
    public String confirmPaymentBySeller(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login?redirect=/account/items";
        }

        try {
            itemService.confirmPaymentBySeller(id, userId);
            redirectAttributes.addFlashAttribute("successMessage", "?낃툑 ?뺤씤???꾨즺?섏뼱 嫄곕옒媛 ?깆궗?섏뿀?듬땲??");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("Error confirming payment", e);
            redirectAttributes.addFlashAttribute("errorMessage", "?낃툑 ?뺤씤 泥섎━ 以??ㅻ쪟媛 諛쒖깮?덉뒿?덈떎.");
        }

        return "redirect:/account/items/" + id + "/manage";
    }

    @PostMapping(value = "/update-nickname", produces = "application/json; charset=UTF-8")
    @ResponseBody
    public String updateNickname(HttpSession session,
                                  @RequestParam(name = "newNickname", required = false) String newNickname,
                                  RedirectAttributes redirectAttributes,
                                  jakarta.servlet.http.HttpServletRequest request) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            if (isAjax(request)) return jsonError("濡쒓렇?몄씠 ?꾩슂?⑸땲??");
            return "redirect:/login?redirect=/account";
        }

        try {
            if (newNickname == null || newNickname.trim().isEmpty()) {
                if (isAjax(request)) return jsonError("?됰꽕?꾩쓣 ?낅젰?댁＜?몄슂.");
                redirectAttributes.addFlashAttribute("errorMessage", "?됰꽕?꾩쓣 ?낅젰?댁＜?몄슂.");
                return "redirect:/account";
            }

            newNickname = newNickname.trim();

            if (newNickname.length() > 50) {
                if (isAjax(request)) return jsonError("?됰꽕?꾩? 50???댄븯?ъ빞 ?⑸땲??");
                redirectAttributes.addFlashAttribute("errorMessage", "?됰꽕?꾩? 50???댄븯?ъ빞 ?⑸땲??");
                return "redirect:/account";
            }

            Optional<SignupEntity> currentUser = signupRepository.findById(userId);
            if (currentUser.isEmpty()) {
                if (isAjax(request)) return jsonError("?ъ슜???뺣낫瑜?李얠쓣 ???놁뒿?덈떎.");
                redirectAttributes.addFlashAttribute("errorMessage", "?ъ슜???뺣낫瑜?李얠쓣 ???놁뒿?덈떎.");
                return "redirect:/login";
            }

            if (currentUser.get().getNickname().equals(newNickname)) {
                if (isAjax(request)) return jsonError("?꾩옱 ?됰꽕?꾧낵 ?숈씪?⑸땲??");
                redirectAttributes.addFlashAttribute("errorMessage", "?꾩옱 ?됰꽕?꾧낵 ?숈씪?⑸땲??");
                return "redirect:/account";
            }

            if (signupRepository.existsByNickname(newNickname)) {
                if (isAjax(request)) return jsonError("?대? ?ъ슜 以묒씤 ?됰꽕?꾩엯?덈떎.");
                redirectAttributes.addFlashAttribute("errorMessage", "?대? ?ъ슜 以묒씤 ?됰꽕?꾩엯?덈떎.");
                return "redirect:/account";
            }

            currentUser.get().setNickname(newNickname);
            signupRepository.save(currentUser.get());
            session.setAttribute("nickname", newNickname);
            if (isAjax(request)) {
                return "{\"success\": true, \"message\": \"?됰꽕?꾩씠 蹂寃쎈릺?덉뒿?덈떎.\", \"nickname\": \"" + newNickname + "\"}";
            }
            redirectAttributes.addFlashAttribute("successMessage", "?됰꽕?꾩씠 蹂寃쎈릺?덉뒿?덈떎.");
            return "redirect:/account";
        } catch (Exception e) {
            log.error("?됰꽕??蹂寃??ㅽ뙣: userId={}, newNickname={}", userId, newNickname, e);
            if (isAjax(request)) return jsonError("?됰꽕??蹂寃?以??ㅻ쪟媛 諛쒖깮?덉뒿?덈떎: " + e.getClass().getSimpleName());
            redirectAttributes.addFlashAttribute("errorMessage", "?됰꽕??蹂寃?以??ㅻ쪟媛 諛쒖깮?덉뒿?덈떎: " + e.getClass().getSimpleName());
            return "redirect:/account";
        }
    }

    private boolean isAjax(jakarta.servlet.http.HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }

    private String jsonError(String message) {
        return "{\"success\": false, \"message\": \"" + message.replace("\"", "\\\"") + "\"}";
    }

    @PostMapping("/update-password")
    public String updatePassword(HttpSession session,
                                  @RequestParam(name = "currentPassword", required = false) String currentPassword,
                                  @RequestParam(name = "newPassword", required = false) String newPassword,
                                  @RequestParam(name = "confirmPassword", required = false) String confirmPassword,
                                  RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login?redirect=/account";
        }

        try {
            if (currentPassword == null || currentPassword.isEmpty()) {
                redirectAttributes.addFlashAttribute("passwordErrorMessage", "?꾩옱 鍮꾨?踰덊샇瑜??낅젰?댁＜?몄슂.");
                return "redirect:/account";
            }

            if (newPassword == null || newPassword.length() < 6) {
                redirectAttributes.addFlashAttribute("passwordErrorMessage", "??鍮꾨?踰덊샇??6???댁긽?댁뼱???⑸땲??");
                return "redirect:/account";
            }

            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("passwordErrorMessage", "??鍮꾨?踰덊샇媛 ?쇱튂?섏? ?딆뒿?덈떎.");
                return "redirect:/account";
            }

            if (currentPassword.equals(newPassword)) {
                redirectAttributes.addFlashAttribute("passwordErrorMessage", "??鍮꾨?踰덊샇???꾩옱 鍮꾨?踰덊샇? ?щ씪???⑸땲??");
                return "redirect:/account";
            }

            Optional<SignupEntity> user = signupRepository.findById(userId);
            if (user.isEmpty()) {
                redirectAttributes.addFlashAttribute("passwordErrorMessage", "?ъ슜???뺣낫瑜?李얠쓣 ???놁뒿?덈떎.");
                return "redirect:/login";
            }

            if (!user.get().getPassword().equals(currentPassword)) {
                redirectAttributes.addFlashAttribute("passwordErrorMessage", "?꾩옱 鍮꾨?踰덊샇媛 ?쇱튂?섏? ?딆뒿?덈떎.");
                return "redirect:/account";
            }

            user.get().setPassword(newPassword);
            signupRepository.save(user.get());
            redirectAttributes.addFlashAttribute("passwordSuccessMessage", "鍮꾨?踰덊샇媛 蹂寃쎈릺?덉뒿?덈떎.");
            return "redirect:/account";
        } catch (Exception e) {
            log.error("鍮꾨?踰덊샇 蹂寃??ㅽ뙣: userId={}", userId, e);
            redirectAttributes.addFlashAttribute("passwordErrorMessage", "鍮꾨?踰덊샇 蹂寃?以??ㅻ쪟媛 諛쒖깮?덉뒿?덈떎: " + e.getClass().getSimpleName());
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
                redirectAttributes.addFlashAttribute("errorMessage", "?대?吏瑜??좏깮?댁＜?몄슂.");
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
                redirectAttributes.addFlashAttribute("successMessage", "?꾨줈???대?吏媛 蹂寃쎈릺?덉뒿?덈떎.");
            }

            return "redirect:/account";
        } catch (IOException e) {
            log.error("?꾨줈???대?吏 ?낅줈???ㅽ뙣: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "?꾨줈???대?吏 ?낅줈??以??ㅻ쪟媛 諛쒖깮?덉뒿?덈떎.");
            return "redirect:/account";
        }
    }

    @PostMapping(value = "/delete", produces = "application/json; charset=UTF-8")
    @ResponseBody
    public String deleteAccount(HttpSession session,
                                 @RequestParam(name = "reason", required = false) String reason,
                                 @RequestParam(name = "password", required = false) String password) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "{\"success\": false, \"message\": \"로그인이 필요합니다.\"}";
        }

        try {
            Optional<SignupEntity> userOpt = signupRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return "{\"success\": false, \"message\": \"사용자를 찾을 수 없습니다.\"}";
            }

            SignupEntity user = userOpt.get();

            if (password == null || !user.getPassword().equals(password)) {
                return "{\"success\": false, \"message\": \"비밀번호가 일치하지 않습니다.\"}";
            }

            log.info("계정 삭제 시작: userId={}, reason={}", userId, reason);

            List<ItemEntity> myItems = itemRepository.findBySellerIdOrderByCreatedAtDesc(userId);
            for (ItemEntity item : myItems) {
                if (item.getImagePaths() != null && !item.getImagePaths().isEmpty()) {
                    String[] paths = item.getImagePaths().split(", ");
                    for (String path : paths) {
                        try {
                            String fileName = path.substring(path.lastIndexOf("/") + 1);
                            File file = new File(itemService.getAbsoluteUploadDir(), fileName);
                            if (file.exists()) file.delete();
                        } catch (Exception e) {
                            log.warn("이미지 파일 삭제 실패: {}", path);
                        }
                    }
                }
            }
            itemRepository.deleteBySellerId(userId);
            bidRepository.deleteByBidderId(userId);
            favoriteRepository.deleteByUserId(userId);
            notificationRepository.deleteByUserId(userId);
            signupRepository.deleteById(userId);

            session.invalidate();

            log.info("계정 삭제 완료: userId={}", userId);

            return "{\"success\": true, \"message\": \"계정이 삭제되었습니다.\"}";
        } catch (Exception e) {
            log.error("계정 삭제 실패: userId={}", userId, e);
            return "{\"success\": false, \"message\": \"계정 삭제 중 오류가 발생했습니다.\"}";
        }
    }

    @GetMapping("/notifications")
    public String notifications(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login?redirect=/account/notifications";
        }

        model.addAttribute("isLoggedIn", true);

        Optional<SignupEntity> user = signupRepository.findById(userId);

        if (user.isPresent()) {
            model.addAttribute("nickname", user.get().getNickname());
        }

        // ?쎌? ?딆? ?뚮┝ 媛쒖닔
        long unreadCount = notificationService.getUnreadCount(userId);
        model.addAttribute("unreadCount", unreadCount);

        List<NotificationEntity> notifications = notificationService.getNotificationsByUserId(userId);
        model.addAttribute("notifications", notifications);

        // ?뚮┝蹂?linkUrl 怨꾩궛
        Map<Long, String> notificationLinks = new HashMap<>();
        for (NotificationEntity n : notifications) {
            if (n.getItemId() == null) {
                notificationLinks.put(n.getId(), "/account/notifications");
                continue;
            }
            String type = n.getType();
            // ?먮ℓ???뺤씤/愿由??뚮┝ ??愿由??섏씠吏
            if ("auction_seller_won".equals(type) || "auction_failed".equals(type)
                    || "new_bid".equals(type) || "payment_buyer_paid".equals(type)) {
                notificationLinks.put(n.getId(), "/account/items/" + n.getItemId() + "/manage");
            }
            // 援щℓ??寃곗젣 ?뚮┝ ??寃곗젣 ?섏씠吏
            else if ("auction_won".equals(type) || "buy_now_complete".equals(type)
                    || "payment_pending_seller".equals(type)) {
                notificationLinks.put(n.getId(), "/payment/" + n.getItemId());
            }
            // 嫄곕옒 ?꾨즺 ??嫄곕옒 ?댁뿭
            else if ("transaction_completed".equals(type)) {
                notificationLinks.put(n.getId(), "/account/transactions");
            }
            // 湲곕낯: 寃쎈ℓ ?곸꽭
            else {
                notificationLinks.put(n.getId(), "/items/" + n.getItemId());
            }
        }
        model.addAttribute("notificationLinks", notificationLinks);

        return "account/notifications";
    }

    @GetMapping("/transactions")
    public String transactions(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login?redirect=/account/transactions";
        }

        model.addAttribute("isLoggedIn", true);

        Optional<SignupEntity> user = signupRepository.findById(userId);
        if (user.isPresent()) {
            model.addAttribute("nickname", user.get().getNickname());
        }

        long unreadCount = notificationService.getUnreadCount(userId);
        model.addAttribute("unreadCount", unreadCount);

        List<ItemEntity> purchases = itemService.getPurchasesByUser(userId);
        List<ItemEntity> sales = itemService.getSalesByUser(userId);

        Map<Long, String> sellerNicknames = new HashMap<>();
        Map<Long, String> buyerNicknames = new HashMap<>();

        for (ItemEntity item : purchases) {
            if (!sellerNicknames.containsKey(item.getSellerId())) {
                SignupEntity seller = signupRepository.findById(item.getSellerId()).orElse(null);
                sellerNicknames.put(item.getSellerId(), seller != null ? seller.getNickname() : "?????놁쓬");
            }
        }
        for (ItemEntity item : sales) {
            if (item.getWinnerId() != null && !buyerNicknames.containsKey(item.getWinnerId())) {
                SignupEntity buyer = signupRepository.findById(item.getWinnerId()).orElse(null);
                buyerNicknames.put(item.getWinnerId(), buyer != null ? buyer.getNickname() : "?????놁쓬");
            }
        }

        model.addAttribute("purchases", purchases != null ? purchases : new ArrayList<>());
        model.addAttribute("sales", sales != null ? sales : new ArrayList<>());
        model.addAttribute("sellerNicknames", sellerNicknames);
        model.addAttribute("buyerNicknames", buyerNicknames);

        return "account/transactions";
    }

    @PostMapping("/notifications/{id}/read")
    public String markNotificationAsRead(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login?redirect=/account/notifications";
        }

        try {
            notificationService.markAsRead(id);
        } catch (Exception e) {
            log.error("媛쒕퀎 ?뚮┝ ?쎌쓬 泥섎━ ?ㅽ뙣: notificationId={}, userId={}", id, userId, e);
            redirectAttributes.addFlashAttribute("errorMessage", "?뚮┝ 泥섎━ 以??ㅻ쪟媛 諛쒖깮?덉뒿?덈떎.");
        }
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

    @PostMapping("/notifications/delete-read")
    public String deleteReadNotifications(HttpSession session, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login?redirect=/account/notifications";
        }

        int deleted = notificationService.deleteReadByUserId(userId);
        redirectAttributes.addFlashAttribute("successMessage", "?쎌? ?뚮┝ " + deleted + "媛쒕? ??젣?덉뒿?덈떎.");
        return "redirect:/account/notifications";
    }

    @PostMapping("/notifications/{id}/delete")
    public String deleteNotification(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login?redirect=/account/notifications";
        }

        try {
            notificationService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "?뚮┝????젣?섏뿀?듬땲??");
        } catch (Exception e) {
            log.error("媛쒕퀎 ?뚮┝ ??젣 ?ㅽ뙣: notificationId={}, userId={}", id, userId, e);
            redirectAttributes.addFlashAttribute("errorMessage", "?뚮┝ ??젣 以??ㅻ쪟媛 諛쒖깮?덉뒿?덈떎.");
        }
        return "redirect:/account/notifications";
    }

    @PostMapping("/favorites/toggle")
    public ResponseEntity<Map<String, Object>> toggleFavorite(@RequestParam(name = "itemId") Long itemId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        Map<String, Object> response = new HashMap<>();

        if (userId == null) {
            response.put("success", false);
            response.put("message", "濡쒓렇?몄씠 ?꾩슂?⑸땲??");
            return ResponseEntity.status(401).body(response);
        }

        try {
            boolean isFavorite = favoriteService.toggleFavorite(userId, itemId);
            response.put("success", true);
            response.put("isFavorite", isFavorite);
            response.put("message", isFavorite ? "李?紐⑸줉??異붽??섏뿀?듬땲??" : "李?紐⑸줉?먯꽌 ?쒓굅?섏뿀?듬땲??");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error toggling favorite", e);
            response.put("success", false);
            response.put("message", "泥섎━ 以??ㅻ쪟媛 諛쒖깮?덉뒿?덈떎.");
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/items/{id}/images")
    public String updateImages(@PathVariable Long id,
                               HttpSession session,
                               @RequestParam(required = false) MultipartFile[] newImages,
                               @RequestParam(required = false) String[] removeImages,
                               RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login?redirect=/account/items/" + id + "/manage";
        }

        try {
            itemService.updateImages(id, userId, newImages, removeImages);
            redirectAttributes.addFlashAttribute("successMessage", "?ъ쭊???낅뜲?댄듃?섏뿀?듬땲??");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("Error updating images", e);
            redirectAttributes.addFlashAttribute("errorMessage", "?ъ쭊 ?낅뜲?댄듃 以??ㅻ쪟媛 諛쒖깮?덉뒿?덈떎.");
        }

        return "redirect:/account/items/" + id + "/manage";
    }

    @PostMapping("/items/{id}/images/delete")
    public ResponseEntity<Map<String, Object>> deleteSingleImage(@PathVariable Long id,
                                                                  @RequestParam(name = "imagePath") String imagePath,
                                                                  HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        Map<String, Object> response = new HashMap<>();

        if (userId == null) {
            response.put("success", false);
            response.put("message", "濡쒓렇?몄씠 ?꾩슂?⑸땲??");
            return ResponseEntity.status(401).body(response);
        }

        try {
            itemService.updateImages(id, userId, null, new String[]{imagePath});
            response.put("success", true);
            response.put("message", "?ъ쭊????젣?섏뿀?듬땲??");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (SecurityException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(403).body(response);
        } catch (Exception e) {
            log.error("Error deleting image", e);
            response.put("success", false);
            response.put("message", "?ъ쭊 ??젣 以??ㅻ쪟媛 諛쒖깮?덉뒿?덈떎.");
            return ResponseEntity.status(500).body(response);
        }
    }
}
