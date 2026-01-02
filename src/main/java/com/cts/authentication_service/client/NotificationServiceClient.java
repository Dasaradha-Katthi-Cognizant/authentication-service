package com.cts.authentication_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "NOTIFICATION-SERVICE", contextId = "notificationServiceClient")
public interface NotificationServiceClient {

    @PostMapping("/notifications/welcome")
    void sendWelcomeEmail(@RequestParam String email, @RequestParam String name);

    @PostMapping("/notifications/otp")
    public void sendOtpEmail(@RequestParam String email, @RequestParam String otp);
    
}
