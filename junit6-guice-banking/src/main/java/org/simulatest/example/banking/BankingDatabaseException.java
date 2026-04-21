package org.simulatest.example.banking;

public final class BankingDatabaseException extends RuntimeException {
	public BankingDatabaseException(String message, Throwable cause) { super(message, cause); }
	public BankingDatabaseException(String message) { super(message); }
}
