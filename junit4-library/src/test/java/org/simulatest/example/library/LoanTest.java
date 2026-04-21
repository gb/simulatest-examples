package org.simulatest.example.library;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.simulatest.environment.annotation.UseEnvironment;
import org.simulatest.environment.junit.EnvironmentJUnitRunner;
import org.simulatest.example.library.environment.LoansEnvironment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * Tests at LEVEL 5 (leaf) — everything exists: 5 active loans, 2 holds,
 * the full library.
 */
@RunWith(EnvironmentJUnitRunner.class)
@UseEnvironment(LoansEnvironment.class)
public class LoanTest {

	@Test
	public void fiveActiveLoans() {
		assertEquals(5, LibraryDatabase.queryInt(
			"SELECT COUNT(*) FROM loan WHERE return_date IS NULL"));
	}

	@Test
	public void checkoutNewBook() {
		assertEquals(5, LibraryDatabase.queryInt(
			"SELECT COUNT(*) FROM loan WHERE return_date IS NULL"));

		LibraryDatabase.execute(
			"INSERT INTO loan VALUES (6, 3, 7, CURRENT_DATE, DATEADD('DAY', 14, CURRENT_DATE), NULL)");
		LibraryDatabase.execute("UPDATE book_copy SET status = 'CHECKED_OUT' WHERE id = 3");

		assertEquals(6, LibraryDatabase.queryInt(
			"SELECT COUNT(*) FROM loan WHERE return_date IS NULL"));
		assertEquals("CHECKED_OUT", LibraryDatabase.queryString("SELECT status FROM book_copy WHERE id = 3"));
	}

	@Test
	public void returnBook() {
		assertEquals(2, LibraryDatabase.queryInt(
			"SELECT COUNT(*) FROM loan WHERE member_id = 1 AND return_date IS NULL"));

		LibraryDatabase.execute("UPDATE loan SET return_date = CURRENT_DATE WHERE id = 1");
		LibraryDatabase.execute("UPDATE book_copy SET status = 'AVAILABLE' WHERE id = 1");

		assertEquals(1, LibraryDatabase.queryInt(
			"SELECT COUNT(*) FROM loan WHERE member_id = 1 AND return_date IS NULL"));
		assertEquals("AVAILABLE", LibraryDatabase.queryString("SELECT status FROM book_copy WHERE id = 1"));
	}

	@Test
	public void returnOverdueBook() {
		assertEquals(1, LibraryDatabase.queryInt(
			"SELECT COUNT(*) FROM loan WHERE due_date < CURRENT_DATE AND return_date IS NULL"));

		LibraryDatabase.execute("UPDATE loan SET return_date = CURRENT_DATE WHERE id = 2");
		LibraryDatabase.execute("UPDATE book_copy SET status = 'AVAILABLE' WHERE id = 5");

		assertEquals(0, LibraryDatabase.queryInt(
			"SELECT COUNT(*) FROM loan WHERE due_date < CURRENT_DATE AND return_date IS NULL"));
	}

	@Test
	public void deleteAllLoans() {
		assertEquals(5, LibraryDatabase.queryInt("SELECT COUNT(*) FROM loan"));

		LibraryDatabase.execute("DELETE FROM loan");
		LibraryDatabase.execute(
			"UPDATE book_copy SET status = 'AVAILABLE' WHERE status = 'CHECKED_OUT'");

		assertEquals(0, LibraryDatabase.queryInt("SELECT COUNT(*) FROM loan"));
		assertEquals(18, LibraryDatabase.queryInt(
			"SELECT COUNT(*) FROM book_copy WHERE status = 'AVAILABLE'"));
	}

	@Test
	public void aliceStillHasTwoActiveLoans() {
		assertEquals(2, LibraryDatabase.queryInt(
			"SELECT COUNT(*) FROM loan WHERE member_id = 1 AND return_date IS NULL"));
	}

	@Test
	public void loanTwoIsExactlyBobsOverdueLoan() {
		assertEquals(2, LibraryDatabase.queryInt("SELECT member_id FROM loan WHERE id = 2"));
		assertEquals(5, LibraryDatabase.queryInt("SELECT book_copy_id FROM loan WHERE id = 2"));
		assertNull(LibraryDatabase.queryString(
			"SELECT CAST(return_date AS VARCHAR) FROM loan WHERE id = 2"));
	}

	@Test
	public void copyOneIsStillCheckedOut() {
		assertEquals("CHECKED_OUT", LibraryDatabase.queryString("SELECT status FROM book_copy WHERE id = 1"));
	}

	@Test
	public void copyThreeIsStillAvailable() {
		assertEquals("AVAILABLE", LibraryDatabase.queryString("SELECT status FROM book_copy WHERE id = 3"));
	}

	@Test
	public void twoActiveHolds() {
		assertEquals(2, LibraryDatabase.queryInt("SELECT COUNT(*) FROM hold WHERE status = 'ACTIVE'"));
	}

	@Test
	public void placeNewHold() {
		assertEquals(2, LibraryDatabase.queryInt("SELECT COUNT(*) FROM hold WHERE status = 'ACTIVE'"));

		LibraryDatabase.execute(
			"INSERT INTO hold VALUES (3, 8, 6, CURRENT_DATE, 'ACTIVE')");

		assertEquals(3, LibraryDatabase.queryInt("SELECT COUNT(*) FROM hold WHERE status = 'ACTIVE'"));
	}

	@Test
	public void cancelHold() {
		LibraryDatabase.execute("UPDATE hold SET status = 'CANCELLED' WHERE id = 1");

		assertEquals(1, LibraryDatabase.queryInt("SELECT COUNT(*) FROM hold WHERE status = 'ACTIVE'"));
		assertEquals("CANCELLED", LibraryDatabase.queryString("SELECT status FROM hold WHERE id = 1"));
	}

	@Test
	public void deleteHold() {
		LibraryDatabase.execute("DELETE FROM hold WHERE id = 2");

		assertEquals(1, LibraryDatabase.queryInt("SELECT COUNT(*) FROM hold"));
		assertFalse(LibraryDatabase.queryExists("SELECT 1 FROM hold WHERE id = 2"));
	}

	@Test
	public void holdOneIsStillActive() {
		assertEquals("ACTIVE", LibraryDatabase.queryString("SELECT status FROM hold WHERE id = 1"));
	}

	@Test
	public void fiveCopiesCheckedOutThirteenAvailable() {
		assertEquals(5, LibraryDatabase.queryInt(
			"SELECT COUNT(*) FROM book_copy WHERE status = 'CHECKED_OUT'"));
		assertEquals(13, LibraryDatabase.queryInt(
			"SELECT COUNT(*) FROM book_copy WHERE status = 'AVAILABLE'"));
	}

	@Test
	public void markCopyAsLost() {
		LibraryDatabase.execute("UPDATE book_copy SET status = 'LOST' WHERE id = 5");

		assertEquals(1, LibraryDatabase.queryInt(
			"SELECT COUNT(*) FROM book_copy WHERE status = 'LOST'"));
	}

	@Test
	public void fullCheckoutWorkflow() {
		// Multi-table workflow: touch loan, book_copy, and hold in a single test.
		LibraryDatabase.execute(
			"INSERT INTO loan VALUES (6, 17, 5, CURRENT_DATE, DATEADD('DAY', 14, CURRENT_DATE), NULL)");
		LibraryDatabase.execute("UPDATE book_copy SET status = 'CHECKED_OUT' WHERE id = 17");

		LibraryDatabase.execute("UPDATE loan SET return_date = CURRENT_DATE WHERE id = 1");
		LibraryDatabase.execute("UPDATE book_copy SET status = 'AVAILABLE' WHERE id = 1");
		LibraryDatabase.execute("UPDATE hold SET status = 'FULFILLED' WHERE id = 1");

		LibraryDatabase.execute(
			"INSERT INTO loan VALUES (7, 1, 7, CURRENT_DATE, DATEADD('DAY', 14, CURRENT_DATE), NULL)");
		LibraryDatabase.execute("UPDATE book_copy SET status = 'CHECKED_OUT' WHERE id = 1");

		assertEquals(7, LibraryDatabase.queryInt("SELECT COUNT(*) FROM loan"));
		assertEquals(6, LibraryDatabase.queryInt(
			"SELECT COUNT(*) FROM loan WHERE return_date IS NULL"));
		assertEquals("FULFILLED", LibraryDatabase.queryString("SELECT status FROM hold WHERE id = 1"));
	}

	@Test
	public void fullWorkflowLeftNoTrace() {
		// fullCheckoutWorkflow touched three tables. All three must be clean.
		assertEquals(5, LibraryDatabase.queryInt("SELECT COUNT(*) FROM loan"));
		assertEquals(2, LibraryDatabase.queryInt("SELECT COUNT(*) FROM hold"));
		assertEquals(5, LibraryDatabase.queryInt(
			"SELECT COUNT(*) FROM book_copy WHERE status = 'CHECKED_OUT'"));
		assertEquals(0, LibraryDatabase.queryInt(
			"SELECT COUNT(*) FROM hold WHERE status = 'FULFILLED'"));
	}

	@Test
	public void deletingMemberWithActiveLoansFailsOnFK() {
		assertThrows(RuntimeException.class, () ->
			LibraryDatabase.execute("DELETE FROM member WHERE id = 1"));

		assertEquals(8, LibraryDatabase.queryInt("SELECT COUNT(*) FROM member"));
	}

	@Test
	public void deletingBookCopyWithActiveLoanFailsOnFK() {
		assertThrows(RuntimeException.class, () ->
			LibraryDatabase.execute("DELETE FROM book_copy WHERE id = 1"));

		assertEquals(18, LibraryDatabase.queryInt("SELECT COUNT(*) FROM book_copy"));
	}

	@Test
	public void entireAncestryIsVisible() {
		assertEquals(5,  LibraryDatabase.queryInt("SELECT COUNT(*) FROM genre"));
		assertEquals(3,  LibraryDatabase.queryInt("SELECT COUNT(*) FROM member_type"));
		assertEquals(3,  LibraryDatabase.queryInt("SELECT COUNT(*) FROM branch"));
		assertEquals(10, LibraryDatabase.queryInt("SELECT COUNT(*) FROM book"));
		assertEquals(18, LibraryDatabase.queryInt("SELECT COUNT(*) FROM book_copy"));
		assertEquals(8,  LibraryDatabase.queryInt("SELECT COUNT(*) FROM member"));
		assertTrue(LibraryDatabase.queryExists("SELECT 1 FROM hold WHERE id = 1"));
	}

}
