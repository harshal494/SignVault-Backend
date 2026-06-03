package com.harshalkhade.signvault.controller;

import com.harshalkhade.signvault.dto.response.ApiResponse;
import com.harshalkhade.signvault.dto.response.NotificationResponse;
import com.harshalkhade.signvault.entity.Notification;
import com.harshalkhade.signvault.entity.User;
import com.harshalkhade.signvault.exception.ResourceNotFoundException;
import com.harshalkhade.signvault.repository.UserRepository;
import com.harshalkhade.signvault.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse> getUserNotifications(Authentication authentication) {

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        log.info("Fetching notifications for {}", email);

        List<Notification> notifications = notificationService.getUserNotifications(user);

        List<NotificationResponse> mappedList = notifications.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse(true,"Notifications fetched successfully", mappedList));
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse> markAsRead(@PathVariable Long id, Authentication authentication) {

        String email = authentication.getName();
        log.info("Marking notification {} as read for {}", id, email);
        notificationService.markAsRead(id);
        return ResponseEntity.ok(new ApiResponse(true, "Notification marked as read", null));
    }

    @PutMapping("/read-all")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse> markAllAsRead(Authentication authentication) {

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        log.info("Marking all notifications as read for {}", email);

        notificationService.markAllAsRead(user);
        return ResponseEntity.ok(new ApiResponse(true, "All notifications marked as read", null));
    }

    @GetMapping("/unread-count")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse> getUnreadCount(Authentication authentication) {

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        log.info("Fetching unread count for {}", email);
        long count = notificationService.getUnreadCount(user);
        return ResponseEntity.ok(new ApiResponse(true, "Unread count fetched successfully", count));
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .type(notification.getType())
                .read(notification.isRead())
                .contractId(notification.getContract().getContractId())
                .createdAt(notification.getCreatedAt())
                .build();
    }

}
