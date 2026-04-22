package org.simulatest.example.library;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.simulatest.environment.annotation.UseEnvironment;
import org.simulatest.environment.junit.EnvironmentJUnitRunner;
import org.simulatest.example.library.environment.StockedLibraryEnvironment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * Tests at the <b>stocked library</b> world-state — 10 books and 18 copies
 * on the shelves across 3 branches, no members or staff yet.
 */
@RunWith(EnvironmentJUnitRunner.class)
@UseEnvironment(StockedLibraryEnvironment.class)
public class CatalogTest {

	@Test
	public void tenBooksInCatalog() {
		assertEquals(10, LibraryDatabase.queryInt("SELECT COUNT(*) FROM book"));
	}

	@Test
	public void addNewBook() {
		assertEquals(10, LibraryDatabase.queryInt("SELECT COUNT(*) FROM book"));

		LibraryDatabase.execute(
			"INSERT INTO book VALUES (11, 'The Martian', 'Andy Weir', '9780553418026', 3, 2014)");

		assertEquals(11, LibraryDatabase.queryInt("SELECT COUNT(*) FROM book"));
		assertTrue(LibraryDatabase.queryExists("SELECT 1 FROM book WHERE title = 'The Martian'"));
	}

	@Test
	public void updateBookTitle() {
		assertEquals("The Great Adventure", LibraryDatabase.queryString("SELECT title FROM book WHERE id = 1"));

		LibraryDatabase.execute("UPDATE book SET title = 'The Greatest Adventure' WHERE id = 1");

		assertEquals("The Greatest Adventure", LibraryDatabase.queryString("SELECT title FROM book WHERE id = 1"));
	}

	@Test
	public void changeBookGenre() {
		assertEquals(2, LibraryDatabase.queryInt("SELECT genre_id FROM book WHERE id = 2"));

		LibraryDatabase.execute("UPDATE book SET genre_id = 3 WHERE id = 2");

		assertEquals(3, LibraryDatabase.queryInt("SELECT genre_id FROM book WHERE id = 2"));
	}

	@Test
	public void removeBookAndItsCopies() {
		assertEquals(10, LibraryDatabase.queryInt("SELECT COUNT(*) FROM book"));
		assertEquals(18, LibraryDatabase.queryInt("SELECT COUNT(*) FROM book_copy"));

		LibraryDatabase.execute("DELETE FROM book_copy WHERE book_id = 7");
		LibraryDatabase.execute("DELETE FROM book WHERE id = 7");

		assertEquals(9, LibraryDatabase.queryInt("SELECT COUNT(*) FROM book"));
		assertFalse(LibraryDatabase.queryExists("SELECT 1 FROM book WHERE title = 'Cooking for Engineers'"));
	}

	@Test
	public void everyBookHasAnAuthorAndGenre() {
		assertEquals(10, LibraryDatabase.queryInt(
			"SELECT COUNT(*) FROM book b JOIN genre g ON b.genre_id = g.id"));
		assertEquals(0, LibraryDatabase.queryInt(
			"SELECT COUNT(*) FROM book WHERE author IS NULL OR author = ''"));
	}

	@Test
	public void bookTitleIsExactlyOriginal() {
		// Same-row isolation — updateBookTitle changes it to "The Greatest Adventure".
		assertEquals("The Great Adventure", LibraryDatabase.queryString("SELECT title FROM book WHERE id = 1"));
	}

	@Test
	public void bookTwoGenreIsStillNonFiction() {
		assertEquals(2, LibraryDatabase.queryInt("SELECT genre_id FROM book WHERE id = 2"));
	}

	@Test
	public void eighteenCopiesAcrossThreeBranches() {
		assertEquals(18, LibraryDatabase.queryInt("SELECT COUNT(*) FROM book_copy"));
		assertEquals(7, LibraryDatabase.queryInt("SELECT COUNT(*) FROM book_copy WHERE branch_id = 1"));
		assertEquals(6, LibraryDatabase.queryInt("SELECT COUNT(*) FROM book_copy WHERE branch_id = 2"));
		assertEquals(5, LibraryDatabase.queryInt("SELECT COUNT(*) FROM book_copy WHERE branch_id = 3"));
	}

	@Test
	public void markCopyAsDamaged() {
		assertEquals(18, LibraryDatabase.queryInt(
			"SELECT COUNT(*) FROM book_copy WHERE status = 'AVAILABLE'"));

		LibraryDatabase.execute("UPDATE book_copy SET status = 'DAMAGED' WHERE id = 13");

		assertEquals(17, LibraryDatabase.queryInt(
			"SELECT COUNT(*) FROM book_copy WHERE status = 'AVAILABLE'"));
		assertEquals("DAMAGED", LibraryDatabase.queryString("SELECT status FROM book_copy WHERE id = 13"));
	}

	@Test
	public void deleteAllCopiesAtEastville() {
		assertEquals(5, LibraryDatabase.queryInt("SELECT COUNT(*) FROM book_copy WHERE branch_id = 3"));

		LibraryDatabase.execute("DELETE FROM book_copy WHERE branch_id = 3");

		assertEquals(0, LibraryDatabase.queryInt("SELECT COUNT(*) FROM book_copy WHERE branch_id = 3"));
		assertEquals(7, LibraryDatabase.queryInt("SELECT COUNT(*) FROM book_copy WHERE branch_id = 1"));
	}

	@Test
	public void allCopiesStillAvailable() {
		assertEquals(18, LibraryDatabase.queryInt("SELECT COUNT(*) FROM book_copy"));
		assertEquals(18, LibraryDatabase.queryInt(
			"SELECT COUNT(*) FROM book_copy WHERE status = 'AVAILABLE'"));
	}

	@Test
	public void deletingBranchWithCopiesFails() {
		// FK constraint prevents deletion; the savepoint must survive the failed statement.
		assertThrows(RuntimeException.class, () ->
			LibraryDatabase.execute("DELETE FROM branch WHERE id = 1"));

		assertEquals(3, LibraryDatabase.queryInt("SELECT COUNT(*) FROM branch"));
		assertEquals(7, LibraryDatabase.queryInt("SELECT COUNT(*) FROM book_copy WHERE branch_id = 1"));
	}

	@Test
	public void insertingBookWithInvalidGenreFails() {
		assertThrows(RuntimeException.class, () ->
			LibraryDatabase.execute(
				"INSERT INTO book VALUES (11, 'Ghost Book', 'Nobody', '0000000000000', 99, 2024)"));

		assertEquals(10, LibraryDatabase.queryInt("SELECT COUNT(*) FROM book"));
	}

	@Test
	public void noMembersExistYet() {
		assertEquals(0, LibraryDatabase.queryInt("SELECT COUNT(*) FROM member"));
	}

	@Test
	public void noStaffExist() {
		// Sibling StaffedLibraryEnvironment's data is invisible here.
		assertEquals(0, LibraryDatabase.queryInt("SELECT COUNT(*) FROM staff"));
	}

}
