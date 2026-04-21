package org.simulatest.example.banking.environment;

import com.google.inject.Inject;
import org.simulatest.environment.Environment;
import org.simulatest.environment.annotation.EnvironmentParent;
import org.simulatest.example.banking.BankingDatabase;

@EnvironmentParent(CurrencyEnvironment.class)
public final class CustomersEnvironment implements Environment {

	private final BankingDatabase db;

	@Inject
	public CustomersEnvironment(BankingDatabase db) {
		this.db = db;
	}

	@Override
	public void run() {
		db.update("INSERT INTO customer VALUES (?, ?)", 1, "Alice Thompson");
		db.update("INSERT INTO customer VALUES (?, ?)", 2, "Bob Martinez");
		db.update("INSERT INTO customer VALUES (?, ?)", 3, "Carol Davis");
	}

}
