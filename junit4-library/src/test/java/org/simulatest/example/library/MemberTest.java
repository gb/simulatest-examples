package org.simulatest.example.library;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.simulatest.environment.annotation.UseEnvironment;
import org.simulatest.environment.junit.EnvironmentJUnitRunner;
import org.simulatest.example.library.environment.MembersEnvironment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * Tests at LEVEL 4 — full catalog + 8 members. Loans don't exist yet.
 */
@RunWith(EnvironmentJUnitRunner.class)
@UseEnvironment(MembersEnvironment.class)
public class MemberTest {

	@Test
	public void eightMembersRegistered() {
		assertEquals(8, LibraryDatabase.queryInt("SELECT COUNT(*) FROM member"));
	}

	@Test
	public void registerNewMember() {
		assertEquals(8, LibraryDatabase.queryInt("SELECT COUNT(*) FROM member"));

		LibraryDatabase.execute(
			"INSERT INTO member VALUES (9, 'Iris Newman', 'iris@email.com', 1, 1)");

		assertEquals(9, LibraryDatabase.queryInt("SELECT COUNT(*) FROM member"));
		assertTrue(LibraryDatabase.queryExists("SELECT 1 FROM member WHERE name = 'Iris Newman'"));
	}

	@Test
	public void upgradeMembership() {
		assertEquals(1, LibraryDatabase.queryInt("SELECT member_type_id FROM member WHERE id = 1"));

		LibraryDatabase.execute("UPDATE member SET member_type_id = 2 WHERE id = 1");

		assertEquals(2, LibraryDatabase.queryInt("SELECT member_type_id FROM member WHERE id = 1"));
	}

	@Test
	public void transferHomeBranch() {
		assertEquals(3, LibraryDatabase.queryInt("SELECT home_branch_id FROM member WHERE id = 4"));

		LibraryDatabase.execute("UPDATE member SET home_branch_id = 1 WHERE id = 4");

		assertEquals(1, LibraryDatabase.queryInt("SELECT home_branch_id FROM member WHERE id = 4"));
	}

	@Test
	public void updateEmail() {
		assertEquals("bob@email.com", LibraryDatabase.queryString("SELECT email FROM member WHERE id = 2"));

		LibraryDatabase.execute("UPDATE member SET email = 'robert@newmail.com' WHERE id = 2");

		assertEquals("robert@newmail.com", LibraryDatabase.queryString("SELECT email FROM member WHERE id = 2"));
		assertFalse(LibraryDatabase.queryExists("SELECT 1 FROM member WHERE email = 'bob@email.com'"));
	}

	@Test
	public void deleteMember() {
		assertEquals(8, LibraryDatabase.queryInt("SELECT COUNT(*) FROM member"));

		LibraryDatabase.execute("DELETE FROM member WHERE id = 7");

		assertEquals(7, LibraryDatabase.queryInt("SELECT COUNT(*) FROM member"));
	}

	@Test
	public void deleteAllChildrenMembers() {
		assertEquals(2, LibraryDatabase.queryInt("SELECT COUNT(*) FROM member WHERE member_type_id = 3"));

		LibraryDatabase.execute("DELETE FROM member WHERE member_type_id = 3");

		assertEquals(6, LibraryDatabase.queryInt("SELECT COUNT(*) FROM member"));
		assertEquals(0, LibraryDatabase.queryInt("SELECT COUNT(*) FROM member WHERE member_type_id = 3"));
	}

	@Test
	public void threeRegularThreePremiumTwoChildren() {
		assertEquals(3, LibraryDatabase.queryInt("SELECT COUNT(*) FROM member WHERE member_type_id = 1"));
		assertEquals(3, LibraryDatabase.queryInt("SELECT COUNT(*) FROM member WHERE member_type_id = 2"));
		assertEquals(2, LibraryDatabase.queryInt("SELECT COUNT(*) FROM member WHERE member_type_id = 3"));
	}

	@Test
	public void allMembersHaveUniqueEmails() {
		int total = LibraryDatabase.queryInt("SELECT COUNT(email) FROM member");
		int unique = LibraryDatabase.queryInt("SELECT COUNT(DISTINCT email) FROM member");
		assertEquals(total, unique);
	}

	@Test
	public void aliceIsStillRegular() {
		// Same-row isolation — upgradeMembership moves Alice to Premium.
		assertEquals(1, LibraryDatabase.queryInt("SELECT member_type_id FROM member WHERE id = 1"));
	}

	@Test
	public void bobsEmailIsStillOriginal() {
		assertEquals("bob@email.com", LibraryDatabase.queryString("SELECT email FROM member WHERE id = 2"));
	}

	// =========================================================================
	// Constraint-rejection tests. JUnit 4 can't mix @RunWith(Parameterized) with
	// the Simulatest runner, so the three cases from junit6-library become three
	// methods sharing a helper. The failed statements must leave the savepoint
	// intact — verified by the unchanged count each time.
	// =========================================================================

	@Test
	public void duplicateEmailIsRejected() {
		assertInvalidInsertRejected(9, "Fake Alice", "alice@email.com", 1, 1);
	}

	@Test
	public void invalidBranchIsRejected() {
		assertInvalidInsertRejected(9, "Nowhere Man", "nowhere@email.com", 1, 99);
	}

	@Test
	public void invalidMemberTypeIsRejected() {
		assertInvalidInsertRejected(9, "Bad Type", "badtype@email.com", 99, 1);
	}

	private void assertInvalidInsertRejected(int id, String name, String email, int typeId, int branchId) {
		assertThrows(RuntimeException.class, () ->
			LibraryDatabase.execute(
				"INSERT INTO member VALUES (" + id + ", '" + name + "', '" + email + "', " + typeId + ", " + branchId + ")"));

		assertEquals(8, LibraryDatabase.queryInt("SELECT COUNT(*) FROM member"));
	}

	@Test
	public void parentCatalogDataVisible() {
		assertEquals(10, LibraryDatabase.queryInt("SELECT COUNT(*) FROM book"));
		assertEquals(18, LibraryDatabase.queryInt("SELECT COUNT(*) FROM book_copy"));
	}

	@Test
	public void noLoansExistYet() {
		assertEquals(0, LibraryDatabase.queryInt("SELECT COUNT(*) FROM loan"));
		assertEquals(0, LibraryDatabase.queryInt("SELECT COUNT(*) FROM hold"));
	}

}
