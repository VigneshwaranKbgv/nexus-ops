package com.nexusops.ledger.model;

import javax.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "inventories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String sku;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private Integer stockLevel;

    @Column(nullable = false)
    private BigDecimal unitCost;

    @Column(nullable = false)
    private BigDecimal retailPrice;

    @Column(nullable = false)
    private String supplierId;

    @Column(nullable = false)
    private Integer salesVelocity; // Units sold per week

    @Column(nullable = false)
    private Integer supplierLeadTimeDays;
}
