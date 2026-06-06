package com.nexusops.ledger.service;

import com.nexusops.ledger.model.Account;
import com.nexusops.ledger.model.Inventory;
import com.nexusops.ledger.repository.AccountRepository;
import com.nexusops.ledger.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final AccountRepository accountRepository;
    private final InventoryRepository inventoryRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("Initializing in-memory database mock seeding...");

        // 1. Seed Customer Accounts if empty
        if (accountRepository.count() == 0) {
            Account vip = Account.builder()
                    .name("Vigneshwaran K")
                    .email("vip_customer@gmail.com")
                    .creditBalance(new BigDecimal("500.00"))
                    .lifetimeValue(new BigDecimal("2500.00"))
                    .loyaltyTier("VIP")
                    .churnRiskScore(0.78)
                    .build();

            Account regular = Account.builder()
                    .name("Jane Smith")
                    .email("regular_customer@gmail.com")
                    .creditBalance(new BigDecimal("100.00"))
                    .lifetimeValue(new BigDecimal("150.00"))
                    .loyaltyTier("REGULAR")
                    .churnRiskScore(0.12)
                    .build();

            accountRepository.save(vip);
            accountRepository.save(regular);
            log.info("Seeded 2 customer accounts (including VIP with high churn risk).");
        }

        // 2. Seed Warehouse Products / Inventories if empty
        if (inventoryRepository.count() == 0) {
            Inventory semicon = Inventory.builder()
                    .sku("SKU-SEMICON-99")
                    .productName("Logitech Semiconductor Processing Unit")
                    .stockLevel(50)
                    .unitCost(new BigDecimal("120.00"))
                    .retailPrice(new BigDecimal("200.00"))
                    .supplierId("spl_logitech_01")
                    .salesVelocity(15)
                    .supplierLeadTimeDays(5)
                    .build();

            Inventory oled = Inventory.builder()
                    .sku("SKU-DISP-4K")
                    .productName("UltraHD OLED Display Panel")
                    .stockLevel(100)
                    .unitCost(new BigDecimal("80.00"))
                    .retailPrice(new BigDecimal("150.00"))
                    .supplierId("spl_samsung_02")
                    .salesVelocity(25)
                    .supplierLeadTimeDays(7)
                    .build();

            inventoryRepository.save(semicon);
            inventoryRepository.save(oled);
            log.info("Seeded 2 primary inventory SKUs.");
        }

        log.info("Database mock seeding successfully completed!");
    }
}
