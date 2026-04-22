package org.simulatest.example.library;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.simulatest.environment.annotation.UseEnvironment;
import org.simulatest.environment.junit.EnvironmentJUnitRunner;
import org.simulatest.example.library.environment.StaffedLibraryEnvironment;

import static org.junit.Assert.assertEquals;

/**
 * Tests at the <b>staffed library</b> world-state — staff on duty but no
 * books, members, or loans.
 *
 * <p>Dual purpose:
 * <ol>
 *   <li>Staff CRUD and isolation at this level.</li>
 *   <li>PROOF of sibling subtree isolation — the stocked subtree (books,
 *       copies, members, loans, holds) ran first and was rolled back before
 *       this state runs. None of its tables may leak here.</li>
 * </ol>
 */
@RunWith(EnvironmentJUnitRunner.class)
@UseEnvironment(StaffedLibraryEnvironment.class)
public class StaffTest {

	@Test
	public void sevenStaffMembers() {
		assertEquals(7, LibraryDatabase.queryInt("SELECT COUNT(*) FROM staff"));
	}

	@Test
	public void hireNewStaff() {
		assertEquals(7, LibraryDatabase.queryInt("SELECT COUNT(*) FROM staff"));

		LibraryDatabase.execute("INSERT INTO staff VALUES (8, 'Zoe Chen', 'ASSISTANT', 2)");

		assertEquals(8, LibraryDatabase.queryInt("SELECT COUNT(*) FROM staff"));
		assertEquals(3, LibraryDatabase.queryInt("SELECT COUNT(*) FROM staff WHERE branch_id = 2"));
	}

	@Test
	public void promoteToHeadLibrarian() {
		assertEquals(1, LibraryDatabase.queryInt(
			"SELECT COUNT(*) FROM staff WHERE branch_id = 1 AND role = 'HEAD_LIBRARIAN'"));

		LibraryDatabase.execute(
			"UPDATE staff SET role = 'LIBRARIAN' WHERE branch_id = 1 AND role = 'HEAD_LIBRARIAN'");
		LibraryDatabase.execute("UPDATE staff SET role = 'HEAD_LIBRARIAN' WHERE id = 2");

		assertEquals(1, LibraryDatabase.queryInt(
			"SELECT COUNT(*) FROM staff WHERE branch_id = 1 AND role = 'HEAD_LIBRARIAN'"));
	}

	@Test
	public void transferStaffToDifferentBranch() {
		assertEquals(3, LibraryDatabase.queryInt("SELECT COUNT(*) FROM staff WHERE branch_id = 1"));
		assertEquals(2, LibraryDatabase.queryInt("SELECT COUNT(*) FROM staff WHERE branch_id = 3"));

		LibraryDatabase.execute("UPDATE staff SET branch_id = 3 WHERE id = 2");

		assertEquals(2, LibraryDatabase.queryInt("SELECT COUNT(*) FROM staff WHERE branch_id = 1"));
		assertEquals(3, LibraryDatabase.queryInt("SELECT COUNT(*) FROM staff WHERE branch_id = 3"));
	}

	@Test
	public void fireAllStaff() {
		assertEquals(7, LibraryDatabase.queryInt("SELECT COUNT(*) FROM staff"));

		LibraryDatabase.execute("DELETE FROM staff");

		assertEquals(0, LibraryDatabase.queryInt("SELECT COUNT(*) FROM staff"));
	}

	@Test
	public void everyBranchStillHasAHeadLibrarian() {
		assertEquals(3, LibraryDatabase.queryInt("SELECT COUNT(*) FROM staff WHERE role = 'HEAD_LIBRARIAN'"));
	}

	@Test
	public void threeDistinctRolesExist() {
		assertEquals(3, LibraryDatabase.queryInt("SELECT COUNT(DISTINCT role) FROM staff"));
	}

	@Test
	public void margaretIsStillHeadLibrarian() {
		assertEquals("HEAD_LIBRARIAN", LibraryDatabase.queryString("SELECT role FROM staff WHERE id = 1"));
	}

	@Test
	public void robertIsStillAtDowntown() {
		assertEquals(1, LibraryDatabase.queryInt("SELECT branch_id FROM staff WHERE id = 2"));
		assertEquals("LIBRARIAN", LibraryDatabase.queryString("SELECT role FROM staff WHERE id = 2"));
	}

	@Test
	public void ancestorDataIsVisible() {
		assertEquals(3, LibraryDatabase.queryInt("SELECT COUNT(*) FROM branch"));
		assertEquals(5, LibraryDatabase.queryInt("SELECT COUNT(*) FROM genre"));
		assertEquals(3, LibraryDatabase.queryInt("SELECT COUNT(*) FROM member_type"));
	}

	// =========================================================================
	// Sibling isolation — the crown jewel. The stocked-library subtree
	// (book, book_copy, member, loan, hold) ran before the staffed state,
	// then was rolled back. If any row leaks, isolation is broken.
	// =========================================================================

	@Test
	public void noBooksExist_stockedSiblingWasRolledBack() {
		assertEquals(0, LibraryDatabase.queryInt("SELECT COUNT(*) FROM book"));
		assertEquals(0, LibraryDatabase.queryInt("SELECT COUNT(*) FROM book_copy"));
	}

	@Test
	public void noMembersOrLoansExist_entireSubtreeWasRolledBack() {
		assertEquals(0, LibraryDatabase.queryInt("SELECT COUNT(*) FROM member"));
		assertEquals(0, LibraryDatabase.queryInt("SELECT COUNT(*) FROM loan"));
		assertEquals(0, LibraryDatabase.queryInt("SELECT COUNT(*) FROM hold"));
	}

}
