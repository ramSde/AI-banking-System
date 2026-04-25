package com.banking.account.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

/**
 * Account Service Configuration Properties
 * 
 * Externalized configuration for account service behavior.
 */
@Configuration
@ConfigurationProperties(prefix = "account")
@Data
public class AccountProperties {

    private Cache cache = new Cache();
    private Number number = new Number();
    private Iban iban = new Iban();
    private String defaultCurrency = "USD";
    private MinBalance minBalance = new MinBalance();
    private int maxAccountsPerUser = 10;

    @Data
    public static class Cache {
        private int balanceTtl = 5;
        private int detailsTtl = 15;
    }

    @Data
    public static class Number {
        private String prefix = "ACC";
    }

    @Data
    public static class Iban {
        private String countryCode = "US";
        private String bankCode = "BANK";
    }

    @Data
    public static class MinBalance {
        private BigDecimal savings = new BigDecimal("100.00");
        private BigDecimal checking = BigDecimal.ZERO;
    }
}
