package org.simulatest.example.catalog

import scalikejdbc.*

/** Scalikejdbc wrappers for the catalog domain.
 *
 *  Uses `DB.autoCommit`, not `DB.localTx`. With `localTx`, ScalikeJDBC
 *  calls `commit()` at the end of each block, which `ConnectionWrapper`
 *  translates into a `USER_COMMIT` savepoint bump. JDBC's contract then
 *  cascade-releases any savepoints Simulatest created after that bump,
 *  which corrupts the level stack. `autoCommit` asks the driver for
 *  auto-commit mode (silently ignored by `ConnectionWrapper`) and never
 *  issues an explicit commit, so Simulatest's savepoints stay intact.
 */
object CatalogDb:

	def insertCategory(id: Int, name: String): Unit =
		DB.autoCommit { implicit session =>
			sql"INSERT INTO category VALUES ($id, $name)".update.apply()
		}

	def insertProduct(id: Int, categoryId: Int, name: String, priceCents: Long): Unit =
		DB.autoCommit { implicit session =>
			sql"""INSERT INTO product (id, category_id, name, price_cents)
			      VALUES ($id, $categoryId, $name, $priceCents)""".update.apply()
		}

	def updateProductPrice(productId: Int, priceCents: Long): Unit =
		DB.autoCommit { implicit session =>
			sql"UPDATE product SET price_cents = $priceCents WHERE id = $productId".update.apply()
		}

	def markProductInStock(productId: Int, inStock: Boolean): Unit =
		DB.autoCommit { implicit session =>
			sql"UPDATE product SET in_stock = $inStock WHERE id = $productId".update.apply()
		}

	def deleteProductsByCategory(categoryId: Int): Unit =
		DB.autoCommit { implicit session =>
			sql"DELETE FROM product WHERE category_id = $categoryId".update.apply()
		}

	def categoryCount(): Int =
		DB.readOnly { implicit session =>
			sql"SELECT COUNT(*) FROM category".map(_.int(1)).single.apply().getOrElse(0)
		}

	def categoryName(id: Int): String =
		DB.readOnly { implicit session =>
			sql"SELECT name FROM category WHERE id = $id".map(_.string(1)).single.apply()
				.getOrElse(throw IllegalStateException(s"no category $id"))
		}

	def productCount(): Int =
		DB.readOnly { implicit session =>
			sql"SELECT COUNT(*) FROM product".map(_.int(1)).single.apply().getOrElse(0)
		}

	def productCountInCategory(categoryId: Int): Int =
		DB.readOnly { implicit session =>
			sql"SELECT COUNT(*) FROM product WHERE category_id = $categoryId"
				.map(_.int(1)).single.apply().getOrElse(0)
		}

	def productPrice(id: Int): Long =
		DB.readOnly { implicit session =>
			sql"SELECT price_cents FROM product WHERE id = $id".map(_.long(1)).single.apply()
				.getOrElse(throw IllegalStateException(s"no product $id"))
		}

	def productInStock(id: Int): Boolean =
		DB.readOnly { implicit session =>
			sql"SELECT in_stock FROM product WHERE id = $id".map(_.boolean(1)).single.apply()
				.getOrElse(throw IllegalStateException(s"no product $id"))
		}

	def totalCatalogPriceCents(): Long =
		DB.readOnly { implicit session =>
			sql"SELECT COALESCE(SUM(price_cents), 0) FROM product".map(_.long(1)).single.apply()
				.getOrElse(0L)
		}
