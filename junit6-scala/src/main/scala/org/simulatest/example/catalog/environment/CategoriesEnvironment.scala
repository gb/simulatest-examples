package org.simulatest.example.catalog.environment

import org.simulatest.environment.Environment
import org.simulatest.example.catalog.CatalogDb

/** Root environment. Three categories.
 *
 *  {{{
 *    CategoriesEnvironment      ◄── ROOT
 *      └── ProductsEnvironment
 *  }}}
 */
class CategoriesEnvironment extends Environment:

	override def run(): Unit =
		CatalogDb.insertCategory(1, "Books")
		CatalogDb.insertCategory(2, "Electronics")
		CatalogDb.insertCategory(3, "Homeware")
