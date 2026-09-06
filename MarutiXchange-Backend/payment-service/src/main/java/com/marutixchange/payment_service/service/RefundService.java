package com.marutixchange.payment_service.service;

import com.marutixchange.payment_service.dto.*;
import java.util.List;

public interface RefundService {
    RefundResponse initiateRefund(
            RefundRequest request);
    RefundResponse getRefundByReference(
            String refundReference);
    List<RefundResponse> getBuyerRefunds(
            Long buyerId);
}