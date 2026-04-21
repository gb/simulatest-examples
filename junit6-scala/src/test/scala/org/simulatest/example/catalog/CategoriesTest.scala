package org.simulatest.example.catalog

import org.junit.jupiter.api.{Assertions, Test}
import org.simulatest.environment.annotation.UseEnvironment
import org.simulatest.example.catalog.environment.CategoriesEnvironment

@UseEnvironment(classOf[CategoriesEnvironment])
class CategoriesTest:

	@Test def threeCategoriesExist(): Unit =
		Assertions.assertEquals(3, CatalogDb.categoryCount())

	@Test def booksCategoryIsPresent(): Unit =
		Assertions.assertEquals("Books", CatalogDb.categoryName(1))

	@Test def addFashionCategory(): Unit =
		Assertions.assertEquals(3, CatalogDb.categoryCount())

		CatalogDb.insertCategory(4, "Fashion")

		Assertions.assertEquals(4, CatalogDb.categoryCount())

	@Test def fashionIsNotThereAnymore(): Unit =
		// Isolation: addFashionCategory's insert was rolled back.
		Assertions.assertEquals(3, CatalogDb.categoryCount())

	@Test def noProductsYet(): Unit =
		Assertions.assertEquals(0, CatalogDb.productCount())
