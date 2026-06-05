package com.harshalkhade.signvault.service;

import com.harshalkhade.signvault.entity.Contract;
import com.harshalkhade.signvault.entity.Notification;
import com.harshalkhade.signvault.entity.User;
import com.harshalkhade.signvault.enums.NotificationType;
import com.harshalkhade.signvault.repository.ContractRepository;
import com.harshalkhade.signvault.repository.NotificationRepository;
import com.harshalkhade.signvault.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerService {

    private final ContractRepository contractRepository;
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final DateUtil dateUtil;

    @Scheduled(fixedRate = 300000)
    public void sendExpiryReminders() {
        log.info("Running expiry reminders scheduler service");
        List<Contract> contracts = contractRepository.findAll();

        for (Contract contract : contracts) {
            if (contract.isPermanent()) continue;
            if (contract.getExpiresAt() == null) continue;
            if (dateUtil.isExpired(contract.getExpiresAt())) continue;

            if (dateUtil.isDaysBeforeExpiry(contract.getExpiresAt(), 7)) {
                sendReminderIfNotSent(contract, "7_DAYS","Your contract expires in 7 days", 7);
            }
            if (dateUtil.isDaysBeforeExpiry(contract.getExpiresAt(), 3)) {
                sendReminderIfNotSent(contract, "3_DAYS", "Your contract expires in 3 days", 3);
            }
            if (dateUtil.isDaysBeforeExpiry(contract.getExpiresAt(), 2)) {
                sendReminderIfNotSent(contract, "2_DAYS", "Your contract expires in 2 days", 2);
            }
            if (dateUtil.isDaysBeforeExpiry(contract.getExpiresAt(), 1)) {
                sendReminderIfNotSent(contract, "1_DAY", "Your contract expires tomorrow", 1);
            }

            long minutesLeft = ChronoUnit.MINUTES.between(LocalDateTime.now(), contract.getExpiresAt());

            if (minutesLeft <= 1440 && minutesLeft > 59) {
                sendReminderIfNotSent(contract, "24_HOURS", "Your contract expires in 24 hours!!!", 0);
            }
            if (minutesLeft <=60 && minutesLeft >29) {
                sendReminderIfNotSent(contract, "1_HOUR", "Your contract expires in 1 hour!!!", 0);
            }
            if (minutesLeft <= 30 && minutesLeft > 9) {
                sendReminderIfNotSent(contract, "30_MINS", "Your contract expires in 30 minutes!!!", 0);
            }
            if (minutesLeft <= 10 && minutesLeft > 0) {
                sendReminderIfNotSent(contract, "10_MINS", "Your contract expires in 10 minutes", 0);
            }
        }
    }

    private void sendReminderIfNotSent(Contract contract, String window, String message, long daysLeft) {

        User sender = contract.getSender();
        User receiver = contract.getReceiver();

        if (!notificationRepository.existsByUserAndContractAndTypeAndReminderWindow(sender, contract, NotificationType.EXPIRY_REMINDER, window)) {
            Notification notification = Notification.builder()
                    .user(sender)
                    .contract(contract)
                    .type(NotificationType.EXPIRY_REMINDER)
                    .message(message + " - " + contract.getTitle())
                    .read(false)
                    .emailSent(true)
                    .reminderWindow(window)
                    .build();
            notificationRepository.save(notification);

            emailService.sendExpiryReminder(sender.getEmail(), contract.getTitle(), daysLeft);
            log.info("Reminder sent to {} for contract {} window {}", sender.getEmail(),contract.getContractId(), window);
        }
        if (!notificationRepository.existsByUserAndContractAndTypeAndReminderWindow(receiver, contract, NotificationType.EXPIRY_REMINDER, window)) {
            Notification notification = Notification.builder()
                    .user(receiver)
                    .contract(contract)
                    .type(NotificationType.EXPIRY_REMINDER)
                    .message(message + " - " + contract.getTitle())
                    .read(false)
                    .emailSent(true)
                    .reminderWindow(window)
                    .build();
            notificationRepository.save(notification);

            emailService.sendExpiryReminder(receiver.getEmail(), contract.getTitle(), daysLeft);
            log.info("Reminder sent to {} for contract {} window {}", receiver.getEmail(),contract.getContractId(), window);
        }
    }
}
