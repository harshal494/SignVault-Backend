package com.harshalkhade.signvault.repository;

import com.harshalkhade.signvault.entity.Contract;
import com.harshalkhade.signvault.entity.User;
import com.harshalkhade.signvault.enums.ContractStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {

    List<Contract> findBySender(User sender);
    List<Contract> findByReceiver(User receiver);
    Optional<Contract> findByContractId(String contractId);
    List<Contract> findByStatus(ContractStatus status);
    List<Contract> findByExpiresAtBefore(LocalDateTime dateTime);
    List<Contract> findBySenderOrReceiver(User sender, User receiver);
}
