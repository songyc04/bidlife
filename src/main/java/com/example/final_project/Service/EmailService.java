package com.example.final_project.Service;

import com.example.final_project.Entity.EmailVerificationEntity;
import com.example.final_project.Repository.EmailVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class EmailService {
    
    private final EmailVerificationRepository verificationRepository;
    private final JavaMailSender mailSender;
    
    private static final int CODE_LENGTH = 5;
    private static final int EXPIRATION_MINUTES = 5;
    private static final List<String> ALLOWED_DOMAINS = Arrays.asList("gmail.com", "naver.com", "test.com");
    
    public String sendVerificationEmail(String email) {
        // 도메인 검증
        String domain = email.substring(email.indexOf("@") + 1);
        if (!ALLOWED_DOMAINS.contains(domain)) {
            throw new IllegalArgumentException("gmail.com, naver.com, test.com 이메일만 가입 가능합니다.");
        }
        
        String code = generateVerificationCode();
        
        EmailVerificationEntity verification = new EmailVerificationEntity();
        verification.setEmail(email);
        verification.setVerificationCode(code);
        verification.setExpiresAt(LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES));
        verification.setVerified(false);
        verification.setCreatedAt(LocalDateTime.now());
        
        verificationRepository.save(verification);
        
        // test.com인 경우 테스트 모드로 처리
        if (domain.equals("test.com")) {
            System.out.println("========================================");
            System.out.println("[테스트 모드] 인증번호: " + code);
            System.out.println("========================================");
            return code; // 테스트용 인증번호 반환
        }
        
        // 실제 이메일 발송
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("[BIDLIFE] 이메일 인증번호");
            message.setText("안녕하세요, BIDLIFE입니다.\n\n" +
                          "이메일 인증을 위한 인증번호는 다음과 같습니다:\n\n" +
                          "인증번호: " + code + "\n\n" +
                          "인증번호는 5분간 유효합니다.\n" +
                          "감사합니다.");
            
            mailSender.send(message);
            return null; // 실제 메일 발송 시에는 인증번호 반환하지 않음
        } catch (Exception e) {
            throw new RuntimeException("이메일 발송에 실패했습니다: " + e.getMessage());
        }
    }
    
    public boolean verifyCode(String email, String code) {
        Optional<EmailVerificationEntity> verificationOpt = 
                verificationRepository.findTopByEmailOrderByCreatedAtDesc(email);
        
        if (verificationOpt.isEmpty()) {
            return false;
        }
        
        EmailVerificationEntity verification = verificationOpt.get();
        
        if (verification.isVerified()) {
            return true;
        }
        
        if (LocalDateTime.now().isAfter(verification.getExpiresAt())) {
            return false;
        }
        
        if (verification.getVerificationCode().equals(code)) {
            verification.setVerified(true);
            verificationRepository.save(verification);
            return true;
        }
        
        return false;
    }
    
    public boolean isEmailVerified(String email) {
        Optional<EmailVerificationEntity> verificationOpt = 
                verificationRepository.findTopByEmailOrderByCreatedAtDesc(email);
        
        return verificationOpt.isPresent() && verificationOpt.get().isVerified();
    }
    
    private String generateVerificationCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        
        return code.toString();
    }
    
    public String sendTemporaryPassword(String email) {
        // 도메인 검증
        String domain = email.substring(email.indexOf("@") + 1);
        if (!ALLOWED_DOMAINS.contains(domain)) {
            throw new IllegalArgumentException("gmail.com, naver.com, test.com 이메일만 사용 가능합니다.");
        }
        
        // 임시 비밀번호 생성 (8자리: 영문 대소문자 + 숫자)
        String tempPassword = generateTempPassword();
        
        // test.com인 경우 테스트 모드로 처리
        if (domain.equals("test.com")) {
            System.out.println("========================================");
            System.out.println("[테스트 모드] 임시 비밀번호: " + tempPassword);
            System.out.println("========================================");
            return tempPassword;
        }
        
        // 실제 이메일 발송
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("[BIDLIFE] 임시 비밀번호 안내");
            message.setText("안녕하세요, BIDLIFE입니다.\n\n" +
                          "요청하신 임시 비밀번호는 다음과 같습니다:\n\n" +
                          "임시 비밀번호: " + tempPassword + "\n\n" +
                          "로그인 후 반드시 비밀번호를 변경해주세요.\n" +
                          "감사합니다.");
            
            mailSender.send(message);
            return tempPassword;
        } catch (Exception e) {
            throw new RuntimeException("이메일 발송에 실패했습니다: " + e.getMessage());
        }
    }
    
    private String generateTempPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder password = new StringBuilder();
        
        for (int i = 0; i < 8; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return password.toString();
    }
}
