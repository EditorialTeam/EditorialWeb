package com.editorial.auth.controller;

import com.editorial.auth.dto.account.EditAccountRequest;
import com.editorial.auth.dto.account.EditAccountResponse;
import com.editorial.auth.dto.account.GetAccountsResponse;
import com.editorial.auth.service.AccountService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/admin/accounts")
public class AdminAccountController {
    private final AccountService service;

    public AdminAccountController(AccountService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Page<GetAccountsResponse>> getAccounts(
            @Min(0) @RequestParam(name="page",defaultValue = "0") int page,
            @Min(1) @Max(100) @RequestParam(name="size", defaultValue = "20") int size) {
        Pageable request = PageRequest.of(page, size, Sort.by("id").ascending());

        return ResponseEntity.ok(service.getAccounts(request));
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<GetAccountsResponse> getAccountById(@PathVariable long accountId) {
        return ResponseEntity.ok(service.getAccountById(accountId));
    }

    @PatchMapping("/{accountId}")
    public ResponseEntity<EditAccountResponse> editAccount(@PathVariable long accountId,
                                                           @RequestBody EditAccountRequest request) {
        return ResponseEntity.ok(service.editAccount(accountId, request));
    }
}
