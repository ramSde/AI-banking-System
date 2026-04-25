package com.banking.account.controller;

import com.banking.account.domain.Account;
import com.banking.account.domain.AccountStatus;
import com.banking.account.domain.AccountType;
import com.banking.account.dto.*;
import com.banking.account.mapper.AccountMapper;
import com.banking.account.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Account Controller
 * 
 * REST API endpoints for account management operations.
 */
@RestController
@RequestMapping("/v1/accounts")
@Tag(name = "Account Management", description = "APIs for managing bank accounts")
@SecurityRequirement(name = "Bearer Authentication")
public class AccountController {

    private static final Logger log = LoggerFactory.getLogger(AccountController.class);

    private final AccountService accountService;
    private final AccountMapper accountMapper;

    public AccountController(AccountService accountService, AccountMapper accountMapper) {
        this.accountService = accountService;
        this.accountMapper = accountMapper;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Create new account", description = "Create a new bank account for the authenticated user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Account created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request or max accounts exceeded"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(
            @Valid @RequestBody AccountCreateRequest request,
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        log.info("Creating account for user: {}", userId);

        Account account = accountService.createAccount(userId, request);
        AccountResponse response = accountMapper.toResponse(account);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @GetMapping("/my-accounts")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get my accounts", description = "Get all accounts for the authenticated user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Accounts retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getMyAccounts(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        log.info("Fetching accounts for user: {}", userId);

        List<Account> accounts = accountService.getAccountsByUserId(userId);
        List<AccountResponse> response = accountMapper.toResponseList(accounts);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get account by ID", description = "Get account details by account ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Account retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Account not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountById(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        log.info("Fetching account: {} for user: {}", id, userId);

        Account account = accountService.getAccountById(id);

        if (!account.getUserId().equals(userId) && !hasAdminRole(authentication)) {
            log.warn("User {} attempted to access account {} owned by {}", userId, id, account.getUserId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("ACCESS_DENIED", "You do not have permission to access this account"));
        }

        AccountResponse response = accountMapper.toResponse(account);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/number/{accountNumber}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get account by account number", description = "Get account details by account number")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Account retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Account not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountByNumber(
            @PathVariable String accountNumber,
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        log.info("Fetching account by number: {} for user: {}", accountNumber, userId);

        Account account = accountService.getAccountByAccountNumber(accountNumber);

        if (!account.getUserId().equals(userId) && !hasAdminRole(authentication)) {
            log.warn("User {} attempted to access account {} owned by {}", userId, accountNumber, account.getUserId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("ACCESS_DENIED", "You do not have permission to access this account"));
        }

        AccountResponse response = accountMapper.toResponse(account);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Update account", description = "Update account settings (overdraft limit, interest rate)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Account updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Account not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<AccountResponse>> updateAccount(
            @PathVariable UUID id,
            @Valid @RequestBody AccountUpdateRequest request,
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        log.info("Updating account: {} by user: {}", id, userId);

        Account account = accountService.getAccountById(id);

        if (!account.getUserId().equals(userId) && !hasAdminRole(authentication)) {
            log.warn("User {} attempted to update account {} owned by {}", userId, id, account.getUserId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("ACCESS_DENIED", "You do not have permission to update this account"));
        }

        account = accountService.updateAccount(id, request);
        AccountResponse response = accountMapper.toResponse(account);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Close account", description = "Close an account (must have zero balance)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Account closed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Account not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Cannot close account with non-zero balance"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<AccountResponse>> closeAccount(
            @PathVariable UUID id,
            @RequestParam(required = false) String reason,
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        log.info("Closing account: {} by user: {}", id, userId);

        Account account = accountService.getAccountById(id);

        if (!account.getUserId().equals(userId) && !hasAdminRole(authentication)) {
            log.warn("User {} attempted to close account {} owned by {}", userId, id, account.getUserId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("ACCESS_DENIED", "You do not have permission to close this account"));
        }

        account = accountService.closeAccount(id, reason != null ? reason : "User requested closure", userId);
        AccountResponse response = accountMapper.toResponse(account);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all accounts (Admin)", description = "Get all accounts with pagination (admin only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Accounts retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin only")
    })
    public ResponseEntity<ApiResponse<Page<AccountResponse>>> getAllAccounts(Pageable pageable) {
        log.info("Fetching all accounts with pagination");

        Page<Account> accounts = accountService.getAllAccounts(pageable);
        Page<AccountResponse> response = accounts.map(accountMapper::toResponse);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update account status (Admin)", description = "Update account status (admin only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Account not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin only")
    })
    public ResponseEntity<ApiResponse<AccountResponse>> updateAccountStatus(
            @PathVariable UUID id,
            @Valid @RequestBody AccountStatusUpdateRequest request,
            Authentication authentication
    ) {
        UUID adminId = UUID.fromString(authentication.getName());
        log.info("Admin {} updating account {} status to {}", adminId, id, request.getAccountStatus());

        Account account = accountService.updateAccountStatus(
                id,
                request.getAccountStatus(),
                request.getReason(),
                adminId
        );
        AccountResponse response = accountMapper.toResponse(account);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}/freeze")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Freeze account (Admin)", description = "Freeze an account (admin only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Account frozen successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Account not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin only")
    })
    public ResponseEntity<ApiResponse<AccountResponse>> freezeAccount(
            @PathVariable UUID id,
            @RequestParam String reason,
            Authentication authentication
    ) {
        UUID adminId = UUID.fromString(authentication.getName());
        log.info("Admin {} freezing account {}", adminId, id);

        Account account = accountService.freezeAccount(id, reason, adminId);
        AccountResponse response = accountMapper.toResponse(account);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}/unfreeze")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Unfreeze account (Admin)", description = "Unfreeze an account (admin only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Account unfrozen successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Account not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin only")
    })
    public ResponseEntity<ApiResponse<AccountResponse>> unfreezeAccount(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        UUID adminId = UUID.fromString(authentication.getName());
        log.info("Admin {} unfreezing account {}", adminId, id);

        Account account = accountService.unfreezeAccount(id, adminId);
        AccountResponse response = accountMapper.toResponse(account);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private boolean hasAdminRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }
}
