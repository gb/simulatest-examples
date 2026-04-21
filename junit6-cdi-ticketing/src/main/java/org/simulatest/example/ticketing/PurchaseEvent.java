package org.simulatest.example.ticketing;

/**
 * Fired by {@link PurchaseService} after a purchase row is written. Observers
 * (currently just {@link InventoryObserver}) react synchronously, so their
 * writes land on the same connection and ride the same savepoint.
 */
public record PurchaseEvent(int tierId, int quantity) {}
