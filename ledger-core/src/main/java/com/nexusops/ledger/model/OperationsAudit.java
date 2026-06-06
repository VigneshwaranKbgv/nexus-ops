package com.nexusops.ledger.model;

import javax.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "operations_audits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperationsAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String eventType; // e.g., SUPPLIER_DELAY, VOUCHER_ISSUED, SKIPPED

    @Column(nullable = false)
    private String triggerEventId;

    @Column(nullable = false)
    private String entityIdentifier; // e.g., Affected SKU or Order ID

    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String actionSummary; // JSON or plaintext details of action taken

    @Column(nullable = false)
    private String status; // PENDING_APPROVAL, EXECUTED, REJECTED

    @Column(nullable = false)
    private LocalDateTime timestamp;
}
