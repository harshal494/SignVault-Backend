package com.harshalkhade.signvault.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class EmailService {

    private final RestClient restClient;

    @Value("${signvault.brevo.api-key}")
    private String apiKey;

    @Value("${signvault.brevo.sender-email}")
    private String senderEmail;

    public EmailService() {
        this.restClient = RestClient.builder()
                .baseUrl("https://brevo.com")
                .build();
    }


    private void executeApiMailRequest(String to, String subject, String htmlContent) {
        log.info("[EmailService] Key length: {}", apiKey != null ? apiKey.length() : "NULL");
        log.info("[EmailService] Key starts with: {}", apiKey != null && apiKey.length() > 8 ? apiKey.substring(0, 8) : "NOTHING");

        try {
            Map<String, Object> requestBody = Map.of(
                    "sender", Map.of("name", "SignVault", "email", senderEmail),
                    "to", List.of(Map.of("email", to)),
                    "subject", subject,
                    "htmlContent", htmlContent
            );

            log.info("[EmailService] Sending API email to: {} using sender: {}", to, senderEmail);

            restClient.post()
                    .uri("/smtp/email")
                    .header("api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    // Aggressively capture any explicit error reason returned directly from Brevo
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        byte[] bodyBytes = response.getBody().readAllBytes();
                        String errorDetails = new String(bodyBytes, StandardCharsets.UTF_8);
                        log.error("[EmailService] Brevo API Error Response: Status Code: {}, Body: {}", response.getStatusCode(), errorDetails);
                        throw new RuntimeException("Brevo rejected transmission: " + errorDetails);
                    })
                    .toBodilessEntity();

            log.info("[EmailService] Email successfully delivered to: {}", to);
        } catch (Exception e) {
            log.error("[EmailService] Crash during execution pipeline: {}", e.getMessage());
            throw new RuntimeException("Failed to send email via Brevo Web API", e);
        }
    }

    public void sendOtpEmail(String to, String otp) {
        String htmlContent = "<h2>This is your OTP for Email service: </h2><h1>" + otp + "</h1>";
        executeApiMailRequest(to, "Email OTP verification", htmlContent);
    }

    @Async
    public void sendContractNotification(String to, String senderName, String contractTitle, String contractId) {
        String htmlContent = "<h2>You have received a new contract on SignVault</h2>" +
                "<p><strong>" + senderName + "</strong> has sent you a contract titled " +
                "<strong>" + contractTitle + "</strong> for your review and signature.</p>" +
                "<p>Contract ID: <strong>" + contractId + "</strong></p>" +
                "<p>Please login to SignVault to review and sign the contract.</p>" +
                "<br><p>— SignVault Team</p>";

        executeApiMailRequest(to, "New Contract Received — " + contractTitle, htmlContent);
    }

    @Async
    public void sendExpiryReminder(String to, String contractTitle, long daysLeft) {
        String htmlContent = "<h2>Your contract <strong>" + contractTitle + "</strong> is expiring soon!\n " +
                "Only <strong>" + daysLeft + "</strong> days left.\n Renew it in the last 2 days!</h2>";

        executeApiMailRequest(to, "Expiry Reminder", htmlContent);
    }
}


























//package com.harshalkhade.signvault.service;
//
//import jakarta.mail.MessagingException;
//import jakarta.mail.internet.MimeMessage;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.MimeMessageHelper;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//
//public class EmailService {
//
//    private final JavaMailSender mailSender;
//
//    @Value("${spring.mail.username}")
//    private String fromEmail;
//
//    public void sendOtpEmail(String to , String otp) {
//
//        try {
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//            helper.setFrom(fromEmail);
//            helper.setTo(to);
//            helper.setSubject("Email OTP verification");
//            helper.setText("<h2>This is your OTP for Email service: </h2><h1>" + otp + "</h1>", true);
//            mailSender.send(message);
//            log.info("Email OTP sent successfully");
//        } catch (MessagingException e) {
//            log.error("Failed to send email to: {}", to);
//            throw new RuntimeException("Failed to send email");
//        }
//
//
//    }
//
//    @Async
//    public void sendContractNotification(String to, String senderName, String contractTitle, String contractId) {
//
//        try {
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//            helper.setFrom(fromEmail);
//            helper.setTo(to);
//            helper.setSubject("New Contract Received — " + contractTitle);
//            helper.setText("<h2>You have received a new contract on SignVault</h2>" +
//            "<p><strong>" + senderName + "</strong> has sent you a contract titled " +
//                    "<strong>" + contractTitle + "</strong> for your review and signature.</p>" +
//                    "<p>Contract ID: <strong>" + contractId + "</strong></p>" +
//                    "<p>Please login to SignVault to review and sign the contract.</p>" +
//                    "<br><p>— SignVault Team</p>",
//                    true);
//            mailSender.send(message);
//            log.info("Email sent to: {}", to);
//        } catch (MessagingException e) {
//            log.error("Failed to send email to: {}", to, e);
//            throw new RuntimeException("Failed to send email");
//        }
//
//    }
//
//    @Async
//    public void sendExpiryReminder(String to, String contractTitle, long daysLeft) {
//
//        try {
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//            helper.setFrom(fromEmail);
//            helper.setTo(to);
//            helper.setSubject("Expiry Reminder");
//            helper.setText("<h2>Your contract <strong>" + contractTitle + "</strong> is expiring soon!\n " +
//                    "Only <strong>" + daysLeft + "</strong> days left.\n Renew it in the last 2 days!</h2>", true);
//            mailSender.send(message);
//            log.info("Expiry Email sent to: {}", to);
//        }catch (MessagingException e) {
//            log.error("Failed to send email to: {}", to, e);
//            throw new RuntimeException("Failed to send email");
//        }
//    }
//}
