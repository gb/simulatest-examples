package org.simulatest.example.ticketing;

public final class TicketingDatabaseException extends RuntimeException {
	public TicketingDatabaseException(String message, Throwable cause) { super(message, cause); }
	public TicketingDatabaseException(String message) { super(message); }
}
