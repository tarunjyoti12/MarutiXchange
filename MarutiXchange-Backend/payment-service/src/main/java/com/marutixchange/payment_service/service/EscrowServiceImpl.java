package com.marutixchange.payment_service.service;

import com.marutixchange.payment_service.client.RuleEngineClient;
import com.marutixchange.payment_service.dto.*;
import com.marutixchange.payment_service.entity.Escrow;
import com.marutixchange.payment_service.exception.EscrowNotFoundException;
import com.marutixchange.payment_service.repository.EscrowRepository;
import com.marutixchange.payment_service.rules.RuleResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EscrowServiceImpl implements EscrowService {

    private final EscrowRepository escrowRepository;
    private final RuleEngineClient ruleEngineClient;

    @Override
    @Transactional
    public EscrowResponse createEscrow(EscrowRequest request) {

        RuleResult result = ruleEngineClient.evaluate(request);

        if (!result.isValid()) {
            throw new RuntimeException(
                    String.join(", ", result.getErrorMessages())
            );
        }

        String ref = "ESC-" + UUID.randomUUID();

        Escrow escrow = Escrow.builder()
                .escrowReference(ref)
                .paymentId(request.getPaymentId())
                .buyerId(request.getBuyerId())
                .sellerId(request.getSellerId())
                .heldAmount(request.getHeldAmount())
                .status(Escrow.EscrowStatus.FUNDS_HELD)
                .autoReleaseAt(LocalDateTime.now().plusDays(7))
                .build();

        return EscrowResponse.fromEntity(escrowRepository.save(escrow));
    }

    @Override
    @Transactional
    public EscrowResponse confirmDelivery(Long escrowId) {

        Escrow escrow = escrowRepository.findById(escrowId)
                .orElseThrow(() -> new EscrowNotFoundException("Escrow not found"));

        escrow.setStatus(Escrow.EscrowStatus.DELIVERY_ACTIVE);
        escrow.setDeliveryConfirmedAt(LocalDateTime.now());

        return EscrowResponse.fromEntity(escrowRepository.save(escrow));
    }

    @Override
    @Transactional
    public EscrowResponse releaseFunds(Long escrowId, String notes) {

        Escrow escrow = escrowRepository.findById(escrowId)
                .orElseThrow(() -> new EscrowNotFoundException("Escrow not found"));

        RuleResult result = ruleEngineClient.evaluate(escrow);

        if (!result.isValid()) {
            throw new RuntimeException(
                    String.join(", ", result.getErrorMessages())
            );
        }

        escrow.setStatus(Escrow.EscrowStatus.FUNDS_RELEASED);
        escrow.setFundsReleasedAt(LocalDateTime.now());
        escrow.setReleaseNotes(notes);

        return EscrowResponse.fromEntity(escrowRepository.save(escrow));
    }

    @Override
    @Transactional
    public EscrowResponse raiseDispute(Long escrowId) {

        Escrow escrow = escrowRepository.findById(escrowId)
                .orElseThrow(() -> new EscrowNotFoundException("Escrow not found"));

        escrow.setStatus(Escrow.EscrowStatus.DISPUTED);

        return EscrowResponse.fromEntity(escrowRepository.save(escrow));
    }

    @Override
    public EscrowResponse getEscrowById(Long id) {
        return null;
    }

    @Override
    public EscrowResponse getEscrowByPaymentId(Long paymentId) {
        return null;
    }

    @Override
    public List<EscrowResponse> getBuyerEscrows(Long buyerId) {
        return List.of();
    }

    @Override
    public List<EscrowResponse> getSellerEscrows(Long sellerId) {
        return List.of();
    }

    @Override
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void processAutoReleases() {

        List<Escrow> list =
                escrowRepository.findEscrowsForAutoRelease(LocalDateTime.now());

        list.forEach(e -> {
            e.setStatus(Escrow.EscrowStatus.FUNDS_RELEASED);
            e.setFundsReleasedAt(LocalDateTime.now());
            e.setReleaseNotes("Auto released");
            escrowRepository.save(e);
        });
    }
}