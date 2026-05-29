package com.harshalkhade.signvault.service;

import com.harshalkhade.signvault.entity.Contract;
import com.harshalkhade.signvault.entity.Notification;
import com.harshalkhade.signvault.entity.User;
import com.harshalkhade.signvault.enums.NotificationType;
import com.harshalkhade.signvault.exception.ResourceNotFoundException;
import com.harshalkhade.signvault.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j

public class NotificationService {

    private final NotificationRepository notificationRepository;


    public void createNotification(User user, Contract contract, NotificationType type,String message) {

        Notification notification = Notification.builder()
                .user(user)
                .contract(contract)
                .type(type)
                .message(message)
                .build();

        notificationRepository.save(notification);

        log.info("Notification has been created");
    }

    public void markAsRead(Long notificationId) {
         Notification notification =notificationRepository.findById(notificationId)
                         .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
         notification.setRead(true);
         notificationRepository.save(notification);
    }

    public void markAllAsRead(User user ) {
        List<Notification> unread = notificationRepository.findByUserAndRead(user, false);
        unread.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unread);
    }

    public List<Notification> getUserNotifications(User user) {
        return  notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public long getUnreadCount(User user) {
        return notificationRepository.countByUserAndRead(user, false);
    }
}
