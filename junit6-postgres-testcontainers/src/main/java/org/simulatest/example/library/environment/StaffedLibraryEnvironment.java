package org.simulatest.example.library.environment;

import org.simulatest.environment.annotation.EnvironmentParent;
import org.simulatest.environment.Environment;
import org.simulatest.example.library.LibraryDatabase;

/**
 * World-state: <b>the library has been staffed</b>.
 *
 * <p>Seven employees — head librarians, librarians, and assistants —
 * are on duty across the three branches. Crucially, no books exist in
 * this state and no members are enrolled; it's a staffed <em>building</em>,
 * not an operating library. This captures a real moment in a library's
 * life: the week before opening day, when the team is training but the
 * collection hasn't been delivered.
 *
 * <p>Parent: {@link OpenLibraryEnvironment}.
 * Sibling: {@link StockedLibraryEnvironment}. By the time this environment
 * runs, the entire stocked subtree (books, copies, members, loans) has
 * already been rolled back — see {@code StaffTest} for assertions that
 * exercise that sibling-subtree isolation directly.
 */
@EnvironmentParent(OpenLibraryEnvironment.class)
public final class StaffedLibraryEnvironment implements Environment {

	@Override
	public void run() {
		// 7 staff across 3 branches (id, name, role, branch_id)
		LibraryDatabase.execute("INSERT INTO staff VALUES (1, 'Margaret Chen',   'HEAD_LIBRARIAN', 1)");
		LibraryDatabase.execute("INSERT INTO staff VALUES (2, 'Robert Taylor',   'LIBRARIAN',      1)");
		LibraryDatabase.execute("INSERT INTO staff VALUES (7, 'Linda Garcia',    'ASSISTANT',      1)");
		LibraryDatabase.execute("INSERT INTO staff VALUES (3, 'Susan Park',      'HEAD_LIBRARIAN', 2)");
		LibraryDatabase.execute("INSERT INTO staff VALUES (4, 'James Wilson',    'ASSISTANT',      2)");
		LibraryDatabase.execute("INSERT INTO staff VALUES (5, 'Patricia Adams',  'HEAD_LIBRARIAN', 3)");
		LibraryDatabase.execute("INSERT INTO staff VALUES (6, 'Michael Brown',   'LIBRARIAN',      3)");
	}

}
