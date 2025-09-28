package com.devsu.user.application.service;

import com.devsu.user.application.exception.ClientNotFoundException;
import com.devsu.user.application.mapper.ClientAppMapper;
import com.devsu.user.application.result.ClientResult;
import com.devsu.user.domain.client.Client;
import com.devsu.user.domain.client.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de aplicación para consultar un cliente por id.
 */
@Service
@RequiredArgsConstructor
public class GetClientByClientIdService {

    private final ClientRepository repository;

    @Transactional(readOnly = true)
    public ClientResult execute(String cliendId) {
        Client client = repository.findByClientId(cliendId)
                .orElseThrow(() -> new ClientNotFoundException("Client not found cliendId=" + cliendId));
        return ClientAppMapper.toResult(client);
    }
}
