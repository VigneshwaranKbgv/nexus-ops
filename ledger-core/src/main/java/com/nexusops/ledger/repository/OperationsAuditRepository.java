package com.nexusops.ledger.repository;

import com.nexusops.ledger.model.OperationsAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OperationsAuditRepository extends JpaRepository<OperationsAudit, Long> {
    List<OperationsAudit> findByStatus(String status);
}
