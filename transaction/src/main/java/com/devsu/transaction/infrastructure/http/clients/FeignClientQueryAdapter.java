package com.devsu.transaction.infrastructure.http.clients;

import com.devsu.transaction.application.port.ClientQueryPort;
import com.devsu.transaction.application.dto.ClientResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeignClientQueryAdapter implements ClientQueryPort {

    private final UserClient userClient;

    @Override
    public ClientResponse assertExists(String clientId) {
        return userClient.getClient(clientId);
    }
}

