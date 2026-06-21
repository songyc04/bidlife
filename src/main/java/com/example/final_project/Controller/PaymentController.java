package com.example.final_project.Controller;

import com.example.final_project.Entity.ItemEntity;
import com.example.final_project.Entity.SignupEntity;
import com.example.final_project.Repository.ItemRepository;
import com.example.final_project.Repository.SignupRepository;
import com.example.final_project.Service.NotificationService;
import com.example.final_project.Service.TossPaymentService;
import com.example.final_project.Service.TradeService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class PaymentController {

    private final ItemRepository itemRepository;
    private final SignupRepository signupRepository;
    private final TossPaymentService tossPaymentService;
    private final NotificationService notificationService;
    private final TradeService tradeService;

    @GetMapping("/payment/{itemId}")
    public String paymentPage(@PathVariable Long itemId, HttpSession session, Model model,
                              RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login?redirect=/payment/" + itemId;
        }

        ItemEntity item = itemRepository.findById(itemId).orElse(null);
        if (item == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "존재하지 않는 경매입니다.");
            return "redirect:/items";
        }

        if (!userId.equals(item.getWinnerId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "본인 낙찰/구매 물품만 결제할 수 있습니다.");
            return "redirect:/items/" + itemId;
        }

        if ("completed".equals(item.getPaymentStatus())) {
            redirectAttributes.addFlashAttribute("successMessage", "이미 결제가 완료된 거래입니다.");
            return "redirect:/account/transactions";
        }

        if (!"pending".equals(item.getPaymentStatus())) {
            redirectAttributes.addFlashAttribute("errorMessage", "결제 가능한 상태가 아닙니다. (현재: " + item.getPaymentStatus() + ")");
            return "redirect:/items/" + itemId;
        }

        SignupEntity seller = signupRepository.findById(item.getSellerId()).orElse(null);
        model.addAttribute("item", item);
        model.addAttribute("sellerNickname", seller != null ? seller.getNickname() : "알 수 없음");
        model.addAttribute("isLoggedIn", true);
        return "payment";
    }

    @PostMapping("/payment/{itemId}/test-pay")
    public String testPay(@PathVariable Long itemId, HttpSession session,
                          RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        ItemEntity item = itemRepository.findById(itemId).orElse(null);
        if (item == null || !userId.equals(item.getWinnerId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "결제 권한이 없습니다.");
            return "redirect:/items";
        }

        if (!"pending".equals(item.getPaymentStatus())) {
            redirectAttributes.addFlashAttribute("errorMessage", "이미 처리된 결제입니다.");
            return "redirect:/account/transactions";
        }

        if (item.getOrderId() == null || item.getOrderId().isEmpty()) {
            String orderId = tossPaymentService.generateOrderId(itemId, userId);
            tossPaymentService.saveOrderMetadata(itemId, orderId);
        }

        boolean ok = tossPaymentService.confirmPaymentTestMode(itemId);
        if (!ok) {
            redirectAttributes.addFlashAttribute("errorMessage", "테스트 결제 처리에 실패했습니다.");
            return "redirect:/payment/" + itemId;
        }

        tradeService.markPaid(itemId);
        notifySellerForConfirmation(item);

        redirectAttributes.addFlashAttribute("successMessage", "테스트 결제가 완료되었습니다. 판매자의 확인을 기다립니다.");
        return "redirect:/account/transactions";
    }

    @PostMapping("/payment/{itemId}/request")
    public String requestPayment(@PathVariable Long itemId, HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        return testPay(itemId, session, redirectAttributes);
    }

    @GetMapping("/payment/success")
    public String paymentSuccess(@RequestParam(name = "orderId") String orderId,
                                 @RequestParam(name = "paymentKey") String paymentKey,
                                 @RequestParam(name = "amount") Integer amount,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            Map<String, Object> result = tossPaymentService.confirmPayment(paymentKey, orderId, amount);
            String status = (String) result.get("status");
            if (!"DONE".equals(status)) {
                redirectAttributes.addFlashAttribute("errorMessage", "결제 승인에 실패했습니다. (status: " + status + ")");
                return "redirect:/payment/fail?orderId=" + orderId;
            }

            ItemEntity item = itemRepository.findByOrderId(orderId).orElse(null);
            if (item == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "주문 정보를 찾을 수 없습니다.");
                return "redirect:/items";
            }

            item.setPaymentStatus("buyer_paid");
            item.setBuyerPaidAt(LocalDateTime.now());
            item.setPaymentKey(paymentKey);
            itemRepository.save(item);

            tradeService.markPaid(item.getId());
            notifySellerForConfirmation(item);

            redirectAttributes.addFlashAttribute("successMessage", "결제가 완료되었습니다. 판매자의 확인을 기다립니다.");
            return "redirect:/account/transactions";
        } catch (Exception e) {
            log.error("토스 결제 승인 실패: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "결제 승인 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/payment/fail?orderId=" + orderId;
        }
    }

    @GetMapping("/payment/fail")
    public String paymentFail(@RequestParam(name = "orderId") String orderId,
                              @RequestParam(name = "message", required = false) String message,
                              RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", "결제가 실패했습니다: " + (message != null ? message : "알 수 없는 오류"));
        return "redirect:/account/transactions";
    }

    private void notifySellerForConfirmation(ItemEntity item) {
        Optional<SignupEntity> buyerOpt = signupRepository.findById(item.getWinnerId());
        String buyerNickname = buyerOpt.map(SignupEntity::getNickname).orElse("구매자");

        notificationService.createNotification(
                item.getWinnerId(),
                "✅ \"" + item.getTitle() + "\" 결제가 완료되었습니다. 판매자의 확인을 기다리고 있습니다.",
                item.getId(),
                "payment_pending_seller"
        );
    }
}
