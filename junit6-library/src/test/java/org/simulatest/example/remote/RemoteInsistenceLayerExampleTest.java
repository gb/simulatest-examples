package org.simulatest.example.remote;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.simulatest.environment.annotation.UseEnvironment;
import org.simulatest.example.library.LibraryDatabase;
import org.simulatest.example.library.environment.ActiveCirculationEnvironment;
import org.simulatest.example.library.environment.OpenLibraryEnvironment;
import org.simulatest.example.library.environment.ReferenceDataEnvironment;
import org.simulatest.example.library.environment.StaffedLibraryEnvironment;
import org.simulatest.example.library.environment.StockedLibraryEnvironment;

class RemoteInsistenceLayerExampleTest {

	@Nested
	@UseEnvironment(ReferenceDataEnvironment.class)
	class AtReferenceDataLevel {

		@Test
		void referenceDataVisible() {
			assertEquals(5, LibraryDatabase.queryInt("SELECT COUNT(*) FROM genre"));
			assertEquals(3, LibraryDatabase.queryInt("SELECT COUNT(*) FROM member_type"));
		}

		@Test
		void childDataNotYetVisible() {
			assertEquals(0, LibraryDatabase.queryInt("SELECT COUNT(*) FROM branch"));
			assertEquals(0, LibraryDatabase.queryInt("SELECT COUNT(*) FROM book"));
		}

		@Test
		void insertIsIsolatedBetweenTests() {
			LibraryDatabase.execute("INSERT INTO genre VALUES (99, 'Temp Genre')");
			assertEquals(6, LibraryDatabase.queryInt("SELECT COUNT(*) FROM genre"));
		}

		@Test
		void previousInsertWasRolledBack() {
			assertEquals(5, LibraryDatabase.queryInt("SELECT COUNT(*) FROM genre"));
		}
	}

	@Nested
	@UseEnvironment(OpenLibraryEnvironment.class)
	class AtOpenLibraryLevel {

		@Test
		void branchesAndParentDataVisible() {
			assertEquals(3, LibraryDatabase.queryInt("SELECT COUNT(*) FROM branch"));
			assertEquals(5, LibraryDatabase.queryInt("SELECT COUNT(*) FROM genre"));
		}

		@Test
		void deeperDataNotYetVisible() {
			assertEquals(0, LibraryDatabase.queryInt("SELECT COUNT(*) FROM book"));
			assertEquals(0, LibraryDatabase.queryInt("SELECT COUNT(*) FROM staff"));
		}
	}

	@Nested
	@UseEnvironment(StockedLibraryEnvironment.class)
	class AtStockedLibraryLevel {

		@Test
		void catalogDataVisible() {
			assertEquals(10, LibraryDatabase.queryInt("SELECT COUNT(*) FROM book"));
			assertEquals(18, LibraryDatabase.queryInt("SELECT COUNT(*) FROM book_copy"));
		}

		@Test
		void fullAncestryVisible() {
			assertEquals(5, LibraryDatabase.queryInt("SELECT COUNT(*) FROM genre"));
			assertEquals(3, LibraryDatabase.queryInt("SELECT COUNT(*) FROM branch"));
		}

		@Test
		void siblingDataInvisible() {
			assertEquals(0, LibraryDatabase.queryInt("SELECT COUNT(*) FROM staff"));
		}

		@Test
		void mutationIsolatedBetweenTests() {
			LibraryDatabase.execute("DELETE FROM book_copy WHERE branch_id = 1");
			assertEquals(11, LibraryDatabase.queryInt("SELECT COUNT(*) FROM book_copy"));
		}

		@Test
		void previousMutationWasRolledBack() {
			assertEquals(18, LibraryDatabase.queryInt("SELECT COUNT(*) FROM book_copy"));
		}
	}

	@Nested
	@UseEnvironment(StaffedLibraryEnvironment.class)
	class AtStaffedLibraryLevel {

		@Test
		void staffDataVisible() {
			assertEquals(7, LibraryDatabase.queryInt("SELECT COUNT(*) FROM staff"));
		}

		@Test
		void siblingSubtreeRolledBack() {
			assertEquals(0, LibraryDatabase.queryInt("SELECT COUNT(*) FROM book"));
			assertEquals(0, LibraryDatabase.queryInt("SELECT COUNT(*) FROM book_copy"));
			assertEquals(0, LibraryDatabase.queryInt("SELECT COUNT(*) FROM member"));
			assertEquals(0, LibraryDatabase.queryInt("SELECT COUNT(*) FROM loan"));
		}

		@Test
		void sharedAncestorDataSurvives() {
			assertEquals(3, LibraryDatabase.queryInt("SELECT COUNT(*) FROM branch"));
			assertEquals(5, LibraryDatabase.queryInt("SELECT COUNT(*) FROM genre"));
		}
	}

	@Nested
	@UseEnvironment(ActiveCirculationEnvironment.class)
	class AtActiveCirculationLevel {

		@Test
		void loansAndHoldsVisible() {
			assertEquals(5, LibraryDatabase.queryInt(
				"SELECT COUNT(*) FROM loan WHERE return_date IS NULL"));
			assertEquals(2, LibraryDatabase.queryInt(
				"SELECT COUNT(*) FROM hold WHERE status = 'ACTIVE'"));
		}

		@Test
		void fullAncestryChainVisible() {
			assertEquals(5, LibraryDatabase.queryInt("SELECT COUNT(*) FROM genre"));
			assertEquals(3, LibraryDatabase.queryInt("SELECT COUNT(*) FROM member_type"));
			assertEquals(3, LibraryDatabase.queryInt("SELECT COUNT(*) FROM branch"));
			assertEquals(10, LibraryDatabase.queryInt("SELECT COUNT(*) FROM book"));
			assertEquals(18, LibraryDatabase.queryInt("SELECT COUNT(*) FROM book_copy"));
			assertEquals(8, LibraryDatabase.queryInt("SELECT COUNT(*) FROM member"));
		}

		@Test
		void checkedOutCopiesMarkedCorrectly() {
			assertEquals(5, LibraryDatabase.queryInt(
				"SELECT COUNT(*) FROM book_copy WHERE status = 'CHECKED_OUT'"));
			assertEquals(13, LibraryDatabase.queryInt(
				"SELECT COUNT(*) FROM book_copy WHERE status = 'AVAILABLE'"));
		}

		@Test
		void multiTableWorkflowIsIsolated() {
			LibraryDatabase.execute("UPDATE loan SET return_date = CURRENT_DATE WHERE id = 1");
			LibraryDatabase.execute("UPDATE book_copy SET status = 'AVAILABLE' WHERE id = 1");
			LibraryDatabase.execute("INSERT INTO hold VALUES (3, 1, 5, CURRENT_DATE, 'ACTIVE')");

			assertEquals(4, LibraryDatabase.queryInt(
				"SELECT COUNT(*) FROM loan WHERE return_date IS NULL"));
			assertEquals(3, LibraryDatabase.queryInt(
				"SELECT COUNT(*) FROM hold WHERE status = 'ACTIVE'"));
			assertEquals("AVAILABLE", LibraryDatabase.queryString(
				"SELECT status FROM book_copy WHERE id = 1"));
		}

		@Test
		void multiTableWorkflowWasRolledBack() {
			assertEquals(5, LibraryDatabase.queryInt(
				"SELECT COUNT(*) FROM loan WHERE return_date IS NULL"));
			assertEquals(2, LibraryDatabase.queryInt(
				"SELECT COUNT(*) FROM hold WHERE status = 'ACTIVE'"));
			assertEquals("CHECKED_OUT", LibraryDatabase.queryString(
				"SELECT status FROM book_copy WHERE id = 1"));
		}

		@Test
		void overdueDetectionWorksThroughRemote() {
			assertTrue(LibraryDatabase.queryExists(
				"SELECT 1 FROM loan WHERE id = 2 AND due_date < CURRENT_DATE AND return_date IS NULL"));
			assertFalse(LibraryDatabase.queryExists(
				"SELECT 1 FROM loan WHERE id = 1 AND due_date < CURRENT_DATE AND return_date IS NULL"));
		}
	}
}
