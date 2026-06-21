package com.example.final_project.Service;

import com.example.final_project.Entity.DisputeEntity;
import com.example.final_project.Entity.InspectionEntity;
import com.example.final_project.Entity.ItemEntity;
import com.example.final_project.Entity.SignupEntity;
import com.example.final_project.Repository.DisputeRepository;
import com.example.final_project.Repository.InspectionRepository;
import com.example.final_project.Repository.ItemRepository;
import com.example.final_project.Repository.SignupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeService {

    public static final String STATUS_SOLD = "SOLD";
    public static final String STATUS_PAID = "PAID";
    public static final String STATUS_SELLER_SHIPPED = "SELLER_SHIPPED";
    public static final String STATUS_INSPECTION = "INSPECTION";
    public static final String STATUS_INSPECTION_PASSED = "INSPECTION_PASSED";
    public static final String STATUS_INSPECTION_FAILED = "INSPECTION_FAILED";
    public static final String STATUS_SHIPPING = "SHIPPING";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_DISPUTED = "DISPUTED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    public static final String INSPECTION_RESULT_PASS = "PASS";
    public static final String INSPECTION_RESULT_FAIL = "FAIL";

    public static final String DISPUTE_OPEN = "OPEN";
    public static final String DISPUTE_REVIEWING = "REVIEWING";
    public static final String DISPUTE_RESOLVED = "RESOLVED";

    public static final String INSPECTION_CENTER_NAME = "BIDLIFE 검수센터";
    public static final String INSPECTION_CENTER_ADDRESS = "서울특별시 강남구 테헤란로 123, BIDLIFE 검수센터 (우편번호 06234)";
    public static final String INSPECTION_CENTER_TEL = "02-1234-5678";

    private final ItemRepository itemRepository;
    private final InspectionRepository inspectionRepository;
    private final DisputeRepository disputeRepository;
    private final SignupRepository signupRepository;
    private final NotificationService notificationService;

    @Transactional
    public ItemEntity markPaid(Long itemId) {
        ItemEntity item = mustGet(itemId);
        item.setTradeStatus(STATUS_PAID);
        itemRepository.save(item);
        notificationService.createNotification(
                item.getSellerId(),
                "💳 \"" + item.getTitle() + "\" 결제가 완료되었습니다. 검수소로 발송해주세요.",
                item.getId(),
                "trade_paid"
        );
        return item;
    }

    @Transactional
    public ItemEntity sellerShipToInspection(Long itemId, Long sellerId, String carrier, String tracking) {
        ItemEntity item = mustGet(itemId);
        if (!item.getSellerId().equals(sellerId)) {
            throw new SecurityException("본인 판매 물품만 발송 처리할 수 있습니다.");
        }
        if (!STATUS_PAID.equals(item.getTradeStatus()) && !STATUS_DISPUTED.equals(item.getTradeStatus())) {
            throw new IllegalStateException("결제 완료 상태에서만 검수소 발송이 가능합니다. (현재: " + item.getTradeStatus() + ")");
        }
        if (carrier == null || carrier.isBlank() || tracking == null || tracking.isBlank()) {
            throw new IllegalArgumentException("택배사와 송장번호를 모두 입력해주세요.");
        }

        item.setTradeStatus(STATUS_SELLER_SHIPPED);
        item.setToInspectionCarrier(carrier.trim());
        item.setToInspectionTracking(tracking.trim());
        item.setToInspectionShippedAt(LocalDateTime.now());
        item.setShippingStatus("shipped_to_inspection");
        itemRepository.save(item);

        if (item.getToBuyerTracking() != null) {
            item.setTradeStatus(STATUS_SHIPPING);
            itemRepository.save(item);
        }

        notificationService.createNotification(
                item.getWinnerId(),
                "📦 판매자가 \"" + item.getTitle() + "\"을(를) 검수소로 발송했습니다. 검수 진행 상황은 곧 알려드립니다.",
                item.getId(),
                "trade_seller_shipped"
        );
        return item;
    }

    @Transactional
    public InspectionEntity startInspection(Long itemId) {
        ItemEntity item = mustGet(itemId);
        if (!STATUS_SELLER_SHIPPED.equals(item.getTradeStatus()) && !STATUS_INSPECTION_FAILED.equals(item.getTradeStatus())) {
            throw new IllegalStateException("검수 시작 가능한 상태가 아닙니다. (현재: " + item.getTradeStatus() + ")");
        }
        if (inspectionRepository.findByItemId(itemId).isPresent()) {
            throw new IllegalStateException("이미 검수가 진행 중입니다.");
        }
        item.setTradeStatus(STATUS_INSPECTION);
        itemRepository.save(item);

        InspectionEntity inspection = new InspectionEntity();
        inspection.setItemId(itemId);
        inspection.setStatus("IN_PROGRESS");
        inspection.setResult("PENDING");
        return inspectionRepository.save(inspection);
    }

    @Transactional
    public ItemEntity completeInspectionPass(Long itemId, String grade, String memo, String evidenceImages,
                                             String inspector, String toBuyerCarrier, String toBuyerTracking) {
        ItemEntity item = mustGet(itemId);
        if (!STATUS_INSPECTION.equals(item.getTradeStatus())) {
            throw new IllegalStateException("검수 중인 물품이 아닙니다. (현재: " + item.getTradeStatus() + ")");
        }
        if (toBuyerCarrier == null || toBuyerCarrier.isBlank() || toBuyerTracking == null || toBuyerTracking.isBlank()) {
            throw new IllegalArgumentException("구매자 발송을 위한 택배사와 송장번호를 입력해주세요.");
        }

        InspectionEntity inspection = inspectionRepository.findByItemId(itemId)
                .orElseThrow(() -> new IllegalStateException("검수 정보를 찾을 수 없습니다."));
        inspection.setStatus("COMPLETED");
        inspection.setResult(INSPECTION_RESULT_PASS);
        inspection.setGrade(grade);
        inspection.setMemo(memo);
        inspection.setEvidenceImages(evidenceImages);
        inspection.setInspector(inspector);
        inspection.setCompletedAt(LocalDateTime.now());
        inspectionRepository.save(inspection);

        item.setTradeStatus(STATUS_INSPECTION_PASSED);
        item.setShippingStatus("inspection_passed");
        item.setToBuyerCarrier(toBuyerCarrier.trim());
        item.setToBuyerTracking(toBuyerTracking.trim());
        item.setToBuyerShippedAt(LocalDateTime.now());
        item.setTradeStatus(STATUS_SHIPPING);
        itemRepository.save(item);

        notificationService.createNotification(
                item.getWinnerId(),
                "✅ \"" + item.getTitle() + "\" 검수가 통과되어 검수소에서 구매자님께 발송했습니다. (송장: " + toBuyerTracking + ")",
                item.getId(),
                "trade_inspection_passed"
        );
        return item;
    }

    @Transactional
    public ItemEntity completeInspectionFail(Long itemId, String failureReason, String memo, String evidenceImages, String inspector) {
        ItemEntity item = mustGet(itemId);
        if (!STATUS_INSPECTION.equals(item.getTradeStatus())) {
            throw new IllegalStateException("검수 중인 물품이 아닙니다. (현재: " + item.getTradeStatus() + ")");
        }
        if (failureReason == null || failureReason.isBlank()) {
            throw new IllegalArgumentException("검수 실패 사유를 입력해주세요.");
        }

        InspectionEntity inspection = inspectionRepository.findByItemId(itemId)
                .orElseThrow(() -> new IllegalStateException("검수 정보를 찾을 수 없습니다."));
        inspection.setStatus("COMPLETED");
        inspection.setResult(INSPECTION_RESULT_FAIL);
        inspection.setFailureReason(failureReason);
        inspection.setMemo(memo);
        inspection.setEvidenceImages(evidenceImages);
        inspection.setInspector(inspector);
        inspection.setCompletedAt(LocalDateTime.now());
        inspectionRepository.save(inspection);

        item.setTradeStatus(STATUS_INSPECTION_FAILED);
        item.setPaymentStatus("cancelled");
        item.setShippingStatus("inspection_failed");
        itemRepository.save(item);

        Optional<SignupEntity> buyerOpt = signupRepository.findById(item.getWinnerId());
        String buyerNickname = buyerOpt.map(SignupEntity::getNickname).orElse("구매자");
        notificationService.createNotification(
                item.getWinnerId(),
                "❌ \"" + item.getTitle() + "\" 검수가 실패했습니다. 사유: " + failureReason + " 결제는 자동으로 환불됩니다.",
                item.getId(),
                "trade_inspection_failed"
        );
        notificationService.createNotification(
                item.getSellerId(),
                "⚠️ \"" + item.getTitle() + "\" 검수가 실패했습니다. 사유: " + failureReason + " 물품은 판매자에게 반송됩니다.",
                item.getId(),
                "trade_inspection_failed"
        );
        return item;
    }

    @Transactional
    public ItemEntity buyerConfirmReceipt(Long itemId, Long buyerId) {
        ItemEntity item = mustGet(itemId);
        if (!item.getWinnerId().equals(buyerId)) {
            throw new SecurityException("본인 구매 물품만 수령 확인할 수 있습니다.");
        }
        if (!STATUS_SHIPPING.equals(item.getTradeStatus()) && !STATUS_INSPECTION_PASSED.equals(item.getTradeStatus())) {
            throw new IllegalStateException("배송 중인 물품만 수령 확인할 수 있습니다. (현재: " + item.getTradeStatus() + ")");
        }

        item.setTradeStatus(STATUS_COMPLETED);
        item.setPaymentStatus("completed");
        item.setBuyerReceivedAt(LocalDateTime.now());
        item.setSellerConfirmedAt(LocalDateTime.now());
        itemRepository.save(item);

        notificationService.createNotification(
                item.getSellerId(),
                "🎉 \"" + item.getTitle() + "\" 거래가 최종 완료되었습니다. 에스크로가 판매자 계정으로 정산됩니다.",
                item.getId(),
                "trade_completed"
        );
        return item;
    }

    @Transactional
    public DisputeEntity openDispute(Long itemId, Long reporterId, String reporterRole,
                                     String type, String content, String evidenceImages) {
        ItemEntity item = mustGet(itemId);
        boolean isBuyer = "BUYER".equalsIgnoreCase(reporterRole);
        boolean isSeller = "SELLER".equalsIgnoreCase(reporterRole);
        if (isBuyer && !item.getWinnerId().equals(reporterId)) {
            throw new SecurityException("본인 구매 물품에 대해서만 분쟁 신고가 가능합니다.");
        }
        if (isSeller && !item.getSellerId().equals(reporterId)) {
            throw new SecurityException("본인 판매 물품에 대해서만 분쟁 신고가 가능합니다.");
        }
        if (type == null || type.isBlank() || content == null || content.isBlank()) {
            throw new IllegalArgumentException("분쟁 유형과 내용을 입력해주세요.");
        }
        if (STATUS_COMPLETED.equals(item.getTradeStatus()) || STATUS_CANCELLED.equals(item.getTradeStatus())) {
            throw new IllegalStateException("이미 종료된 거래에는 분쟁을 신청할 수 없습니다.");
        }

        Optional<SignupEntity> reporterOpt = signupRepository.findById(reporterId);
        String reporterNickname = reporterOpt.map(SignupEntity::getNickname).orElse("사용자");

        DisputeEntity dispute = new DisputeEntity();
        dispute.setItemId(itemId);
        dispute.setReporterId(reporterId);
        dispute.setReporterRole(reporterRole.toUpperCase());
        dispute.setReporterNickname(reporterNickname);
        dispute.setType(type);
        dispute.setContent(content);
        dispute.setEvidenceImages(evidenceImages);
        dispute.setStatus(DISPUTE_OPEN);
        DisputeEntity saved = disputeRepository.save(dispute);

        item.setTradeStatus(STATUS_DISPUTED);
        itemRepository.save(item);

        notificationService.createNotification(
                item.getWinnerId(),
                "⚠️ \"" + item.getTitle() + "\" 거래에 분쟁이 접수되었습니다. 관리자 검토가 진행됩니다.",
                item.getId(),
                "trade_disputed"
        );
        notificationService.createNotification(
                item.getSellerId(),
                "⚠️ \"" + item.getTitle() + "\" 거래에 분쟁이 접수되었습니다. 관리자 검토가 진행됩니다.",
                item.getId(),
                "trade_disputed"
        );
        return saved;
    }

    @Transactional
    public DisputeEntity resolveDispute(Long disputeId, String adminResponse, boolean restoreToBuyer) {
        DisputeEntity dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 분쟁입니다."));
        if (DISPUTE_RESOLVED.equals(dispute.getStatus())) {
            throw new IllegalStateException("이미 처리된 분쟁입니다.");
        }
        ItemEntity item = mustGet(dispute.getItemId());

        dispute.setAdminResponse(adminResponse);
        dispute.setStatus(DISPUTE_RESOLVED);
        dispute.setResolvedAt(LocalDateTime.now());
        disputeRepository.save(dispute);

        if (restoreToBuyer) {
            item.setTradeStatus(STATUS_CANCELLED);
            item.setPaymentStatus("cancelled");
            item.setShippingStatus("cancelled");
        } else {
            item.setTradeStatus(STATUS_COMPLETED);
            item.setPaymentStatus("completed");
        }
        itemRepository.save(item);

        notificationService.createNotification(
                item.getWinnerId(),
                "📋 \"" + item.getTitle() + "\" 분쟁이 처리되었습니다. 결과: " + (restoreToBuyer ? "구매자 환불" : "판매자 정산"),
                item.getId(),
                "trade_dispute_resolved"
        );
        notificationService.createNotification(
                item.getSellerId(),
                "📋 \"" + item.getTitle() + "\" 분쟁이 처리되었습니다. 결과: " + (restoreToBuyer ? "구매자 환불" : "판매자 정산"),
                item.getId(),
                "trade_dispute_resolved"
        );
        return dispute;
    }

    public String getTradeStatusLabel(String status) {
        if (status == null) return "—";
        return switch (status) {
            case STATUS_SOLD -> "결제 대기";
            case STATUS_PAID -> "결제 완료";
            case STATUS_SELLER_SHIPPED -> "검수소 발송";
            case STATUS_INSPECTION -> "검수 중";
            case STATUS_INSPECTION_PASSED -> "검수 통과";
            case STATUS_INSPECTION_FAILED -> "검수 실패";
            case STATUS_SHIPPING -> "배송 중";
            case STATUS_COMPLETED -> "거래 완료";
            case STATUS_DISPUTED -> "분쟁 중";
            case STATUS_CANCELLED -> "취소됨";
            default -> status;
        };
    }

    public String getTradeStatusClass(String status) {
        if (status == null) return "ts-default";
        return switch (status) {
            case STATUS_PAID, STATUS_SELLER_SHIPPED -> "ts-progress";
            case STATUS_INSPECTION, STATUS_SHIPPING -> "ts-active";
            case STATUS_INSPECTION_PASSED -> "ts-pass";
            case STATUS_INSPECTION_FAILED, STATUS_CANCELLED -> "ts-fail";
            case STATUS_COMPLETED -> "ts-done";
            case STATUS_DISPUTED -> "ts-warn";
            default -> "ts-default";
        };
    }

    private ItemEntity mustGet(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 거래입니다."));
    }
}
