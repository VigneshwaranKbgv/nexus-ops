package com.nexusops.ledger.controller;

import com.nexusops.ledger.dto.OrderRequest;
import com.nexusops.ledger.event.MarketplaceEvent;
import com.nexusops.ledger.model.Account;
import com.nexusops.ledger.model.Inventory;
import com.nexusops.ledger.model.OperationsAudit;
import com.nexusops.ledger.model.OrderTicket;
import com.nexusops.ledger.repository.AccountRepository;
import com.nexusops.ledger.repository.InventoryRepository;
import com.nexusops.ledger.repository.OperationsAuditRepository;
import com.nexusops.ledger.repository.OrderTicketRepository;
import com.nexusops.ledger.service.KafkaEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/ledger")
@RequiredArgsConstructor
@Slf4j
public class LedgerController {

    private final AccountRepository accountRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderTicketRepository orderTicketRepository;
    private final OperationsAuditRepository operationsAuditRepository;
    private final KafkaEventPublisher kafkaEventPublisher;

    @GetMapping("/accounts")
    public List<Account> getAccounts() {
        return accountRepository.findAll();
    }

    @GetMapping("/inventory")
    public List<Inventory> getInventory() {
        return inventoryRepository.findAll();
    }

    @GetMapping("/orders")
    public List<OrderTicket> getOrders() {
        return orderTicketRepository.findAll();
    }

    @GetMapping("/audits")
    public List<OperationsAudit> getAudits() {
        return operationsAuditRepository.findAll();
    }

    @PostMapping("/orders")
    public ResponseEntity<?> placeOrder(@RequestBody OrderRequest request) {
        log.info("Processing order request: User={} SKU={} Qty={}", request.getEmail(), request.getSku(), request.getQuantity());

        // 1. Verify Customer Account
        Account account = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + request.getEmail()));

        // 2. Verify Inventory & SKU Stock
        Inventory inventory = inventoryRepository.findBySku(request.getSku())
                .orElseThrow(() -> new IllegalArgumentException("SKU not found: " + request.getSku()));

        if (inventory.getStockLevel() < request.getQuantity()) {
            return ResponseEntity.badRequest().body("Insufficient stock for SKU: " + request.getSku());
        }

        // 3. Compute Value & Check credit balance
        BigDecimal totalCost = inventory.getRetailPrice().multiply(BigDecimal.valueOf(request.getQuantity()));
        if (account.getCreditBalance().compareTo(totalCost) < 0) {
            return ResponseEntity.badRequest().body("Insufficient credit balance. Cost: $" + totalCost + ", Available: $" + account.getCreditBalance());
        }

        // 4. Update Database (Atomic Execution)
        inventory.setStockLevel(inventory.getStockLevel() - request.getQuantity());
        account.setCreditBalance(account.getCreditBalance().subtract(totalCost));
        account.setLifetimeValue(account.getLifetimeValue().add(totalCost));

        accountRepository.save(account);
        inventoryRepository.save(inventory);

        // 5. Save Order Ticket
        String generatedOrderId = "ord_" + UUID.randomUUID().toString().substring(0, 8);
        OrderTicket ticket = OrderTicket.builder()
                .orderId(generatedOrderId)
                .account(account)
                .sku(request.getSku())
                .quantity(request.getQuantity())
                .totalAmount(totalCost)
                .orderStatus("PLACED")
                .orderDate(LocalDateTime.now())
                .ticketStatus("NONE")
                .build();
        orderTicketRepository.save(ticket);

        // 6. Audit state change
        OperationsAudit audit = OperationsAudit.builder()
                .eventType("ORDER_PLACED")
                .triggerEventId("sys_" + UUID.randomUUID().toString().substring(0, 8))
                .entityIdentifier(generatedOrderId)
                .actionSummary("Placed order " + generatedOrderId + " for " + request.getQuantity() + " units of " + request.getSku())
                .status("EXECUTED")
                .timestamp(LocalDateTime.now())
                .build();
        operationsAuditRepository.save(audit);

        // 7. Publish ORDER_PLACED event to Kafka
        Map<String, Object> eventPayload = new HashMap<>();
        eventPayload.put("orderId", generatedOrderId);
        eventPayload.put("email", account.getEmail());
        eventPayload.put("sku", request.getSku());
        eventPayload.put("quantity", request.getQuantity());
        eventPayload.put("totalAmount", totalCost);
        eventPayload.put("loyaltyTier", account.getLoyaltyTier());
        eventPayload.put("churnRisk", account.getChurnRiskScore());

        MarketplaceEvent event = MarketplaceEvent.builder()
                .eventId("evt_" + UUID.randomUUID().toString().substring(0, 8))
                .eventType("ORDER_PLACED")
                .timestamp(LocalDateTime.now().toString())
                .payload(eventPayload)
                .build();

        kafkaEventPublisher.publishEvent("marketplace-events", event);

        return ResponseEntity.ok(ticket);
    }

    @PostMapping("/supplier/delay")
    public ResponseEntity<?> triggerSupplierDelay(
            @RequestParam String supplierId,
            @RequestParam String affectedSku,
            @RequestParam Integer delayDays) {

        log.warn("Simulating SUPPLIER_DELAY_ALERT: Supplier={} SKU={} DelayDays={}", supplierId, affectedSku, delayDays);

        // Find affected pending orders
        List<OrderTicket> affectedOrders = orderTicketRepository.findBySku(affectedSku);

        // 1. Audit incident entry as PENDING
        String triggerId = "evt_" + UUID.randomUUID().toString().substring(0, 8);
        OperationsAudit audit = OperationsAudit.builder()
                .eventType("SUPPLIER_DELAY_ALERT")
                .triggerEventId(triggerId)
                .entityIdentifier(affectedSku)
                .actionSummary(String.format("Supplier %s reported a manufacturing failure delaying %s by %d days. Affected orders: %d.", 
                        supplierId, affectedSku, delayDays, affectedOrders.size()))
                .status("PENDING_APPROVAL")
                .timestamp(LocalDateTime.now())
                .build();
        operationsAuditRepository.save(audit);

        // 2. Publish SUPPLIER_DELAY_ALERT event to Kafka to kickstart the FastAPI + LangGraph pipeline!
        Map<String, Object> eventPayload = new HashMap<>();
        eventPayload.put("triggerEventId", triggerId);
        eventPayload.put("supplierId", supplierId);
        eventPayload.put("affectedSku", affectedSku);
        eventPayload.put("delayDays", delayDays);
        eventPayload.put("pendingOrdersCount", affectedOrders.size());

        MarketplaceEvent event = MarketplaceEvent.builder()
                .eventId(triggerId)
                .eventType("SUPPLIER_DELAY_ALERT")
                .timestamp(LocalDateTime.now().toString())
                .payload(eventPayload)
                .build();

        kafkaEventPublisher.publishEvent("marketplace-events", event);

        return ResponseEntity.ok(Map.of(
                "message", "Supplier delay incident simulated and streamed successfully.",
                "triggerEventId", triggerId,
                "affectedSku", affectedSku,
                "delayDays", delayDays,
                "status", "PENDING_APPROVAL (Streaming to AI Analytics Pipeline)"
        ));
    }
}
