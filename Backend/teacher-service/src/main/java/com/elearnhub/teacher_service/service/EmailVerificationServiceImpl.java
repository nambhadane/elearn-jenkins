package com.elearnhub.teacher_service.service;

import com.elearnhub.teacher_service.entity.User;
import com.elearnhub.teacher_service.entity.EmailVerificationToken;
import com.elearnhub.teacher_service.entity.SystemSettings;
import com.elearnhub.teacher_service.repository.UserRepository;
import com.elearnhub.teacher_service.repository.EmailVerificationTokenRepository;
import com.elearnhub.teacher_service.repository.SystemSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class EmailVerificationServiceImpl implements EmailVerificationService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmailVerificationTokenRepository tokenRepository;
    
    @Autowired
    private SystemSettingsRepository systemSettingsRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Override
    public String generateVerificationToken(User user) {
        // Invalidate any existing tokens for this user
        invalidateUserTokens(user.getId());
        
        // Generate new token
        String token = UUID.randomUUID().toString();
        
        // Create and save token entity
        EmailVerificationToken verificationToken = new EmailVerificationToken(token, user.getEmail(), user.getId());
        tokenRepository.save(verificationToken);
        
        return token;
    }
    
    @Override
    public void sendVerificationEmail(User user) {
        if (!isEmailVerificationRequired()) {
            // If verification is not required, mark as verified immediately
            markEmailAsVerified(user.getId());
            return;
        }
        
        String token = generateVerificationToken(user);
        emailService.sendVerificationEmail(user, token);
    }
    
    @Override
    public void resendVerificationEmail(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (!isEmailVerified(user.getId())) {
                sendVerificationEmail(user);
            } else {
                throw new RuntimeException("Email is already verified");
            }
        } else {
            throw new RuntimeException("User not found with email: " + email);
        }
    }
    
    @Override
    public boolean verifyEmail(String token) {
        Optional<EmailVerificationToken> tokenOpt = tokenRepository.findByToken(token);
        
        if (tokenOpt.isPresent()) {
            EmailVerificationToken verificationToken = tokenOpt.get();
            
            if (verificationToken.isValid()) {
                // Mark token as used
                verificationToken.markAsUsed();
                tokenRepository.save(verificationToken);
                
                // Mark user email as verified
                markEmailAsVerified(verificationToken.getUserId());
                
                // Send welcome email
                Optional<User> userOpt = userRepository.findById(verificationToken.getUserId());
                if (userOpt.isPresent()) {
                    emailService.sendWelcomeEmail(userOpt.get());
                }
                
                return true;
            } else {
                throw new RuntimeException("Token is expired or already used");
            }
        } else {
            throw new RuntimeException("Invalid verification token");
        }
    }
    
    @Override
    public boolean isEmailVerified(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        return userOpt.map(User::getEmailVerified).orElse(false);
    }
    
    @Override
    public boolean isEmailVerified(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        return userOpt.map(User::getEmailVerified).orElse(false);
    }
    
    @Override
    public boolean isTokenValid(String token) {
        Optional<EmailVerificationToken> tokenOpt = tokenRepository.findByToken(token);
        return tokenOpt.map(EmailVerificationToken::isValid).orElse(false);
    }
    
    @Override
    public void invalidateUserTokens(Long userId) {
        List<EmailVerificationToken> tokens = tokenRepository.findByUserId(userId);
        for (EmailVerificationToken token : tokens) {
            if (!token.getUsed()) {
                token.markAsUsed();
                tokenRepository.save(token);
            }
        }
    }
    
    @Override
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        
        // Delete expired tokens
        tokenRepository.deleteExpiredTokens(now);
        
        // Delete used tokens older than 30 days
        LocalDateTime cutoffDate = now.minusDays(30);
        tokenRepository.deleteOldUsedTokens(cutoffDate);
    }
    
    @Override
    public void markEmailAsVerified(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setEmailVerified(true);
            user.setEmailVerifiedAt(LocalDateTime.now());
            userRepository.save(user);
        }
    }
    
    @Override
    public void markEmailAsUnverified(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setEmailVerified(false);
            user.setEmailVerifiedAt(null);
            userRepository.save(user);
        }
    }
    
    @Override
    public void forceVerifyUser(Long userId) {
        markEmailAsVerified(userId);
        invalidateUserTokens(userId);
    }
    
    @Override
    public void sendVerificationReminderEmail(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (!isEmailVerified(userId)) {
                // Check if user already has a valid token
                LocalDateTime now = LocalDateTime.now();
                Optional<EmailVerificationToken> existingToken = tokenRepository.findValidTokenByUserId(userId, now);
                
                if (existingToken.isPresent()) {
                    // Resend email with existing token
                    emailService.sendVerificationEmail(user, existingToken.get().getToken());
                } else {
                    // Generate new token and send
                    sendVerificationEmail(user);
                }
            }
        }
    }
    
    @Override
    @org.springframework.transaction.annotation.Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public boolean isEmailVerificationRequired() {
        try {
            Optional<SystemSettings> settingsOpt = systemSettingsRepository.findCurrentSettings();
            return settingsOpt.map(SystemSettings::getRequireEmailVerification).orElse(false);
        } catch (Exception e) {
            System.err.println("⚠️ Could not read system settings (table may not exist yet): " + e.getMessage());
            return false; // Default to not required if settings can't be retrieved
        }
    }
    
    public void sendWelcomeEmail(User user) {
        emailService.sendWelcomeEmail(user);
    }
    
    @Override
    public void updateVerificationRequirement(boolean required) {
        Optional<SystemSettings> settingsOpt = systemSettingsRepository.findCurrentSettings();
        SystemSettings settings;
        
        if (settingsOpt.isPresent()) {
            settings = settingsOpt.get();
        } else {
            settings = new SystemSettings();
        }
        
        settings.setRequireEmailVerification(required);
        systemSettingsRepository.save(settings);
    }
}