package com.devsu.transaction.infrastructure.http.clients;

import com.devsu.transaction.application.dto.ClientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "userClient",
        url  = "${clients.base-url}",
        configuration = UserClientConfig.class
)
public interface UserClient {
    @GetMapping("/clientId/{clientId}")
    ClientResponse getClient(@PathVariable("clientId") String clientId);
}