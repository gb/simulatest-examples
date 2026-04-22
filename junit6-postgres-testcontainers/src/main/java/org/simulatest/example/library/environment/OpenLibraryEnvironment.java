package org.simulatest.example.library.environment;

import org.simulatest.environment.annotation.EnvironmentParent;
import org.simulatest.environment.Environment;
import org.simulatest.example.library.LibraryDatabase;

/**
 * World-state: <b>the library has opened its doors</b>.
 *
 * <p>Three physical branches exist at real addresses. The building is
 * provisioned but nothing is happening inside yet — no books on shelves,
 * no staff hired, no members enrolled. A municipal inspector could walk
 * through and sign off that the library <em>exists</em>; that's the
 * baseline this environment represents.
 *
 * <p>Parent: {@link ReferenceDataEnvironment} (genres, member tiers).
 * Children split the world into two independent next-states:
 * {@link StockedLibraryEnvironment} (books arrive) and
 * {@link StaffedLibraryEnvironment} (people are hired) — siblings, because
 * a library can be stocked without staff or staffed without stock.
 */
@EnvironmentParent(ReferenceDataEnvironment.class)
public final class OpenLibraryEnvironment implements Environment {

	@Override
	public void run() {
		LibraryDatabase.execute("INSERT INTO branch VALUES (1, 'Downtown Branch', '100 Main Street')");
		LibraryDatabase.execute("INSERT INTO branch VALUES (2, 'Westside Branch', '250 Oak Avenue')");
		LibraryDatabase.execute("INSERT INTO branch VALUES (3, 'Eastville Branch', '75 Elm Road')");
	}

}
