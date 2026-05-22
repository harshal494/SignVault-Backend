package com.harshalkhade.signvault.repository;

import com.harshalkhade.signvault.entity.Contract;
import com.harshalkhade.signvault.entity.ContractFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContractFileRepository extends JpaRepository<ContractFile, Long> {
    Optional<ContractFile> findByContract(Contract contract);
}
