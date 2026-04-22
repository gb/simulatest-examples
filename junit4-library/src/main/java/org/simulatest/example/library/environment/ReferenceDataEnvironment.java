package org.simulatest.example.library.environment;

import org.simulatest.environment.Environment;
import org.simulatest.example.library.LibraryDatabase;

/**
 * The Community Library — Environment Tree, read as a sequence of world-states:
 *
 * <pre>
 *   ReferenceDataEnvironment        ◄── ROOT (the library is chartered)
 *     └── OpenLibraryEnvironment               (branches exist, doors open)
 *           ├── StockedLibraryEnvironment      (books on the shelves)
 *           │     └── LendingLibraryEnvironment    (members enrolled, ready to lend)
 *           │           └── ActiveCirculationEnvironment  (loans out, holds queued)
 *           └── StaffedLibraryEnvironment       (a staffed building, no stock yet)
 * </pre>
 *
 * <p>World-state: <b>the library is chartered</b>. Genres and membership
 * tiers are defined — the rules of operation the rest of the domain will
 * plug into. Nothing exists physically yet: no branches, no books, no people.
 *
 * <p>An environment trusts its parent the way a class trusts its superclass;
 * every child is a strictly richer world-state than its ancestors. Siblings
 * ({@link StockedLibraryEnvironment} and {@link StaffedLibraryEnvironment})
 * represent independent next-states, and the Insistence Layer rolls back one
 * sibling's subtree before the other runs.
 *
 * <p>Schema is created by {@link LibraryDatabase#createSchema()} BEFORE the
 * tree runs, because DDL causes implicit commits that invalidate savepoints.
 */
public final class ReferenceDataEnvironment implements Environment {

	@Override
	public void run() {
		// Genres
		LibraryDatabase.execute("INSERT INTO genre VALUES (1, 'Fiction')");
		LibraryDatabase.execute("INSERT INTO genre VALUES (2, 'Non-Fiction')");
		LibraryDatabase.execute("INSERT INTO genre VALUES (3, 'Science')");
		LibraryDatabase.execute("INSERT INTO genre VALUES (4, 'History')");
		LibraryDatabase.execute("INSERT INTO genre VALUES (5, 'Children')");

		// Member types: (name, max_checkouts, loan_period_days, fine_per_day_cents)
		LibraryDatabase.execute("INSERT INTO member_type VALUES (1, 'Regular',  5, 14, 25)");
		LibraryDatabase.execute("INSERT INTO member_type VALUES (2, 'Premium', 10, 21, 10)");
		LibraryDatabase.execute("INSERT INTO member_type VALUES (3, 'Children', 3, 14,  0)");
	}

}
