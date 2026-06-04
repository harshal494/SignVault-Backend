package com.harshalkhade.signvault.entity;

import com.harshalkhade.signvault.enums.NotificationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)

public class Notification {
    @Id  //@Id for primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) //this for auto increament for id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "contract_id")
    private Contract contract;

    @Enumerated(EnumType.STRING)
    @Column(name = "`type`")
    private NotificationType type;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(name = "`read`", nullable = false, columnDefinition = "boolean default false")
    private boolean read;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean emailSent;

    private String reminderWindow;

    @CreatedDate
    private LocalDateTime createdAt;
}
