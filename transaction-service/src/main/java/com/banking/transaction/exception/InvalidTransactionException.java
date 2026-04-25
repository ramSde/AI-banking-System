package com.banking.transaction.exception;

/**
 * Invalid Transaction Exception
 * 
 * Thrown when a transaction request violates business rules.
 */
public class InvalidTransactionException extends TransactionException {

    public InvalidTransactionException(String message) {
        super(message);
    }

    public static InvalidTransactionException missingSourceAccount() {
        return new InvalidTransactionException("Source account is required for this transaction type");
    }

    public static InvalidTransactionException missingDestinationAccount() {
        return new InvalidTransactionException("Destination account is required for this transaction type");
    }

    public static InvalidTransactionException sameSourceAndDestination() {
        return new InvalidTransactionException("Source and destination accounts cannot be the same");
    }

    public static InvalidTransactionException currencyMismatch() {
        return new InvalidTransactionException("Account currency does not match transaction currency");
    }

    public static InvalidTransactionException cannotReverse(String reason) {
        return new InvalidTransactionException(String.format("Transaction cannot be reversed: %s", reason));
    }
}
