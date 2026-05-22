package com.harshalkhade.signvault.entity;

import com.harshalkhade.signvault.enums.ContractStatus;
import com.harshalkhade.signvault.enums.PeriodType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contracts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)

public class Contract {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String contractId;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    private ContractStatus status;

    private String fileHash;

    @Enumerated(EnumType.STRING)
    private PeriodType periodType;

    private Integer periodValue;
    private LocalDate periodFrom;
    private LocalDate periodTo;
    private boolean permanent;

    @ManyToOne
    @JoinColumn(name = "parent_contract_id")
    private Contract parentContract;

    private boolean renewal;
    private boolean renewalDone;


    private LocalDateTime expiresAt;

    @CreatedDate
    private LocalDateTime createdAt;


}
