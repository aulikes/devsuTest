package com.devsu.transaction.application.service;

import com.devsu.transaction.application.command.CreateAccountCommand;
import com.devsu.transaction.application.exception.ClientNotFoundException;
import com.devsu.transaction.application.mapper.AccountAppMapper;
import com.devsu.transaction.application.port.AccountNumberGenerator;
import com.devsu.transaction.application.port.ClientQueryPort;
import com.devsu.transaction.application.result.AccountResult;
import com.devsu.transaction.domain.model.account.Account;
import com.devsu.transaction.domain.model.account.AccountType;
import com.devsu.transaction.domain.model.money.Money;
import com.devsu.transaction.domain.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de aplicación que orquesta la creación de cuentas.
 * Se genera el número de cuenta, se valida el saldo inicial (>= 0) y se persiste.
 */
@Service
@RequiredArgsConstructor
public class CreateAccountService {

    private final AccountRepository accountRepository;
    private final AccountNumberGenerator accountNumberGenerator;
    private final ClientQueryPort clientQueryPort;

    @Transactional
    public AccountResult execute(CreateAccountCommand command) {
        if (clientQueryPort.assertExists(command.clientId()) == null) {
            throw new ClientNotFoundException(command.clientId());
        }
        // Se genera el número de cuenta
        String generatedNumber = accountNumberGenerator.generate();

        Account toSave = Account.create(
                generatedNumber,
                AccountType.valueOf(command.accountType()),
                Money.of(command.initialBalance()),
                command.clientId()
        );

        Account saved = accountRepository.save(toSave);
        return AccountAppMapper.toResult(saved);
    }
}
