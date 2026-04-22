package org.simulatest.example.catalog.environment

import org.simulatest.environment.Environment
import org.simulatest.environment.annotation.EnvironmentParent
import org.simulatest.example.catalog.CatalogDb

/** World-state: '''the catalog is stocked and browsable'''.
 *
 *  Seven products populate the three categories at realistic spreads — three
 *  books, two electronics, two homeware items. Enough variety that a search,
 *  filter, or cart test sees something meaningful, not a single demo row.
 *
 *  Parent: [[OpenCatalogEnvironment]].
 */
@EnvironmentParent(classOf[OpenCatalogEnvironment])
class StockedCatalogEnvironment extends Environment:

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
