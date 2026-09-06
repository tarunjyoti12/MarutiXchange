package com.marutixchange.order_service.client;

import com.marutixchange.order_service.dto.NotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service", url = "${app.services.notification-url:http://localhost:8051}")
public interface NotificationClient {

    @PostMapping("/api/notifications/send")
    void sendNotification(@RequestBody NotificationRequest request);
}
