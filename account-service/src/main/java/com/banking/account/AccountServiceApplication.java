package com.banking.account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Account Service Application
 * 
 * Manages bank accounts, balances, account types, and IBAN generation.
 * Provides multi-account support with real-time balance tracking.
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableKafka
public class AccountServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountServiceApplication.class, args);
    }
}
