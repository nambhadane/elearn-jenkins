package com.elearnhub.teacher_service.service;

import com.elearnhub.teacher_service.dto.ParticipantDTO;
import com.elearnhub.teacher_service.dto.UserDTO;
import com.elearnhub.teacher_service.entity.User;
import com.elearnhub.teacher_service.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService, UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private EmailVerificationService emailVerificationService;

    @Override
    @org.springframework.transaction.annotation.Transactional(noRollbackFor = Exception.class)
    public User createUser(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists: " + user.getUsername());
        }
        
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists: " + user.getEmail());
        }
        
        // Set email verification status based on admin settings (EXEMPT ADMINS)
        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());
        boolean requiresVerification = !isAdmin && emailVerificationService.isEmailVerificationRequired();
        user.setEmailVerified(!requiresVerification);
        
        // ✅ DEBUG: Log password before saving
        System.out.println("🔐 CREATEUSER DEBUG:");
        System.out.println("🔐 Password before save: " + user.getPassword().substring(0, Math.min(30, user.getPassword().length())) + "...");
        System.out.println("🔐 Password length before save: " + user.getPassword().length());
        
        User savedUser = userRepository.save(user);
        System.out.println("✅ User saved with ID: " + savedUser.getId() + " Role: " + savedUser.getRole());
        
        // ✅ DEBUG: Log password after saving
        System.out.println("🔐 Password after save: " + savedUser.getPassword().substring(0, Math.min(30, savedUser.getPassword().length())) + "...");
        System.out.println("🔐 Password length after save: " + savedUser.getPassword().length());
        
        // Send verification email if required (not for admins)
        if (requiresVerification) {
            try {
                emailVerificationService.sendVerificationEmail(savedUser);
                System.out.println("📧 Verification email sent to: " + savedUser.getEmail());
            } catch (Exception e) {
                System.err.println("❌ Failed to send verification email: " + e.getMessage());
            }
        } else {
            // Send welcome email directly if verification not required
            try {
                emailVerificationService.sendWelcomeEmail(savedUser);
                System.out.println("📧 Welcome email sent to: " + savedUser.getEmail());
            } catch (Exception e) {
                System.err.println("❌ Failed to send welcome email: " + e.getMessage());
            }
        }
        
        return savedUser;
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(user -> new UserDTO(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getRole()))
                .collect(Collectors.toList());
    }

    @Override
    public User updateUser(Long id, User user) {
        Optional<User> existingUser = userRepository.findById(id);
        if (existingUser.isPresent()) {
            User updatedUser = existingUser.get();
            String originalUsername = updatedUser.getUsername();
            String originalRole = updatedUser.getRole();
            String originalPassword = updatedUser.getPassword(); // ✅ Preserve original password

            if (user.getEmail() != null) {
                updatedUser.setEmail(user.getEmail());
            }
            if (user.getName() != null) {
                updatedUser.setName(user.getName());
            }
            if (user.getPhoneNumber() != null) {
                updatedUser.setPhoneNumber(user.getPhoneNumber());
            }
            if (user.getAddress() != null) {
                updatedUser.setAddress(user.getAddress());
            }
            if (user.getProfilePicture() != null) {
                updatedUser.setProfilePicture(user.getProfilePicture());
            }
            // ✅ CRITICAL: Only update password if explicitly provided and not already hashed
            if (user.getPassword() != null && !user.getPassword().isEmpty() && !user.getPassword().startsWith("$2")) {
                updatedUser.setPassword(passwordEncoder.encode(user.getPassword()));
            } else {
                // ✅ Preserve original password
                updatedUser.setPassword(originalPassword);
            }

            updatedUser.setUsername(originalUsername);
            updatedUser.setRole(originalRole);
            return userRepository.save(updatedUser);
        }
        throw new RuntimeException("User not found with id: " + id);
    }

    // ✅ ADD: Simple update method for password reset
    @Override
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        String passwordPrefix = user.getPassword() != null && user.getPassword().length() > 10
                ? user.getPassword().substring(0, Math.min(30, user.getPassword().length()))
                : "null or too short";
        
        System.out.println("🔍 Authenticated user: " + user.getUsername() + " | Role: " + user.getRole());
        System.out.println("🔐 Password prefix in DB: " + passwordPrefix + "...");
        System.out.println("🔐 Password length: " + (user.getPassword() != null ? user.getPassword().length() : 0));
        System.out.println("🔐 Is BCrypt format: " + (user.getPassword() != null && user.getPassword().startsWith("$2")));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole())));
    }

    @Override
    public boolean resetPassword(String username, String newPlainPassword) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            return false;
        }

        User user = userOptional.get();
        String encodedPassword = passwordEncoder.encode(newPlainPassword);
        user.setPassword(encodedPassword);
        userRepository.save(user);

        System.out.println("✅ Password reset for user: " + username);
        System.out.println("🔐 New encoded password: " + encodedPassword.substring(0, Math.min(30, encodedPassword.length())) + "...");
        return true;
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<ParticipantDTO> getAllTeachers() {
        List<User> teachers = userRepository.findByRole("TEACHER");
        return teachers.stream()
                .map(teacher -> {
                    ParticipantDTO dto = new ParticipantDTO();
                    dto.setId(teacher.getId());
                    dto.setName(teacher.getName());
                    dto.setUsername(teacher.getUsername());
                    dto.setRole(teacher.getRole());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public List<ParticipantDTO> getAllStudents() {
        List<User> students = userRepository.findByRole("STUDENT");
        return students.stream()
                .map(student -> {
                    ParticipantDTO dto = new ParticipantDTO();
                    dto.setId(student.getId());
                    dto.setName(student.getName());
                    dto.setUsername(student.getUsername());
                    dto.setRole(student.getRole());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void save(User user) {
        // ✅ CRITICAL FIX: Preserve password when updating existing user
        if (user.getId() != null) {
            Optional<User> existingUserOpt = userRepository.findById(user.getId());
            if (existingUserOpt.isPresent()) {
                User existingUser = existingUserOpt.get();
                // ✅ Preserve the original password hash
                user.setPassword(existingUser.getPassword());
            }
        }
        
        User savedUser = userRepository.save(user);
        System.out.println("✅ User saved successfully: " + savedUser.getUsername());
    }
}
