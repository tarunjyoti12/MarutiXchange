package com.marutixchange.test_drive_service.client;

import com.marutixchange.test_drive_service.dto.NotificationClientRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "notification-service",
        url = "${app.services.notification-service-url}"
)
public interface NotificationFeignClient {

    @PostMapping("/api/notifications/send")
    void sendNotification(@RequestBody NotificationClientRequest request);
}
