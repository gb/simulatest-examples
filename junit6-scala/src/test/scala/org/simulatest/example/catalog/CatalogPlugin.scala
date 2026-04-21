package org.simulatest.example.catalog

import java.util

import org.h2.jdbcx.JdbcDataSource
import org.simulatest.environment.plugin.SimulatestPlugin
import org.simulatest.insistencelayer.InsistenceLayerFactory
import scalikejdbc.{ConnectionPool, DataSourceConnectionPool}

/** Starts H2, wires the Insistence Layer in front of it, then registers
 *  ScalikeJDBC's default connection pool to pull from the wrapped
 *  DataSource. After this runs, every ScalikeJDBC `DB.localTx` in
 *  environments and tests talks to the savepoint-aware connection.
 */
class CatalogPlugin extends SimulatestPlugin:

	override def initialize(testClasses: util.Collection[Class[_]]): Unit =
		val h2 = JdbcDataSource()
		h2.setURL("jdbc:h2:mem:catalog;DB_CLOSE_DELAY=-1")
		h2.setUser("sa")

		InsistenceLayerFactory.configure(h2)
		val wrapped = InsistenceLayerFactory.requireDataSource()

		CatalogSchema.create(wrapped)
		ConnectionPool.add(ConnectionPool.DEFAULT_NAME, DataSourceConnectionPool(wrapped))
