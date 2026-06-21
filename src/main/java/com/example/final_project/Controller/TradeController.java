package com.example.final_project.Controller;

import com.example.final_project.Entity.DisputeEntity;
import com.example.final_project.Entity.InspectionEntity;
import com.example.final_project.Entity.ItemEntity;
import com.example.final_project.Entity.SignupEntity;
import com.example.final_project.Repository.DisputeRepository;
import com.example.final_project.Repository.InspectionRepository;
import com.example.final_project.Repository.ItemRepository;
import com.example.final_project.Repository.SignupRepository;
import com.example.final_project.Service.TradeService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/trade")
public class TradeController {

    private final TradeService tradeService;
    private final ItemRepository itemRepository;
    private final SignupRepository signupRepository;
    private final InspectionRepository inspectionRepository;
    private final DisputeRepository disputeRepository;

    @GetMapping("/{id}/ship")
    public String shipForm(@PathVariable Long id, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login?redirect=/trade/" + id + "/ship";

        ItemEntity item = itemRepository.findById(id).orElse(null);
        if (item == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "존재하지 않는 거래입니다.");
            return "redirect:/account/transactions";
        }
        if (!item.getSellerId().equals(userId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "본인 판매 물품만 발송 처리할 수 있습니다.");
            return "redirect:/account/transactions";
        }
        if (!TradeService.STATUS_PAID.equals(item.getTradeStatus())) {
            redirectAttributes.addFlashAttribute("errorMessage", "결제 완료 후 발송 가능합니다. (현재: " + tradeService.getTradeStatusLabel(item.getTradeStatus()) + ")");
            return "redirect:/account/transactions";
        }

        addCommonAttributes(session, model);
        model.addAttribute("item", item);
        model.addAttribute("inspectionCenter", Map.of(
                "name", TradeService.INSPECTION_CENTER_NAME,
                "address", TradeService.INSPECTION_CENTER_ADDRESS,
                "tel", TradeService.INSPECTION_CENTER_TEL
        ));
        return "trade/ship";
    }

    @PostMapping("/{id}/ship")
    public String shipSubmit(@PathVariable Long id,
                             @RequestParam String carrier,
                             @RequestParam String tracking,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        try {
            tradeService.sellerShipToInspection(id, userId, carrier, tracking);
            redirectAttributes.addFlashAttribute("successMessage", "검수소로의 발송이 등록되었습니다. 검수센터 도착 후 검수가 진행됩니다.");
        } catch (Exception e) {
            log.error("검수소 발송 처리 실패: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/account/transactions";
    }

    @PostMapping("/{id}/receive")
    public String receiveSubmit(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        try {
            tradeService.buyerConfirmReceipt(id, userId);
            redirectAttributes.addFlashAttribute("successMessage", "수령 확인이 완료되었습니다. 거래가 최종 완료되었습니다.");
        } catch (Exception e) {
            log.error("수령 확인 실패: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/account/transactions";
    }

    @GetMapping("/{id}/dispute")
    public String disputeForm(@PathVariable Long id, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login?redirect=/trade/" + id + "/dispute";

        ItemEntity item = itemRepository.findById(id).orElse(null);
        if (item == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "존재하지 않는 거래입니다.");
            return "redirect:/account/transactions";
        }
        boolean isBuyer = userId.equals(item.getWinnerId());
        boolean isSeller = userId.equals(item.getSellerId());
        if (!isBuyer && !isSeller) {
            redirectAttributes.addFlashAttribute("errorMessage", "본인 거래에만 분쟁을 신청할 수 있습니다.");
            return "redirect:/account/transactions";
        }

        String role = isBuyer ? "BUYER" : "SELLER";
        List<DisputeEntity> existing = disputeRepository.findByItemIdOrderByCreatedAtDesc(id);
        addCommonAttributes(session, model);
        model.addAttribute("item", item);
        model.addAttribute("role", role);
        model.addAttribute("disputes", existing);
        return "trade/dispute";
    }

    @PostMapping("/{id}/dispute")
    public String disputeSubmit(@PathVariable Long id,
                                @RequestParam String role,
                                @RequestParam String type,
                                @RequestParam String content,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        try {
            tradeService.openDispute(id, userId, role, type, content, null);
            redirectAttributes.addFlashAttribute("successMessage", "분쟁이 접수되었습니다. 관리자가 검토 후 연락드립니다.");
        } catch (Exception e) {
            log.error("분쟁 신청 실패: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/account/transactions";
    }

    @GetMapping("/{id}/detail")
    public String tradeDetail(@PathVariable Long id, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login?redirect=/trade/" + id + "/detail";

        ItemEntity item = itemRepository.findById(id).orElse(null);
        if (item == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "존재하지 않는 거래입니다.");
            return "redirect:/account/transactions";
        }
        if (!userId.equals(item.getSellerId()) && !userId.equals(item.getWinnerId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "본인 거래만 조회할 수 있습니다.");
            return "redirect:/account/transactions";
        }

        InspectionEntity inspection = inspectionRepository.findByItemId(id).orElse(null);
        List<DisputeEntity> disputes = disputeRepository.findByItemIdOrderByCreatedAtDesc(id);

        Optional<SignupEntity> sellerOpt = signupRepository.findById(item.getSellerId());
        Optional<SignupEntity> buyerOpt = userId.equals(item.getWinnerId()) ? Optional.empty() : signupRepository.findById(item.getWinnerId());

        addCommonAttributes(session, model);
        model.addAttribute("item", item);
        model.addAttribute("inspection", inspection);
        model.addAttribute("disputes", disputes);
        model.addAttribute("seller", sellerOpt.orElse(null));
        model.addAttribute("inspectionCenter", Map.of(
                "name", TradeService.INSPECTION_CENTER_NAME,
                "address", TradeService.INSPECTION_CENTER_ADDRESS,
                "tel", TradeService.INSPECTION_CENTER_TEL
        ));
        model.addAttribute("timeline", buildTimeline(item, inspection, disputes));
        return "trade/detail";
    }

    @GetMapping("/admin/inspection")
    public String adminInspectionList(HttpSession session, Model model) {
        List<InspectionEntity> inspections = inspectionRepository.findAllByOrderByStartedAtDesc();
        List<ItemEntity> items = itemRepository.findAllByOrderByCreatedAtDesc();
        Map<Long, ItemEntity> itemMap = new HashMap<>();
        for (ItemEntity item : items) {
            itemMap.put(item.getId(), item);
        }
        Map<Long, SignupEntity> userMap = new HashMap<>();
        for (SignupEntity user : signupRepository.findAll()) {
            userMap.put(user.getId(), user);
        }

        addCommonAttributes(session, model);
        model.addAttribute("inspections", inspections);
        model.addAttribute("itemMap", itemMap);
        model.addAttribute("userMap", userMap);
        model.addAttribute("inspectionCenter", Map.of(
                "name", TradeService.INSPECTION_CENTER_NAME,
                "address", TradeService.INSPECTION_CENTER_ADDRESS,
                "tel", TradeService.INSPECTION_CENTER_TEL
        ));
        return "trade/admin-inspection";
    }

    @GetMapping("/admin/inspection/{id}")
    public String adminInspectionDetail(@PathVariable Long id, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        ItemEntity item = itemRepository.findById(id).orElse(null);
        if (item == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "존재하지 않는 거래입니다.");
            return "redirect:/trade/admin/inspection";
        }
        InspectionEntity inspection = inspectionRepository.findByItemId(id).orElse(null);
        Optional<SignupEntity> sellerOpt = signupRepository.findById(item.getSellerId());
        Optional<SignupEntity> buyerOpt = item.getWinnerId() != null ? signupRepository.findById(item.getWinnerId()) : Optional.empty();

        addCommonAttributes(session, model);
        model.addAttribute("item", item);
        model.addAttribute("inspection", inspection);
        model.addAttribute("seller", sellerOpt.orElse(null));
        model.addAttribute("buyer", buyerOpt.orElse(null));
        model.addAttribute("inspectionCenter", Map.of(
                "name", TradeService.INSPECTION_CENTER_NAME,
                "address", TradeService.INSPECTION_CENTER_ADDRESS,
                "tel", TradeService.INSPECTION_CENTER_TEL
        ));
        return "trade/admin-inspection-detail";
    }

    @PostMapping("/admin/inspection/{id}/start")
    public String adminInspectionStart(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            tradeService.startInspection(id);
            redirectAttributes.addFlashAttribute("successMessage", "검수가 시작되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/trade/admin/inspection/" + id;
    }

    @PostMapping("/admin/inspection/{id}/pass")
    public String adminInspectionPass(@PathVariable Long id,
                                      @RequestParam(required = false) String grade,
                                      @RequestParam(required = false) String memo,
                                      @RequestParam(required = false) String evidenceImages,
                                      @RequestParam String inspector,
                                      @RequestParam String toBuyerCarrier,
                                      @RequestParam String toBuyerTracking,
                                      RedirectAttributes redirectAttributes) {
        try {
            tradeService.completeInspectionPass(id, grade, memo, evidenceImages, inspector, toBuyerCarrier, toBuyerTracking);
            redirectAttributes.addFlashAttribute("successMessage", "검수 통과 처리되었습니다. 구매자에게 발송되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/trade/admin/inspection/" + id;
    }

    @PostMapping("/admin/inspection/{id}/fail")
    public String adminInspectionFail(@PathVariable Long id,
                                      @RequestParam String failureReason,
                                      @RequestParam(required = false) String memo,
                                      @RequestParam(required = false) String evidenceImages,
                                      @RequestParam String inspector,
                                      RedirectAttributes redirectAttributes) {
        try {
            tradeService.completeInspectionFail(id, failureReason, memo, evidenceImages, inspector);
            redirectAttributes.addFlashAttribute("successMessage", "검수 실패로 처리되었습니다. 결제 취소 및 판매자 반송 안내가 전송됩니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/trade/admin/inspection/" + id;
    }

    @GetMapping("/admin/disputes")
    public String adminDisputes(HttpSession session, Model model) {
        List<DisputeEntity> disputes = disputeRepository.findAllByOrderByCreatedAtDesc();
        List<ItemEntity> items = itemRepository.findAllByOrderByCreatedAtDesc();
        Map<Long, ItemEntity> itemMap = new HashMap<>();
        for (ItemEntity item : items) {
            itemMap.put(item.getId(), item);
        }
        addCommonAttributes(session, model);
        model.addAttribute("disputes", disputes);
        model.addAttribute("itemMap", itemMap);
        return "trade/admin-disputes";
    }

    @PostMapping("/admin/disputes/{id}/resolve")
    public String adminDisputeResolve(@PathVariable Long id,
                                      @RequestParam String adminResponse,
                                      @RequestParam String decision,
                                      RedirectAttributes redirectAttributes) {
        try {
            boolean restoreToBuyer = "REFUND_BUYER".equalsIgnoreCase(decision);
            tradeService.resolveDispute(id, adminResponse, restoreToBuyer);
            redirectAttributes.addFlashAttribute("successMessage", "분쟁이 처리되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/trade/admin/disputes";
    }

    private List<Map<String, Object>> buildTimeline(ItemEntity item, InspectionEntity inspection, List<DisputeEntity> disputes) {
        List<Map<String, Object>> events = new ArrayList<>();
        if (item.getTransactionDate() != null) {
            events.add(event("낙찰 확정", item.getTransactionDate(), "auction_sold", "낙찰자가 결정되어 거래가 시작되었습니다."));
        }
        if (item.getBuyerPaidAt() != null) {
            events.add(event("결제 완료 (에스크로 보관)", item.getBuyerPaidAt(), "escrow_held", "구매자가 결제를 완료했습니다."));
        }
        if (item.getToInspectionShippedAt() != null) {
            events.add(event("검수소로 발송", item.getToInspectionShippedAt(), "shipped_to_inspection",
                    "택배: " + (item.getToInspectionCarrier() != null ? item.getToInspectionCarrier() : "-") +
                            " / 송장: " + (item.getToInspectionTracking() != null ? item.getToInspectionTracking() : "-")));
        }
        if (inspection != null) {
            events.add(event("검수 시작", inspection.getStartedAt(), "inspection_start", "검수센터에서 검수가 시작되었습니다."));
            if (inspection.getCompletedAt() != null) {
                String result = TradeService.INSPECTION_RESULT_PASS.equals(inspection.getResult()) ? "검수 통과" : "검수 실패";
                events.add(event(result, inspection.getCompletedAt(),
                        TradeService.INSPECTION_RESULT_PASS.equals(inspection.getResult()) ? "inspection_pass" : "inspection_fail",
                        TradeService.INSPECTION_RESULT_PASS.equals(inspection.getResult())
                                ? "검수 통과" + (inspection.getGrade() != null ? " (등급: " + inspection.getGrade() + ")" : "")
                                : "실패 사유: " + (inspection.getFailureReason() != null ? inspection.getFailureReason() : "-")));
            }
        }
        if (item.getToBuyerShippedAt() != null) {
            events.add(event("구매자에게 발송", item.getToBuyerShippedAt(), "shipped_to_buyer",
                    "택배: " + (item.getToBuyerCarrier() != null ? item.getToBuyerCarrier() : "-") +
                            " / 송장: " + (item.getToBuyerTracking() != null ? item.getToBuyerTracking() : "-")));
        }
        if (item.getBuyerReceivedAt() != null) {
            events.add(event("거래 완료", item.getBuyerReceivedAt(), "trade_completed", "구매자가 수령을 확인했습니다. 에스크로가 판매자에게 정산됩니다."));
        }
        for (DisputeEntity d : disputes) {
            events.add(event("분쟁 " + ("RESOLVED".equals(d.getStatus()) ? "처리 완료" : "접수"), d.getCreatedAt(),
                    "dispute", "신청자: " + (d.getReporterNickname() != null ? d.getReporterNickname() : "-") + " (" + d.getReporterRole() + ")"));
        }
        events.sort((a, b) -> ((LocalDateTime) b.get("at")).compareTo((LocalDateTime) a.get("at")));
        return events;
    }

    private Map<String, Object> event(String title, LocalDateTime at, String type, String desc) {
        Map<String, Object> e = new LinkedHashMap<>();
        e.put("title", title);
        e.put("at", at);
        e.put("type", type);
        e.put("desc", desc);
        return e;
    }

    private void addCommonAttributes(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId != null) {
            model.addAttribute("isLoggedIn", true);
            String nickname = (String) session.getAttribute("nickname");
            model.addAttribute("nickname", nickname != null ? nickname : "");
        } else {
            model.addAttribute("isLoggedIn", false);
        }
    }
}
