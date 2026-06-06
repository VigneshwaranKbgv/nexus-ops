package com.nexusops.ledger.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {
    private String email;
    private String sku;
    private Integer quantity;
}
