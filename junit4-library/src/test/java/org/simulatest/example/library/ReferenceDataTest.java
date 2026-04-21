package org.simulatest.example.library;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.simulatest.environment.annotation.UseEnvironment;
import org.simulatest.environment.junit.EnvironmentJUnitRunner;
import org.simulatest.example.library.environment.ReferenceDataEnvironment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * Tests at the ROOT level — only genres and member types exist.
 *
 * <p>This is the foundation layer. Every test here mutates reference data and
 * relies on per-test rollback to keep siblings from seeing the damage.
 */
@RunWith(EnvironmentJUnitRunner.class)
@UseEnvironment(ReferenceDataEnvironment.class)
public class ReferenceDataTest {

	@Test
	public void fiveGenresExist() {
		assertEquals(5, LibraryDatabase.queryInt("SELECT COUNT(*) FROM genre"));
	}

	@Test
	public void addNewGenre() {
		assertEquals(5, LibraryDatabase.queryInt("SELECT COUNT(*) FROM genre"));

		LibraryDatabase.execute("INSERT INTO genre VALUES (6, 'Romance')");

		assertEquals(6, LibraryDatabase.queryInt("SELECT COUNT(*) FROM genre"));
		assertTrue(LibraryDatabase.queryExists("SELECT 1 FROM genre WHERE name = 'Romance'"));
	}

	@Test
	public void renameGenre() {
		assertEquals("Fiction", LibraryDatabase.queryString("SELECT name FROM genre WHERE id = 1"));

		LibraryDatabase.execute("UPDATE genre SET name = 'Literary Fiction' WHERE id = 1");

		assertEquals("Literary Fiction", LibraryDatabase.queryString("SELECT name FROM genre WHERE id = 1"));
	}

	@Test
	public void deleteAllGenres() {
		assertEquals(5, LibraryDatabase.queryInt("SELECT COUNT(*) FROM genre"));

		LibraryDatabase.execute("DELETE FROM genre");

		assertEquals(0, LibraryDatabase.queryInt("SELECT COUNT(*) FROM genre"));
	}

	@Test
	public void scienceGenreExists() {
		assertTrue(LibraryDatabase.queryExists("SELECT 1 FROM genre WHERE id = 3 AND name = 'Science'"));
	}

	@Test
	public void fictionGenreIsStillFiction() {
		// Same-row isolation: renameGenre changes genre 1 to "Literary Fiction".
		// fiveGenresExist wouldn't catch this (count stays 5 either way).
		assertEquals("Fiction", LibraryDatabase.queryString("SELECT name FROM genre WHERE id = 1"));
	}

	@Test
	public void deleteAndReinsertSamePk() {
		// Delete genre 5 ("Children"), insert DIFFERENT genre with PK=5.
		// If the savepoint doesn't restore original, the next test sees "Philosophy".
		assertEquals("Children", LibraryDatabase.queryString("SELECT name FROM genre WHERE id = 5"));

		LibraryDatabase.execute("DELETE FROM genre WHERE id = 5");
		LibraryDatabase.execute("INSERT INTO genre VALUES (5, 'Philosophy')");

		assertEquals("Philosophy", LibraryDatabase.queryString("SELECT name FROM genre WHERE id = 5"));
		assertEquals(5, LibraryDatabase.queryInt("SELECT COUNT(*) FROM genre"));
	}

	@Test
	public void genreFiveIsStillChildren() {
		assertEquals("Children", LibraryDatabase.queryString("SELECT name FROM genre WHERE id = 5"));
	}

	@Test
	public void insertGenreThenDeleteItWithinSameTest() {
		LibraryDatabase.execute("INSERT INTO genre VALUES (6, 'Romance')");
		assertEquals(6, LibraryDatabase.queryInt("SELECT COUNT(*) FROM genre"));

		LibraryDatabase.execute("DELETE FROM genre WHERE id = 6");
		assertEquals(5, LibraryDatabase.queryInt("SELECT COUNT(*) FROM genre"));
	}

	@Test
	public void updateNonexistentGenreChangesNothing() {
		LibraryDatabase.execute("UPDATE genre SET name = 'Ghost' WHERE id = 999");

		assertEquals(5, LibraryDatabase.queryInt("SELECT COUNT(*) FROM genre"));
		assertFalse(LibraryDatabase.queryExists("SELECT 1 FROM genre WHERE name = 'Ghost'"));
	}

	@Test
	public void threeMemberTypesExist() {
		assertEquals(3, LibraryDatabase.queryInt("SELECT COUNT(*) FROM member_type"));
	}

	@Test
	public void addSeniorMemberType() {
		assertEquals(3, LibraryDatabase.queryInt("SELECT COUNT(*) FROM member_type"));

		LibraryDatabase.execute("INSERT INTO member_type VALUES (4, 'Senior', 7, 28, 0)");

		assertEquals(4, LibraryDatabase.queryInt("SELECT COUNT(*) FROM member_type"));
		assertEquals(28, LibraryDatabase.queryInt("SELECT loan_period_days FROM member_type WHERE name = 'Senior'"));
	}

	@Test
	public void increasePremiumCheckoutLimit() {
		assertEquals(10, LibraryDatabase.queryInt("SELECT max_checkouts FROM member_type WHERE name = 'Premium'"));

		LibraryDatabase.execute("UPDATE member_type SET max_checkouts = 15 WHERE name = 'Premium'");

		assertEquals(15, LibraryDatabase.queryInt("SELECT max_checkouts FROM member_type WHERE name = 'Premium'"));
	}

	@Test
	public void premiumCheckoutLimitIsStillTen() {
		assertEquals(10, LibraryDatabase.queryInt("SELECT max_checkouts FROM member_type WHERE name = 'Premium'"));
	}

	@Test
	public void childrenMemberHasNoFines() {
		assertEquals(0, LibraryDatabase.queryInt("SELECT fine_per_day_cents FROM member_type WHERE name = 'Children'"));

		LibraryDatabase.execute("UPDATE member_type SET fine_per_day_cents = 50 WHERE name = 'Children'");

		assertEquals(50, LibraryDatabase.queryInt("SELECT fine_per_day_cents FROM member_type WHERE name = 'Children'"));
	}

	@Test
	public void childrenFinesAreStillZero() {
		assertEquals(0, LibraryDatabase.queryInt("SELECT fine_per_day_cents FROM member_type WHERE name = 'Children'"));
	}

	@Test
	public void deleteChildrenMemberType() {
		assertEquals(3, LibraryDatabase.queryInt("SELECT COUNT(*) FROM member_type"));

		LibraryDatabase.execute("DELETE FROM member_type WHERE name = 'Children'");

		assertEquals(2, LibraryDatabase.queryInt("SELECT COUNT(*) FROM member_type"));
		assertFalse(LibraryDatabase.queryExists("SELECT 1 FROM member_type WHERE name = 'Children'"));
	}

	@Test
	public void deleteMultipleGenresButNotAll() {
		LibraryDatabase.execute("DELETE FROM genre WHERE id IN (1, 2)");

		assertEquals(3, LibraryDatabase.queryInt("SELECT COUNT(*) FROM genre"));
		assertFalse(LibraryDatabase.queryExists("SELECT 1 FROM genre WHERE id = 1"));
		assertTrue(LibraryDatabase.queryExists("SELECT 1 FROM genre WHERE id = 3"));
	}

	@Test
	public void allFiveGenresStillExistAfterPartialDelete() {
		assertEquals(5, LibraryDatabase.queryInt("SELECT COUNT(*) FROM genre"));
		assertTrue(LibraryDatabase.queryExists("SELECT 1 FROM genre WHERE id = 1"));
	}

	@Test
	public void failedUpdateDoesNotCorruptSavepoint() {
		// JUnit 4 equivalent of the @ParameterizedTest in junit6-library: one
		// negative path per test method. This is the FK-constraint-survival story.
		assertThrows(RuntimeException.class, () ->
			LibraryDatabase.execute("INSERT INTO member_type VALUES (1, 'Dup', 5, 14, 25)"));

		assertEquals(3, LibraryDatabase.queryInt("SELECT COUNT(*) FROM member_type"));
	}

}
