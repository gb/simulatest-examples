package org.simulatest.example.library.environment;

import org.simulatest.environment.annotation.EnvironmentParent;
import org.simulatest.environment.Environment;
import org.simulatest.example.library.LibraryDatabase;

/**
 * World-state: <b>books are in circulation</b>.
 *
 * <p>Five loans are active — one of them overdue — and two holds are
 * queued against popular titles. This is the operational mid-afternoon:
 * members have borrowed, at least one forgot to bring a book back, and
 * the waiting list is building. Every other level above this one becomes
 * observable through its effects: checkouts flipped copy status, holds
 * reference both members and books.
 *
 * <p>Parent: {@link LendingLibraryEnvironment}. Inherits the full chain:
 * genres and member tiers, branches, books and copies, members. The leaf
 * of the tree — and the richest world-state the demo provides.
 */
@EnvironmentParent(LendingLibraryEnvironment.class)
public final class ActiveCirculationEnvironment implements Environment {

	@Override
	public void run() {
		// 5 active loans (id, copy_id, member_id, checkout_date, due_date, return_date)
		// Dates are relative to today so overdue status stays consistent.
		insertLoan(1, 1,  1,  -7,   7);  // Alice  → "The Great Adventure"
		insertLoan(2, 5,  2, -21,  -7);  // Bob    → "Quantum Physics"      (OVERDUE!)
		insertLoan(3, 7,  3,  -3,  18);  // Charlie → "World War II"
		insertLoan(4, 16, 4,  -5,  16);  // Diana  → "Ancient Rome"
		insertLoan(5, 11, 1, -10,   4);  // Alice  → "Mystery at Midnight"

		// Mark checked-out copies
		LibraryDatabase.execute("UPDATE book_copy SET status = 'CHECKED_OUT' WHERE id IN (1, 5, 7, 16, 11)");

		// 2 active holds (id, book_id, member_id, hold_date, status)
		LibraryDatabase.execute("INSERT INTO hold VALUES (1, 1, 7, DATEADD('DAY', -2, CURRENT_DATE), 'ACTIVE')");
		LibraryDatabase.execute("INSERT INTO hold VALUES (2, 3, 8, DATEADD('DAY', -1, CURRENT_DATE), 'ACTIVE')");
	}

	private void insertLoan(int id, int copyId, int memberId, int checkoutDaysAgo, int dueDaysFromNow) {
		LibraryDatabase.execute(
			"INSERT INTO loan VALUES (" + id + ", " + copyId + ", " + memberId + ", " +
			"DATEADD('DAY', " + checkoutDaysAgo + ", CURRENT_DATE), " +
			"DATEADD('DAY', " + dueDaysFromNow + ", CURRENT_DATE), NULL)");
	}

}
