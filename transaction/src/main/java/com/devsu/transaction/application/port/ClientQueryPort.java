package com.devsu.transaction.application.port;

import com.devsu.transaction.application.dto.ClientResponse;

public interface ClientQueryPort {
    ClientResponse assertExists(String clientId);
}
