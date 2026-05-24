package com.harshalkhade.signvault.util;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Random;

@Component
public class ContractIdGenerator {

    public String generateContractId() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        int year = LocalDate.now().getYear();
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            result.append(chars.charAt(random.nextInt(chars.length())));

        }
        return "SV-" + year + "-" + result;
    }
}
