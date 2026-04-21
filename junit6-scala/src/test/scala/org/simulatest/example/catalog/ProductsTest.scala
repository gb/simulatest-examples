package org.simulatest.example.catalog

import org.junit.jupiter.api.{Assertions, Test}
import org.simulatest.environment.annotation.UseEnvironment
import org.simulatest.example.catalog.environment.ProductsEnvironment

@UseEnvironment(classOf[ProductsEnvironment])
class ProductsTest:

	@Test def sevenProductsExist(): Unit =
		Assertions.assertEquals(7, CatalogDb.productCount())

	@Test def catalogTotalPriceIsConsistent(): Unit =
		// 2995 + 3495 + 4200 + 4999 + 3500 + 1200 + 3750 = 24139 cents
		Assertions.assertEquals(24_139L, CatalogDb.totalCatalogPriceCents())

	@Test def threeBooksInBooksCategory(): Unit =
		Assertions.assertEquals(3, CatalogDb.productCountInCategory(1))

	@Test def updateProductPrice(): Unit =
		Assertions.assertEquals(2_995L, CatalogDb.productPrice(1))

		CatalogDb.updateProductPrice(1, 2_495L)

		Assertions.assertEquals(2_495L, CatalogDb.productPrice(1))

	@Test def priceIsBackToOriginal(): Unit =
		// Rollback check for updateProductPrice.
		Assertions.assertEquals(2_995L, CatalogDb.productPrice(1))

	@Test def deleteAllElectronics(): Unit =
		Assertions.assertEquals(2, CatalogDb.productCountInCategory(2))

		CatalogDb.deleteProductsByCategory(2)

		Assertions.assertEquals(0, CatalogDb.productCountInCategory(2))

	@Test def electronicsAreBack(): Unit =
		// Rollback check for deleteAllElectronics.
		Assertions.assertEquals(2, CatalogDb.productCountInCategory(2))

	@Test def markProductOutOfStock(): Unit =
		Assertions.assertTrue(CatalogDb.productInStock(4))

		CatalogDb.markProductInStock(4, false)

		Assertions.assertFalse(CatalogDb.productInStock(4))

	@Test def usbHubIsInStockAgain(): Unit =
		Assertions.assertTrue(CatalogDb.productInStock(4))
