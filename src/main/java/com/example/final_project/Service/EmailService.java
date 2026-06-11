package com.example.final_project.Service;

import com.example.final_project.Entity.EmailVerificationEntity;
import com.example.final_project.Repository.EmailVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class EmailService {
    
    private final EmailVerificationRepository verificationRepository;
    
    private static final int CODE_LENGTH = 6;
    private static final int EXPIRATION_MINUTES = 5;
    
    public void sendVerificationEmail(String email) {
        String code = generateVerificationCode();
        
        EmailVerificationEntity verification = new EmailVerificationEntity();
        verification.setEmail(email);
        verification.setVerificationCode(code);
        verification.setExpiresAt(LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES));
        verification.setVerified(true);
        verification.setCreatedAt(LocalDateTime.now());
        
        verificationRepository.save(verification);
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
