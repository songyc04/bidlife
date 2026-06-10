package com.example.final_project.Service;

import com.example.final_project.Entity.EmailVerificationEntity;
import com.example.final_project.Repository.EmailVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class EmailService {
    
    private final JavaMailSender mailSender;
    private final EmailVerificationRepository verificationRepository;
    
    private static final int CODE_LENGTH = 6;
    private static final int EXPIRATION_MINUTES = 5;
    
    public void sendVerificationEmail(String email) {
        String code = generateVerificationCode();
        
        EmailVerificationEntity verification = new EmailVerificationEntity();
        verification.setEmail(email);
        verification.setVerificationCode(code);
        verification.setExpiresAt(LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES));
        verification.setVerified(false);
        verification.setCreatedAt(LocalDateTime.now());
        
        verificationRepository.save(verification);
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[BIDLIFE] 이메일 인증번호");
        message.setText("BIDLIFE 이메일 인증번호입니다.\n\n" +
                "인증번호: " + code + "\n\n" +
                "인증번호는 " + EXPIRATION_MINUTES + "분간 유효합니다.\n" +
                "본인이 요청하지 않은 경우 이 이메일을 무시하세요.");
        
        mailSender.send(message);
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
}
