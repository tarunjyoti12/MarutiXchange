package com.marutixchange.bidding_service.controller;

import com.marutixchange.bidding_service.dto.ApiResponse;
import com.marutixchange.bidding_service.dto.AuctionRequest;
import com.marutixchange.bidding_service.dto.AuctionResponse;
import com.marutixchange.bidding_service.service.AuctionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auctions")
@RequiredArgsConstructor
public class AuctionController {

    private final AuctionService auctionService;

    @PostMapping
    public ResponseEntity<ApiResponse<AuctionResponse>> createAuction(
            @Valid @RequestBody AuctionRequest request,
            @RequestAttribute("userId") Long userId) {

        AuctionResponse response = auctionService.createAuction(request, userId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Auction created", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AuctionResponse>> getAuctionById(@PathVariable Long id) {
        AuctionResponse response = auctionService.getAuctionById(id);
        return ResponseEntity.ok(ApiResponse.success("Auction fetched", response));
    }

    @GetMapping("/live")
    public ResponseEntity<ApiResponse<List<AuctionResponse>>> getLiveAuctions() {
        List<AuctionResponse> response = auctionService.getLiveAuctions();
        return ResponseEntity.ok(ApiResponse.success("Live auctions fetched", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AuctionResponse>>> getAllAuctions() {
        List<AuctionResponse> response = auctionService.getAllAuctions();
        return ResponseEntity.ok(ApiResponse.success("All auctions fetched", response));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<AuctionResponse>>> getMyAuctions(
            @RequestAttribute("userId") Long sellerId) {

        List<AuctionResponse> response = auctionService.getSellerAuctions(sellerId);
        return ResponseEntity.ok(ApiResponse.success("My auctions fetched", response));
    }

    @PatchMapping("/{id}/end")
    public ResponseEntity<ApiResponse<AuctionResponse>> endAuction(@PathVariable Long id) {
        AuctionResponse response = auctionService.endAuction(id);
        return ResponseEntity.ok(ApiResponse.success("Auction ended", response));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<AuctionResponse>> cancelAuction(@PathVariable Long id) {
        AuctionResponse response = auctionService.cancelAuction(id);
        return ResponseEntity.ok(ApiResponse.success("Auction cancelled", response));
    }
}