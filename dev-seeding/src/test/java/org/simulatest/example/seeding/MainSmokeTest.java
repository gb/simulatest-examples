package org.simulatest.example.seeding;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exercises both modes end-to-end from a JUnit test so CI proves the
 * main-driven seeding story compiles, runs, and rolls back correctly.
 */
class MainSmokeTest {

	private PrintStream originalOut;
	private ByteArrayOutputStream captured;

	@BeforeEach
	void captureStdout() {
		originalOut = System.out;
		captured = new ByteArrayOutputStream();
		System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
	}

	@AfterEach
	void restoreStdout() throws Exception {
		System.setOut(originalOut);
		// Delete the H2 file so repeat runs start clean.
		Path mv = Path.of("target", "dev-seed.mv.db");
		Path trace = Path.of("target", "dev-seed.trace.db");
		Files.deleteIfExists(mv);
		Files.deleteIfExists(trace);
	}

	@Test
	void seedPopulatesTheDatabase() {
		Main.main(new String[]{"seed"});

		String out = captured.toString(StandardCharsets.UTF_8);
		assertTrue(out.contains("SEEDED"),       out);
		assertTrue(out.contains("companies:    2"), out);
		assertTrue(out.contains("departments:  4"), out);
		assertTrue(out.contains("employees:    10"), out);
	}

	@Test
	void whatIfShowsChangesInsideTheLevelAndRollsThemBack() {
		Main.main(new String[]{"whatif"});

		String out = captured.toString(StandardCharsets.UTF_8);
		// Baseline seeded, 2/4/10.
		int baselineCompanies = countUnder("== BASELINE (persistent) ==", "companies:", out);
		// Inside the level, Harbor Logistics and its subtree are gone.
		int insideCompanies = countUnder("== INSIDE LEVEL (after risky delete) ==", "companies:", out);
		int insideEmployees = countUnder("== INSIDE LEVEL (after risky delete) ==", "employees:", out);
		// After rollback, everything is back.
		int afterCompanies = countUnder("== AFTER ROLLBACK (raw datasource) ==", "companies:", out);
		int afterEmployees = countUnder("== AFTER ROLLBACK (raw datasource) ==", "employees:", out);

		assertEquals(2,  baselineCompanies, "baseline seed");
		assertEquals(1,  insideCompanies,   "inside the level, one company deleted");
		assertEquals(5,  insideEmployees,   "inside the level, five employees left (Nimbus only)");
		assertEquals(2,  afterCompanies,    "after rollback, baseline restored");
		assertEquals(10, afterEmployees,    "after rollback, baseline restored");
	}

	/** Finds {@code headerLine} then the next line starting with {@code prefix} and returns its int. */
	private static int countUnder(String headerLine, String prefix, String output) {
		String[] lines = output.split("\\R");
		boolean inSection = false;
		for (String line : lines) {
			if (line.contains(headerLine)) inSection = true;
			else if (inSection && line.trim().startsWith(prefix)) {
				return Integer.parseInt(line.trim().substring(prefix.length()).trim());
			}
		}
		throw new AssertionError("did not find '" + prefix + "' under header '" + headerLine + "'\n" + output);
	}

}
