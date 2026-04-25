package com.banking.account.exception;

/**
 * Max Accounts Exceeded Exception
 * 
 * Thrown when a user attempts to create more accounts than allowed.
 */
public class MaxAccountsExceededException extends AccountException {

    public MaxAccountsExceededException(int maxAccounts) {
        super("Maximum number of accounts exceeded. Limit: " + maxAccounts);
    }

    public MaxAccountsExceededException(String message) {
        super(message);
    }
}
