package com.harshalkhade.signvault.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j

public class SmsService {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.phone-number}")
    private String twilioPhone;


    @PostConstruct
    public void initTwilio() {
        Twilio.init(accountSid, authToken);
        log.info("Twilio service initialized");
    }

    public void sendOtpSms(String phone, String otp) {

        try{
            Message.creator(
                    new PhoneNumber(phone),  //to
                    new PhoneNumber(twilioPhone), // from
                    "Your SignVault OTP is: " + otp + ". Valid for 10 minutes."
            ).create();
            log.info("SMS OTP has been sent to: {}", phone);
        } catch (Exception e) {
            log.error("Failed to sent SMS to: {}", phone, e);
            throw new RuntimeException("Failed to send SMS");
        }
    }
}
