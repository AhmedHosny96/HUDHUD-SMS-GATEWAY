package com.hudhud.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.IContext;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
//    @Value("${sms.endpoint}")
//    private String SMS_ENDPOINT;
//
//    @Value("${sms.username}")
//    private String SMS_USERNAME;
//
//    @Value("${sms.password}")
//    private String SMS_PASSWORD;
//
//    private final As asyncHttp;

    private final JavaMailSender mailSender;

    private final SpringTemplateEngine templateEngine;
    
    @Async
    public CompletableFuture<Void> sendMail(String toEmail, String subject, String templateName, IContext context) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            String htmlContent = templateEngine.process(templateName, context);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            log.info("Email sent to {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}", toEmail, e);
            throw new RuntimeException("Failed to send email", e);
        }
        return CompletableFuture.completedFuture(null);
    }//    public JSONObject sendSms(String phoneNumber, String message) {
//
//        var smsRequest = new JSONObject();
//        smsRequest.put("username", SMS_USERNAME);
//        smsRequest.put("password", SMS_PASSWORD);
//        smsRequest.put("receiverAddress", phoneNumber);
//        smsRequest.put("message", message);
//
//        log.info("SMS REQUEST : {}", smsRequest);
//
//        RequestBuilder requestBody = new RequestBuilder("POST")
//                .setUrl(SMS_ENDPOINT)
//                .setBody(smsRequest.toString());
//
//        JSONObject smsResponse = asyncHttp.sendRequest(requestBody);
//
//        log.info("SMS RESPONSE : {}", smsResponse);
//
//        return smsResponse;
//
//    }
}
