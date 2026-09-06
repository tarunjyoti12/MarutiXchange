package com.marutixchange.payment_service.service;

import com.marutixchange.payment_service.dto.*;
import java.util.List;

public interface EscrowService {
    EscrowResponse createEscrow(
            EscrowRequest request);
    EscrowResponse confirmDelivery(Long escrowId);
    EscrowResponse releaseFunds(
            Long escrowId, String notes);
    EscrowResponse raiseDispute(Long escrowId);
    EscrowResponse getEscrowById(Long id);
    EscrowResponse getEscrowByPaymentId(
            Long paymentId);
    List<EscrowResponse> getBuyerEscrows(
            Long buyerId);
    List<EscrowResponse> getSellerEscrows(
            Long sellerId);
    void processAutoReleases();
}