package com.nexusops.ledger.model;

import javax.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal creditBalance;

    @Column(nullable = false)
    private BigDecimal lifetimeValue;

    @Column(nullable = false)
    private String loyaltyTier; // VIP, GOLD, SILVER, REGULAR

    @Column(nullable = false)
    private Double churnRiskScore; // Probability between 0.0 and 1.0
}
