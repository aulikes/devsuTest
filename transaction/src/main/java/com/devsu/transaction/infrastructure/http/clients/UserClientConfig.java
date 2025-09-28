package com.devsu.transaction.infrastructure.http.clients;

import com.devsu.transaction.application.exception.ClientNotFoundException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

class UserClientConfig {

    @Bean
    ErrorDecoder userClientErrorDecoder() {
        return new ErrorDecoder() {
            private final ErrorDecoder defaultDecoder = new ErrorDecoder.Default();

            @Override
            public Exception decode(String methodKey, Response response) {
                if (response.status() == 404) {
                    System.err.println("Feign 404 -> " + response.request().httpMethod()
                            + " " + response.request().url()); // <-- IMPRIME URL REAL
                    return new ClientNotFoundException("Client not found in user-service");
                }
                return defaultDecoder.decode(methodKey, response);
            }
        };
    }
}