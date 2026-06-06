package com.nexusops.ledger.repository;

import com.nexusops.ledger.model.OrderTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderTicketRepository extends JpaRepository<OrderTicket, Long> {
    Optional<OrderTicket> findByOrderId(String orderId);
    List<OrderTicket> findBySku(String sku);
}
