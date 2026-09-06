package com.marutixchange.car_listing_service.service;

import com.marutixchange.car_listing_service.dto.PriceEstimateResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Year;

@Service
@Slf4j
public class PricingServiceImpl
        implements PricingService {

    private static final double BASE_PRICES_SWIFT = 700000;
    private static final double BASE_PRICES_BALENO = 800000;
    private static final double BASE_PRICES_VITARA = 1200000;
    private static final double BASE_PRICES_DEFAULT = 600000;

    private static final double DEPRECIATION_RATE = 0.15;

    @Override
    public PriceEstimateResponse getEstimate(
            String brand, String model,
            Integer year, Integer mileage) {

        log.info("Getting price estimate for: {} {}", brand, model);

        int currentYear = Year.now().getValue();
        int carAge = currentYear - year;

        double basePrice = getBasePrice(model);

        // ✅ FIXED: removed PricingRule dependency
        double depreciated = calculateDepreciation(basePrice, carAge);

        double mileageFactor = mileage != null
                ? Math.max(0.8, 1 - (mileage / 1000000.0))
                : 1.0;

        double marketPrice = depreciated * mileageFactor;

        return PriceEstimateResponse.builder()
                .brand(brand)
                .model(model)
                .year(year)
                .marketPrice(round(marketPrice))
                .depreciatedValue(round(depreciated))
                .minPrice(round(marketPrice * 0.85))
                .maxPrice(round(marketPrice * 1.15))
                .depreciationRate(15.0)
                .age(carAge)
                .recommendation(getRecommendation(carAge))
                .build();
    }

    // ✅ NEW METHOD (replaces PricingRule)
    private double calculateDepreciation(double originalPrice, int carAge) {
        return originalPrice * Math.pow(1 - DEPRECIATION_RATE, carAge);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private double getBasePrice(String model) {
        if (model == null) return BASE_PRICES_DEFAULT;

        return switch (model.toLowerCase()) {
            case "swift" -> BASE_PRICES_SWIFT;
            case "baleno" -> BASE_PRICES_BALENO;
            case "grand vitara" -> BASE_PRICES_VITARA;
            case "brezza" -> 1100000;
            case "ertiga" -> 950000;
            case "dzire" -> 750000;
            case "celerio" -> 550000;
            case "alto" -> 400000;
            default -> BASE_PRICES_DEFAULT;
        };
    }

    private String getRecommendation(int carAge) {
        if (carAge <= 2) return "Excellent condition expected";
        if (carAge <= 5) return "Good value for money";
        if (carAge <= 8) return "Check service history";
        return "Verify thoroughly before buying";
    }
}