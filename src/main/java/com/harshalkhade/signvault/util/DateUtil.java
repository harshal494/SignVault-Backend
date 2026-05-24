package com.harshalkhade.signvault.util;

import com.harshalkhade.signvault.enums.PeriodType;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class DateUtil {

    public LocalDateTime calculateExpiryDate(PeriodType periodType, Integer periodValue) {

        return switch (periodType) {
            case DAYS ->  LocalDateTime.now().plusDays(periodValue);
            case MONTHS -> LocalDateTime.now().plusMonths(periodValue);
            case YEARS -> LocalDateTime.now().plusYears(periodValue);
            case PERMANENT -> null ;
        };
    }

    public boolean isDaysBeforeExpiry(LocalDateTime expiresAt, int days) {

        long daysLeft = ChronoUnit.DAYS.between(LocalDateTime.now(), expiresAt);
        return daysLeft <= days && daysLeft >= 0;
    }

    public boolean isExpired(LocalDateTime expiresAt) {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }
}
