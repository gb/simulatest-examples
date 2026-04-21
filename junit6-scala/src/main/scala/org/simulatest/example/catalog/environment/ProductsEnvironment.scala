package org.simulatest.example.catalog.environment

import org.simulatest.environment.Environment
import org.simulatest.environment.annotation.EnvironmentParent
import org.simulatest.example.catalog.CatalogDb

@EnvironmentParent(classOf[CategoriesEnvironment])
class ProductsEnvironment extends Environment:

	override def run(): Unit =
		// Books
		CatalogDb.insertProduct(1, 1, "The Scala Way",            2_995L)
		CatalogDb.insertProduct(2, 1, "Functional Effect Essays", 3_495L)
		CatalogDb.insertProduct(3, 1, "A History of Algorithms",  4_200L)
		// Electronics
		CatalogDb.insertProduct(4, 2, "USB-C Hub",                4_999L)
		CatalogDb.insertProduct(5, 2, "Desk Lamp",                3_500L)
		// Homeware
		CatalogDb.insertProduct(6, 3, "Ceramic Mug",              1_200L)
		CatalogDb.insertProduct(7, 3, "Linen Tablecloth",         3_750L)
