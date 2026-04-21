package org.simulatest.example.catalog

import javax.sql.DataSource

object CatalogSchema:

	private val DDL = List(
		"""CREATE TABLE IF NOT EXISTS category (
		    id INT PRIMARY KEY,
		    name VARCHAR(100) NOT NULL
		  )""",
		"""CREATE TABLE IF NOT EXISTS product (
		    id INT PRIMARY KEY,
		    category_id INT NOT NULL REFERENCES category(id),
		    name VARCHAR(200) NOT NULL,
		    price_cents BIGINT NOT NULL,
		    in_stock BOOLEAN NOT NULL DEFAULT TRUE
		  )"""
	)

	def create(dataSource: DataSource): Unit =
		val conn = dataSource.getConnection()
		try
			val stmt = conn.createStatement()
			try DDL.foreach(stmt.execute)
			finally stmt.close()
		finally conn.close()
