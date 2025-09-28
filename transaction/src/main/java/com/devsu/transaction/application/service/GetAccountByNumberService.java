package com.devsu.transaction.application.service;

import com.devsu.transaction.application.mapper.AccountAppMapper;
import com.devsu.transaction.application.result.AccountResult;
import com.devsu.transaction.application.exception.AccountNotFoundException;
import com.devsu.transaction.domain.model.account.Account;
import com.devsu.transaction.domain.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Servicio de aplicación para consultar una cuenta por su número. */
@Service
@RequiredArgsConstructor
public class GetAccountByNumberService {

    private final AccountRepository accountRepository;

    @Transactional(readOnly = true)
    public AccountResult execute(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));
        return AccountAppMapper.toResult(account);
    }
}
