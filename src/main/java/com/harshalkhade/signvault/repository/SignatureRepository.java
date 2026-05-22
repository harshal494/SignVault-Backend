package com.harshalkhade.signvault.repository;

import com.harshalkhade.signvault.entity.Contract;
import com.harshalkhade.signvault.entity.Signature;
import com.harshalkhade.signvault.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SignatureRepository extends JpaRepository<Signature, Long> {
    List<Signature> findByContract(Contract contract);
    Optional<Signature> findByContractAndUser(Contract contract, User user);
    boolean existsByContractAndUser(Contract contract, User user);

}
