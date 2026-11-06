package com.editorial.auth.service;

import com.editorial.auth.dto.GetAccountsResponse;
import com.editorial.auth.exception.AccountNotFoundException;
import com.editorial.auth.model.Account;
import com.editorial.auth.repository.AccountRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AccountService {
    private final AccountRepository repository;

    public AccountService(AccountRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<GetAccountsResponse> getAccounts(Pageable request) {
        return repository
                .findAll(request)
                .map((account) -> new GetAccountsResponse(
                        account.getId(),
                        account.getEmail(),
                        account.getUsername(),
                        account.getCreatedAt(),
                        account.getRole().name()));

    }

    @Transactional(readOnly = true)
    public GetAccountsResponse getAccountById(Long accountId) {
        Account foundAccount = repository.findById(accountId).orElseThrow(AccountNotFoundException::new);
        return new GetAccountsResponse(
                foundAccount.getId(),
                foundAccount.getEmail(),
                foundAccount.getUsername(),
                foundAccount.getCreatedAt(),
                foundAccount.getRole().name());
    }
}
