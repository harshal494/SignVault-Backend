package com.harshalkhade.signvault.repository;

import com.harshalkhade.signvault.entity.Contract;
import com.harshalkhade.signvault.entity.Notification;
import com.harshalkhade.signvault.entity.User;
import com.harshalkhade.signvault.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    List<Notification> findByUserAndRead(User user, boolean read);
    long countByUserAndRead(User user, boolean read);
    boolean existsByUserAndContractAndTypeAndReminderWindow(User user, Contract contract, NotificationType type, String reminderWindow);
}
