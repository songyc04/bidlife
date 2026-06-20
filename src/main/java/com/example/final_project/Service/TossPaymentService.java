package com.example.final_project.Service;

import com.example.final_project.Config.TossConfig;
import com.example.final_project.Entity.ItemEntity;
import com.example.final_project.Repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TossPaymentService {

    private final TossConfig tossConfig;
    private final RestTemplate restTemplate;
    private final ItemRepository itemRepository;

    public String generateOrderId(Long itemId, Long userId) {
        return "ORDER-" + itemId + "-" + userId + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    public Map<String, Object> buildPaymentRequest(ItemEntity item, Long buyerId, String orderId) {
        Map<String, Object> request = new HashMap<>();
        request.put("orderId", orderId);
        request.put("orderName", item.getTitle());
        request.put("amount", item.getFinalPrice());
        request.put("customerName", "buyer-" + buyerId);
        request.put("successUrl", "http://localhost:7001/payment/success?orderId=" + orderId);
        request.put("failUrl", "http://localhost:7001/payment/fail?orderId=" + orderId);
        return request;
    }

    @Transactional
    public void saveOrderMetadata(Long itemId, String orderId) {
        itemRepository.findById(itemId).ifPresent(item -> {
            item.setOrderId(orderId);
            itemRepository.save(item);
        });
    }

    @Transactional
    public boolean confirmPaymentTestMode(Long itemId) {
        ItemEntity item = itemRepository.findById(itemId).orElse(null);
        if (item == null) {
            return false;
        }
        item.setPaymentStatus("buyer_paid");
        item.setBuyerPaidAt(LocalDateTime.now());
        item.setPaymentKey("test_payment_key_" + UUID.randomUUID().toString().substring(0, 8));
        itemRepository.save(item);
        log.info("테스트 모드 결제 확인 완료: itemId={}, orderId={}", itemId, item.getOrderId());
        return true;
    }

    public Map<String, Object> confirmPayment(String paymentKey, String orderId, int amount) {
        if (tossConfig.isTestMode()) {
            Map<String, Object> mock = new HashMap<>();
            mock.put("paymentKey", paymentKey);
            mock.put("orderId", orderId);
            mock.put("status", "DONE");
            mock.put("totalAmount", amount);
            mock.put("method", "테스트");
            return mock;
        }

        String url = tossConfig.getApiBaseUrl() + "/v1/payments/confirm";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", tossConfig.getAuthorizationHeader());

        Map<String, Object> body = new HashMap<>();
        body.put("paymentKey", paymentKey);
        body.put("orderId", orderId);
        body.put("amount", amount);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.POST, request, new ParameterizedTypeReference<Map<String, Object>>() {});
        return response.getBody();
    }
}
