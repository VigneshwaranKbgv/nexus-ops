package com.nexusops.ledger.event;

import lombok.*;
import java.io.Serializable;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketplaceEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private String eventId;
    private String eventType; // e.g., ORDER_PLACED, SUPPLIER_DELAY_ALERT
    private String timestamp;
    private Map<String, Object> payload;
}
