package com.elearnhub.teacher_service.service;

import com.elearnhub.teacher_service.entity.User;
import com.elearnhub.teacher_service.entity.SystemSettings;
import com.elearnhub.teacher_service.repository.SystemSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Primary
public class EmailServiceImpl implements EmailService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private SystemSettingsRepository systemSettingsRepository;
    
    @Value("${spring.mail.username:noreply@eduflow.com}")
    private String fromEmail;
    
    @Value("${app.base-url:http://localhost:5173}")
    private String baseUrl;
    
    // User Registration & Verification
    @Override
    public void sendVerificationEmail(User user, String verificationToken) {
        if (!isEmailServiceEnabled()) return;
        
        String subject = "Verify Your Email - " + getPlatformName();
        String verificationUrl = baseUrl + "/verify-email?token=" + verificationToken;
        
        String content = generateEmailTemplate("verification", Map.of(
            "userName", user.getName(),
            "verificationUrl", verificationUrl,
            "platformName", getPlatformName()
        ));
        
        sendHtmlEmail(user.getEmail(), subject, content);
    }
    
    @Override
    public void sendWelcomeEmail(User user) {
        if (!isEmailServiceEnabled()) return;
        
        String subject = "Welcome to " + getPlatformName() + "!";
        String content = generateEmailTemplate("welcome", Map.of(
            "userName", user.getName(),
            "userRole", user.getRole(),
            "platformName", getPlatformName(),
            "loginUrl", baseUrl + "/login",
            "supportEmail", getSupportEmail()
        ));
        
        sendHtmlEmail(user.getEmail(), subject, content);
    }
    
    @Override
    public void sendPasswordResetEmail(User user, String resetToken) {
        if (!isEmailServiceEnabled()) return;
        
        String subject = "Password Reset Request - " + getPlatformName();
        String resetUrl = baseUrl + "/reset-password?token=" + resetToken;
        
        String content = generateEmailTemplate("password-reset", Map.of(
            "userName", user.getName(),
            "resetUrl", resetUrl,
            "platformName", getPlatformName()
        ));
        
        sendHtmlEmail(user.getEmail(), subject, content);
    }
    
    // Academic Notifications
    @Override
    public void sendAssignmentNotification(User student, String assignmentTitle, String courseName, String dueDate) {
        if (!isEmailServiceEnabled()) return;
        
        String subject = "New Assignment: " + assignmentTitle;
        String content = generateEmailTemplate("assignment-notification", Map.of(
            "studentName", student.getName(),
            "assignmentTitle", assignmentTitle,
            "courseName", courseName,
            "dueDate", dueDate,
            "platformName", getPlatformName(),
            "loginUrl", baseUrl + "/login"
        ));
        
        sendHtmlEmail(student.getEmail(), subject, content);
    }
    
    @Override
    public void sendQuizNotification(User student, String quizTitle, String courseName, String dueDate) {
        if (!isEmailServiceEnabled()) return;
        
        String subject = "New Quiz Available: " + quizTitle;
        String content = generateEmailTemplate("quiz-notification", Map.of(
            "studentName", student.getName(),
            "quizTitle", quizTitle,
            "courseName", courseName,
            "dueDate", dueDate,
            "platformName", getPlatformName(),
            "loginUrl", baseUrl + "/login"
        ));
        
        sendHtmlEmail(student.getEmail(), subject, content);
    }
    
    @Override
    public void sendGradeNotification(User student, String itemTitle, String grade, String courseName) {
        if (!isEmailServiceEnabled()) return;
        
        String subject = "Grade Posted: " + itemTitle;
        String content = generateEmailTemplate("grade-notification", Map.of(
            "studentName", student.getName(),
            "itemTitle", itemTitle,
            "grade", grade,
            "courseName", courseName,
            "platformName", getPlatformName(),
            "loginUrl", baseUrl + "/login"
        ));
        
        sendHtmlEmail(student.getEmail(), subject, content);
    }
    
    @Override
    public void sendSubmissionConfirmation(User student, String itemTitle, String courseName) {
        if (!isEmailServiceEnabled()) return;
        
        String subject = "Submission Confirmed: " + itemTitle;
        String content = generateEmailTemplate("submission-confirmation", Map.of(
            "studentName", student.getName(),
            "itemTitle", itemTitle,
            "courseName", courseName,
            "platformName", getPlatformName()
        ));
        
        sendHtmlEmail(student.getEmail(), subject, content);
    }
    
    // Class & Course Notifications
    @Override
    public void sendClassEnrollmentNotification(User student, String className, String teacherName) {
        if (!isEmailServiceEnabled()) return;
        
        String subject = "Enrolled in Class: " + className;
        String content = generateEmailTemplate("class-enrollment", Map.of(
            "studentName", student.getName(),
            "className", className,
            "teacherName", teacherName,
            "platformName", getPlatformName(),
            "loginUrl", baseUrl + "/login"
        ));
        
        sendHtmlEmail(student.getEmail(), subject, content);
    }
    
    @Override
    public void sendLiveClassNotification(User student, String className, String startTime, String joinUrl) {
        if (!isEmailServiceEnabled()) return;
        
        String subject = "Live Class Starting Soon: " + className;
        String content = generateEmailTemplate("live-class-notification", Map.of(
            "studentName", student.getName(),
            "className", className,
            "startTime", startTime,
            "joinUrl", joinUrl,
            "platformName", getPlatformName()
        ));
        
        sendHtmlEmail(student.getEmail(), subject, content);
    }
    
    @Override
    public void sendClassCancellationNotification(User student, String className, String reason) {
        if (!isEmailServiceEnabled()) return;
        
        String subject = "Class Cancelled: " + className;
        String content = generateEmailTemplate("class-cancellation", Map.of(
            "studentName", student.getName(),
            "className", className,
            "reason", reason,
            "platformName", getPlatformName()
        ));
        
        sendHtmlEmail(student.getEmail(), subject, content);
    }
    
    // Administrative Notifications
    @Override
    public void sendAccountStatusNotification(User user, String status, String reason) {
        if (!isEmailServiceEnabled()) return;
        
        String subject = "Account Status Update - " + getPlatformName();
        String content = generateEmailTemplate("account-status", Map.of(
            "userName", user.getName(),
            "status", status,
            "reason", reason,
            "platformName", getPlatformName(),
            "supportEmail", getSupportEmail()
        ));
        
        sendHtmlEmail(user.getEmail(), subject, content);
    }
    
    @Override
    public void sendSystemMaintenanceNotification(User user, String maintenanceDate, String duration) {
        if (!isEmailServiceEnabled()) return;
        
        String subject = "Scheduled Maintenance - " + getPlatformName();
        String content = generateEmailTemplate("maintenance-notification", Map.of(
            "userName", user.getName(),
            "maintenanceDate", maintenanceDate,
            "duration", duration,
            "platformName", getPlatformName()
        ));
        
        sendHtmlEmail(user.getEmail(), subject, content);
    }
    
    @Override
    public void sendPlatformUpdateNotification(User user, String updateDetails) {
        if (!isEmailServiceEnabled()) return;
        
        String subject = "Platform Update - " + getPlatformName();
        String content = generateEmailTemplate("platform-update", Map.of(
            "userName", user.getName(),
            "updateDetails", updateDetails,
            "platformName", getPlatformName(),
            "loginUrl", baseUrl + "/login"
        ));
        
        sendHtmlEmail(user.getEmail(), subject, content);
    }
    
    // Teacher Notifications
    @Override
    public void sendNewStudentEnrollmentNotification(User teacher, String studentName, String className) {
        if (!isEmailServiceEnabled()) return;
        
        String subject = "New Student Enrollment: " + className;
        String content = generateEmailTemplate("new-student-enrollment", Map.of(
            "teacherName", teacher.getName(),
            "studentName", studentName,
            "className", className,
            "platformName", getPlatformName(),
            "loginUrl", baseUrl + "/login"
        ));
        
        sendHtmlEmail(teacher.getEmail(), subject, content);
    }
    
    @Override
    public void sendSubmissionReceivedNotification(User teacher, String studentName, String assignmentTitle) {
        if (!isEmailServiceEnabled()) return;
        
        String subject = "New Submission: " + assignmentTitle;
        String content = generateEmailTemplate("submission-received", Map.of(
            "teacherName", teacher.getName(),
            "studentName", studentName,
            "assignmentTitle", assignmentTitle,
            "platformName", getPlatformName(),
            "loginUrl", baseUrl + "/login"
        ));
        
        sendHtmlEmail(teacher.getEmail(), subject, content);
    }
    
    @Override
    public void sendQuizCompletedNotification(User teacher, String studentName, String quizTitle, String score) {
        if (!isEmailServiceEnabled()) return;
        
        String subject = "Quiz Completed: " + quizTitle;
        String content = generateEmailTemplate("quiz-completed", Map.of(
            "teacherName", teacher.getName(),
            "studentName", studentName,
            "quizTitle", quizTitle,
            "score", score,
            "platformName", getPlatformName(),
            "loginUrl", baseUrl + "/login"
        ));
        
        sendHtmlEmail(teacher.getEmail(), subject, content);
    }
    
    // Admin Notifications
    @Override
    public void sendNewUserRegistrationNotification(String adminEmail, User newUser) {
        if (!isEmailServiceEnabled()) return;
        
        String subject = "New User Registration - " + getPlatformName();
        String content = generateEmailTemplate("new-user-registration", Map.of(
            "userName", newUser.getName(),
            "userEmail", newUser.getEmail(),
            "userRole", newUser.getRole(),
            "platformName", getPlatformName(),
            "adminUrl", baseUrl + "/admin/users"
        ));
        
        sendHtmlEmail(adminEmail, subject, content);
    }
    
    @Override
    public void sendSystemErrorNotification(String adminEmail, String errorDetails) {
        if (!isEmailServiceEnabled()) return;
        
        String subject = "System Error Alert - " + getPlatformName();
        String content = generateEmailTemplate("system-error", Map.of(
            "errorDetails", errorDetails,
            "platformName", getPlatformName(),
            "timestamp", java.time.LocalDateTime.now().toString()
        ));
        
        sendHtmlEmail(adminEmail, subject, content);
    }
    
    @Override
    public void sendDailyReportEmail(String adminEmail, String reportData) {
        if (!isEmailServiceEnabled()) return;
        
        String subject = "Daily Report - " + getPlatformName();
        String content = generateEmailTemplate("daily-report", Map.of(
            "reportData", reportData,
            "platformName", getPlatformName(),
            "date", java.time.LocalDate.now().toString()
        ));
        
        sendHtmlEmail(adminEmail, subject, content);
    }
    
    // Generic Email Methods
    @Override
    public void sendCustomEmail(String to, String subject, String content) {
        if (!isEmailServiceEnabled()) return;
        sendHtmlEmail(to, subject, content);
    }
    
    @Override
    public void sendBulkEmail(List<String> recipients, String subject, String content) {
        if (!isEmailServiceEnabled()) return;
        
        for (String recipient : recipients) {
            try {
                sendHtmlEmail(recipient, subject, content);
                // Add small delay to avoid overwhelming email server
                Thread.sleep(100);
            } catch (Exception e) {
                System.err.println("Failed to send email to: " + recipient + " - " + e.getMessage());
            }
        }
    }
    
    // Email Template Generation
    @Override
    public String generateEmailTemplate(String templateName, Map<String, String> variables) {
        String template = getTemplateForType(templateName, variables);
        
        // Replace all variables in template
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            template = template.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        
        // Replace platform name and support email
        template = template.replace("{{platformName}}", getPlatformName());
        template = template.replace("{{supportEmail}}", getSupportEmail());
        
        // Clean up any remaining placeholders
        template = template.replaceAll("\\{\\{[^}]+\\}\\}", "");
        
        return template;
    }
    
    // Utility Methods
    @Override
    @org.springframework.transaction.annotation.Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public boolean isEmailServiceEnabled() {
        try {
            Optional<SystemSettings> settingsOpt = systemSettingsRepository.findCurrentSettings();
            return settingsOpt.map(SystemSettings::getEmailNotificationsEnabled).orElse(true);
        } catch (Exception e) {
            return true; // Default to enabled if settings can't be retrieved
        }
    }
    
    @Override
    public void testEmailConfiguration() {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(getSupportEmail());
            helper.setSubject("Email Configuration Test - " + getPlatformName());
            helper.setText("This is a test email to verify email configuration is working properly.", false);
            helper.setFrom(fromEmail);
            
            mailSender.send(message);
            System.out.println("✅ Test email sent successfully to: " + getSupportEmail());
        } catch (Exception e) {
            System.err.println("❌ Email configuration test failed: " + e.getMessage());
            throw new RuntimeException("Email service not properly configured", e);
        }
    }
    
    // Private Helper Methods
    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);
            
            mailSender.send(message);
            System.out.println("✅ Email sent successfully to: " + to);
        } catch (MessagingException e) {
            System.err.println("❌ Failed to send email to: " + to + " - " + e.getMessage());
            // Fallback to console output if email fails
            System.out.println("📧 EMAIL FALLBACK: " + subject);
            System.out.println("To: " + to);
            System.out.println("Content: " + htmlContent);
            System.out.println("---");
        }
    }
    
    @org.springframework.transaction.annotation.Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    private String getPlatformName() {
        try {
            Optional<SystemSettings> settingsOpt = systemSettingsRepository.findCurrentSettings();
            return settingsOpt.map(SystemSettings::getPlatformName).orElse("EduFlow Hub");
        } catch (Exception e) {
            return "EduFlow Hub";
        }
    }

    @org.springframework.transaction.annotation.Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    private String getSupportEmail() {
        try {
            Optional<SystemSettings> settingsOpt = systemSettingsRepository.findCurrentSettings();
            return settingsOpt.map(SystemSettings::getSupportEmail).orElse("support@eduflow.com");
        } catch (Exception e) {
            return "support@eduflow.com";
        }
    }
    
    private String getTemplateForType(String templateName, Map<String, String> variables) {
        String baseTemplate = getBaseEmailTemplate();
        String content = "";
        String actionButton = "";
        
        switch (templateName) {
            case "verification":
                content = "Welcome to {{platformName}}! Please verify your email address to complete your registration.";
                if (variables.containsKey("verificationUrl") && variables.get("verificationUrl") != null) {
                    actionButton = "<p><a href=\"" + variables.get("verificationUrl") + "\" class=\"button\">Verify Email</a></p>";
                }
                break;
                
            case "welcome":
                content = "Welcome to {{platformName}}! Your account has been successfully created. You can now access all the features available to " + variables.getOrDefault("userRole", "users") + ".";
                if (variables.containsKey("loginUrl") && variables.get("loginUrl") != null) {
                    actionButton = "<p><a href=\"" + variables.get("loginUrl") + "\" class=\"button\">Login Now</a></p>";
                }
                break;
                
            case "password-reset":
                content = "You requested a password reset for your {{platformName}} account. Click the button below to reset your password. This link will expire in 24 hours.";
                if (variables.containsKey("resetUrl") && variables.get("resetUrl") != null) {
                    actionButton = "<p><a href=\"" + variables.get("resetUrl") + "\" class=\"button\">Reset Password</a></p>";
                }
                break;
                
            case "assignment-notification":
                content = "A new assignment \"" + variables.getOrDefault("assignmentTitle", "Assignment") + "\" has been posted in " + variables.getOrDefault("courseName", "your course") + ". Due date: " + variables.getOrDefault("dueDate", "TBD");
                if (variables.containsKey("loginUrl") && variables.get("loginUrl") != null) {
                    actionButton = "<p><a href=\"" + variables.get("loginUrl") + "\" class=\"button\">View Assignment</a></p>";
                }
                break;
                
            case "quiz-notification":
                content = "A new quiz \"" + variables.getOrDefault("quizTitle", "Quiz") + "\" is available in " + variables.getOrDefault("courseName", "your course") + ". Due date: " + variables.getOrDefault("dueDate", "TBD");
                if (variables.containsKey("loginUrl") && variables.get("loginUrl") != null) {
                    actionButton = "<p><a href=\"" + variables.get("loginUrl") + "\" class=\"button\">Take Quiz</a></p>";
                }
                break;
                
            case "grade-notification":
                content = "Your grade for \"" + variables.getOrDefault("itemTitle", "Assignment") + "\" in " + variables.getOrDefault("courseName", "your course") + " has been posted. Grade: " + variables.getOrDefault("grade", "N/A");
                if (variables.containsKey("loginUrl") && variables.get("loginUrl") != null) {
                    actionButton = "<p><a href=\"" + variables.get("loginUrl") + "\" class=\"button\">View Grade</a></p>";
                }
                break;
                
            case "submission-confirmation":
                content = "Your submission for \"" + variables.getOrDefault("itemTitle", "Assignment") + "\" in " + variables.getOrDefault("courseName", "your course") + " has been received successfully.";
                break;
                
            case "class-enrollment":
                content = "You have been enrolled in the class \"" + variables.getOrDefault("className", "Class") + "\" taught by " + variables.getOrDefault("teacherName", "your teacher") + ".";
                if (variables.containsKey("loginUrl") && variables.get("loginUrl") != null) {
                    actionButton = "<p><a href=\"" + variables.get("loginUrl") + "\" class=\"button\">View Class</a></p>";
                }
                break;
                
            case "live-class-notification":
                content = "Live class \"" + variables.getOrDefault("className", "Class") + "\" is starting at " + variables.getOrDefault("startTime", "now") + ". Join now!";
                if (variables.containsKey("joinUrl") && variables.get("joinUrl") != null) {
                    actionButton = "<p><a href=\"" + variables.get("joinUrl") + "\" class=\"button\">Join Class</a></p>";
                }
                break;
                
            case "class-cancellation":
                content = "The class \"" + variables.getOrDefault("className", "Class") + "\" has been cancelled. Reason: " + variables.getOrDefault("reason", "Not specified");
                break;
                
            case "account-status":
                content = "Your account status has been updated to: " + variables.getOrDefault("status", "Updated") + ". " + variables.getOrDefault("reason", "");
                break;
                
            case "maintenance-notification":
                content = "Scheduled maintenance on " + variables.getOrDefault("maintenanceDate", "TBD") + " for approximately " + variables.getOrDefault("duration", "unknown duration") + ". The platform may be temporarily unavailable.";
                break;
                
            case "platform-update":
                content = "{{platformName}} has been updated with new features and improvements: " + variables.getOrDefault("updateDetails", "Various improvements");
                if (variables.containsKey("loginUrl") && variables.get("loginUrl") != null) {
                    actionButton = "<p><a href=\"" + variables.get("loginUrl") + "\" class=\"button\">Explore Updates</a></p>";
                }
                break;
                
            case "new-student-enrollment":
                content = "A new student, " + variables.getOrDefault("studentName", "Student") + ", has enrolled in your class \"" + variables.getOrDefault("className", "Class") + "\".";
                if (variables.containsKey("loginUrl") && variables.get("loginUrl") != null) {
                    actionButton = "<p><a href=\"" + variables.get("loginUrl") + "\" class=\"button\">View Class</a></p>";
                }
                break;
                
            case "submission-received":
                content = variables.getOrDefault("studentName", "A student") + " has submitted their work for \"" + variables.getOrDefault("assignmentTitle", "Assignment") + "\".";
                if (variables.containsKey("loginUrl") && variables.get("loginUrl") != null) {
                    actionButton = "<p><a href=\"" + variables.get("loginUrl") + "\" class=\"button\">Review Submission</a></p>";
                }
                break;
                
            case "quiz-completed":
                content = variables.getOrDefault("studentName", "A student") + " has completed the quiz \"" + variables.getOrDefault("quizTitle", "Quiz") + "\" with a score of " + variables.getOrDefault("score", "N/A") + ".";
                if (variables.containsKey("loginUrl") && variables.get("loginUrl") != null) {
                    actionButton = "<p><a href=\"" + variables.get("loginUrl") + "\" class=\"button\">View Results</a></p>";
                }
                break;
                
            case "new-user-registration":
                content = "A new user has registered: " + variables.getOrDefault("userName", "User") + " (" + variables.getOrDefault("userEmail", "email") + ") as " + variables.getOrDefault("userRole", "user") + ".";
                if (variables.containsKey("adminUrl") && variables.get("adminUrl") != null) {
                    actionButton = "<p><a href=\"" + variables.get("adminUrl") + "\" class=\"button\">Manage Users</a></p>";
                }
                break;
                
            case "system-error":
                content = "A system error has occurred at " + variables.getOrDefault("timestamp", "unknown time") + ": " + variables.getOrDefault("errorDetails", "Error details not available");
                break;
                
            case "daily-report":
                content = "Daily system report for " + variables.getOrDefault("date", "today") + ": " + variables.getOrDefault("reportData", "No data available");
                break;
                
            default:
                content = "This is a notification from {{platformName}}.";
                break;
        }
        
        return baseTemplate.replace("{{content}}", content).replace("{{actionButton}}", actionButton);
    }
    
    private String getBaseEmailTemplate() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>{{platformName}}</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; }
                    .container { max-width: 600px; margin: 0 auto; background: white; }
                    .header { background: #2563eb; color: white; padding: 30px 20px; text-align: center; }
                    .header h1 { margin: 0; font-size: 28px; }
                    .content { padding: 40px 30px; background: #f8fafc; }
                    .content h2 { color: #1e293b; margin-top: 0; }
                    .content p { margin: 15px 0; }
                    .button { display: inline-block; padding: 12px 30px; background: #2563eb; color: white; text-decoration: none; border-radius: 6px; font-weight: bold; margin: 20px 0; }
                    .button:hover { background: #1d4ed8; }
                    .footer { padding: 30px 20px; text-align: center; font-size: 14px; color: #64748b; background: #f1f5f9; }
                    .footer p { margin: 5px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>{{platformName}}</h1>
                    </div>
                    <div class="content">
                        <h2>Hello {{userName}}!</h2>
                        <p>{{content}}</p>
                        {{actionButton}}
                    </div>
                    <div class="footer">
                        <p>&copy; 2025 {{platformName}}. All rights reserved.</p>
                        <p>If you have any questions, contact us at {{supportEmail}}</p>
                    </div>
                </div>
            </body>
            </html>
            """;
    }
}