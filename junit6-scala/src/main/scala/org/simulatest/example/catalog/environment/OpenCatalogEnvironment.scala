package org.simulatest.example.catalog.environment

import org.simulatest.environment.Environment
import org.simulatest.example.catalog.CatalogDb

/** World-state: '''the catalog structure exists but nothing is stocked'''.
 *
 *  Three categories are defined — Books, Electronics, Homeware. A merchant
 *  walking up could see where things will go, but every shelf is empty.
 *
 *  {{{
 *    OpenCatalogEnvironment           ◄── ROOT (categories only)
 *      └── StockedCatalogEnvironment        (products on the shelves)
 *  }}}
 */
class OpenCatalogEnvironment extends Environment:

	override def run(): Unit =
		CatalogDb.insertCategory(1, "Books")
		CatalogDb.insertCategory(2, "Electronics")
		CatalogDb.insertCategory(3, "Homeware")
