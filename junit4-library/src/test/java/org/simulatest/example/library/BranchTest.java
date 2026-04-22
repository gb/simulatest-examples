package org.simulatest.example.library;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.simulatest.environment.annotation.UseEnvironment;
import org.simulatest.environment.junit.EnvironmentJUnitRunner;
import org.simulatest.example.library.environment.OpenLibraryEnvironment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests at the <b>open library</b> world-state — reference data + 3 branches,
 * nothing else.
 *
 * <p>This state is the pivot point of the tree: it splits into
 * {@code StockedLibraryEnvironment} (books arrive) and
 * {@code StaffedLibraryEnvironment} (people hired). If branch data leaks
 * between tests here, every downstream world-state inherits corrupt state.
 */
@RunWith(EnvironmentJUnitRunner.class)
@UseEnvironment(OpenLibraryEnvironment.class)
public class BranchTest {

	@Test
	public void threeBranchesExist() {
		assertEquals(3, LibraryDatabase.queryInt("SELECT COUNT(*) FROM branch"));
	}

	@Test
	public void openNewBranch() {
		assertEquals(3, LibraryDatabase.queryInt("SELECT COUNT(*) FROM branch"));

		LibraryDatabase.execute("INSERT INTO branch VALUES (4, 'Northgate Branch', '400 North Ave')");

		assertEquals(4, LibraryDatabase.queryInt("SELECT COUNT(*) FROM branch"));
		assertTrue(LibraryDatabase.queryExists("SELECT 1 FROM branch WHERE name = 'Northgate Branch'"));
	}

	@Test
	public void renameBranch() {
		assertEquals("Downtown Branch", LibraryDatabase.queryString("SELECT name FROM branch WHERE id = 1"));

		LibraryDatabase.execute("UPDATE branch SET name = 'Central Library' WHERE id = 1");

		assertEquals("Central Library", LibraryDatabase.queryString("SELECT name FROM branch WHERE id = 1"));
	}

	@Test
	public void relocateBranch() {
		String oldAddress = LibraryDatabase.queryString("SELECT address FROM branch WHERE id = 2");

		LibraryDatabase.execute("UPDATE branch SET address = '999 New Street' WHERE id = 2");

		assertEquals("999 New Street", LibraryDatabase.queryString("SELECT address FROM branch WHERE id = 2"));
		assertNotEquals("999 New Street", oldAddress);
	}

	@Test
	public void closeAllBranches() {
		assertEquals(3, LibraryDatabase.queryInt("SELECT COUNT(*) FROM branch"));

		LibraryDatabase.execute("DELETE FROM branch");

		assertEquals(0, LibraryDatabase.queryInt("SELECT COUNT(*) FROM branch"));
	}

	@Test
	public void downtownBranchIsStillCalledDowntown() {
		assertEquals("Downtown Branch", LibraryDatabase.queryString("SELECT name FROM branch WHERE id = 1"));
	}

	@Test
	public void westsideAddressIsStillOriginal() {
		assertEquals("250 Oak Avenue", LibraryDatabase.queryString("SELECT address FROM branch WHERE id = 2"));
	}

	@Test
	public void parentReferenceDataIsAccessible() {
		assertEquals(5, LibraryDatabase.queryInt("SELECT COUNT(*) FROM genre"));
		assertEquals(3, LibraryDatabase.queryInt("SELECT COUNT(*) FROM member_type"));
	}

	@Test
	public void noBooksExistYet() {
		assertEquals(0, LibraryDatabase.queryInt("SELECT COUNT(*) FROM book"));
	}

}
