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

import com.devsu.transaction.application.dto.ClientResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateAccountServiceTest {

    @Mock private AccountRepository accountRepository;
    @Mock private AccountNumberGenerator accountNumberGenerator;
    @Mock private ClientQueryPort clientQueryPort;

    @InjectMocks
    private CreateAccountService service;

    private Account persisted(String number, String balance, String clientId) {
        return Account.fromPersistence(
                1L,
                number,
                AccountType.AHORROS,
                Money.of(new BigDecimal(balance)),
                clientId,
                Instant.now(),
                true,
                new ArrayList<>(),
                Money.of(new BigDecimal(balance))
        );
    }

    @BeforeEach
    void resetMocks() {
        reset(accountRepository, accountNumberGenerator, clientQueryPort);
    }

    @Test
    void shouldCreateAccountAndReturnResult() {
        String clientId = "cli-123";
        String accountType = "AHORROS";
        BigDecimal initial = new BigDecimal("100.00");
        String generated = "ACC-001";

        CreateAccountCommand cmd = mock(CreateAccountCommand.class);
        when(cmd.clientId()).thenReturn(clientId);
        when(cmd.accountType()).thenReturn(accountType);
        when(cmd.initialBalance()).thenReturn(initial);

        ClientResponse clientOk = mock(ClientResponse.class);
        when(clientQueryPort.assertExists(clientId)).thenReturn(clientOk);

        when(accountNumberGenerator.generate()).thenReturn(generated);

        Account saved = persisted(generated, "100.00", clientId);
        when(accountRepository.save(any(Account.class))).thenReturn(saved);

        AccountResult mapped = mock(AccountResult.class);
        try (MockedStatic<AccountAppMapper> mocked = mockStatic(AccountAppMapper.class)) {
            mocked.when(() -> AccountAppMapper.toResult(saved)).thenReturn(mapped);

            AccountResult out = service.execute(cmd);

            assertThat(out).isSameAs(mapped);
            verify(clientQueryPort).assertExists(clientId);
            verify(accountNumberGenerator).generate();
            verify(accountRepository).save(any(Account.class));
            mocked.verify(() -> AccountAppMapper.toResult(saved));
            verifyNoMoreInteractions(accountRepository, accountNumberGenerator, clientQueryPort);
        }
    }

    @Test
    void shouldThrowClientNotFoundWhenDoesNotExist() {
        CreateAccountCommand cmd = mock(CreateAccountCommand.class);
        when(cmd.clientId()).thenReturn("missing");

        when(clientQueryPort.assertExists("missing")).thenReturn(null);

        assertThrows(ClientNotFoundException.class, () -> service.execute(cmd));

        verify(clientQueryPort).assertExists("missing");
        verifyNoInteractions(accountNumberGenerator);
        verifyNoInteractions(accountRepository);
    }
}
