package abb.tech.ticket_service.client;

import abb.tech.ticket_service.dto.response.UserResponse;
import abb.tech.ticket_service.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "${client.user-service.url}", configuration = FeignConfig.class)
public interface UserClient {

    @GetMapping("/users/{id}")
    UserResponse findById(@PathVariable Long id);
}
