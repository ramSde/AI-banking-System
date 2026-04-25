package com.banking.account.repository;

import com.banking.account.domain.Account;
import com.banking.account.domain.AccountStatus;
import com.banking.account.domain.AccountType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Account Repository
 * 
 * Data access layer for Account entity with custom queries.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    /**
     * Find account by ID excluding soft-deleted
     */
    @Query("SELECT a FROM Account a WHERE a.id = :id AND a.deletedAt IS NULL")
    Optional<Account> findByIdAndNotDeleted(@Param("id") UUID id);

    /**
     * Find account by account number
     */
    @Query("SELECT a FROM Account a WHERE a.accountNumber = :accountNumber AND a.deletedAt IS NULL")
    Optional<Account> findByAccountNumber(@Param("accountNumber") String accountNumber);

    /**
     * Find account by IBAN
     */
    @Query("SELECT a FROM Account a WHERE a.iban = :iban AND a.deletedAt IS NULL")
    Optional<Account> findByIban(@Param("iban") String iban);

    /**
     * Find all accounts for a user
     */
    @Query("SELECT a FROM Account a WHERE a.userId = :userId AND a.deletedAt IS NULL ORDER BY a.createdAt DESC")
    List<Account> findByUserId(@Param("userId") UUID userId);

    /**
     * Find all accounts for a user with pagination
     */
    @Query("SELECT a FROM Account a WHERE a.userId = :userId AND a.deletedAt IS NULL")
    Page<Account> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Find accounts by user and status
     */
    @Query("SELECT a FROM Account a WHERE a.userId = :userId AND a.accountStatus = :status AND a.deletedAt IS NULL")
    List<Account> findByUserIdAndStatus(@Param("userId") UUID userId, @Param("status") AccountStatus status);

    /**
     * Find accounts by user and type
     */
    @Query("SELECT a FROM Account a WHERE a.userId = :userId AND a.accountType = :type AND a.deletedAt IS NULL")
    List<Account> findByUserIdAndType(@Param("userId") UUID userId, @Param("type") AccountType type);

    /**
     * Count active accounts for a user
     */
    @Query("SELECT COUNT(a) FROM Account a WHERE a.userId = :userId AND a.accountStatus = 'ACTIVE' AND a.deletedAt IS NULL")
    long countActiveAccountsByUserId(@Param("userId") UUID userId);

    /**
     * Check if account number exists
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Account a WHERE a.accountNumber = :accountNumber AND a.deletedAt IS NULL")
    boolean existsByAccountNumber(@Param("accountNumber") String accountNumber);

    /**
     * Check if IBAN exists
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Account a WHERE a.iban = :iban AND a.deletedAt IS NULL")
    boolean existsByIban(@Param("iban") String iban);

    /**
     * Find all accounts by status
     */
    @Query("SELECT a FROM Account a WHERE a.accountStatus = :status AND a.deletedAt IS NULL")
    Page<Account> findByStatus(@Param("status") AccountStatus status, Pageable pageable);

    /**
     * Find all accounts by type
     */
    @Query("SELECT a FROM Account a WHERE a.accountType = :type AND a.deletedAt IS NULL")
    Page<Account> findByType(@Param("type") AccountType type, Pageable pageable);
}
