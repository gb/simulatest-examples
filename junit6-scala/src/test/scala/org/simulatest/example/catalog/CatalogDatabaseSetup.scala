package org.simulatest.example.catalog

import javax.sql.DataSource

import org.h2.jdbcx.JdbcDataSource
import org.simulatest.environment.bootstrap.SimulatestDatabaseSetup
import scalikejdbc.{ConnectionPool, DataSourceConnectionPool}

/** Hands the catalog H2 DataSource to Simulatest, creates the schema, and
 *  registers ScalikeJDBC's default connection pool to pull from the wrapped
 *  DataSource. After this runs, every ScalikeJDBC `DB.localTx` in
 *  environments and tests talks to the savepoint-aware connection.
 */
class CatalogDatabaseSetup extends SimulatestDatabaseSetup:

	override def dataSource(): DataSource =
		val h2 = JdbcDataSource()
		h2.setURL("jdbc:h2:mem:catalog;DB_CLOSE_DELAY=-1")
		h2.setUser("sa")
		h2

	override def setupSchema(dataSource: DataSource): Unit =
		CatalogSchema.create(dataSource)
		ConnectionPool.add(ConnectionPool.DEFAULT_NAME, DataSourceConnectionPool(dataSource))
