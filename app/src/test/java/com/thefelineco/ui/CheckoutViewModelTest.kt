package com.thefelineco.ui

import com.thefelineco.domain.model.CartItem
import com.thefelineco.domain.model.DeliveryMethod
import com.thefelineco.testing.FakeShopRepository
import com.thefelineco.testing.FakeUserRepository
import com.thefelineco.testing.MainDispatcherRule
import com.thefelineco.testing.testUser
import com.thefelineco.ui.checkout.CheckoutEvent
import com.thefelineco.ui.checkout.CheckoutField
import com.thefelineco.ui.checkout.CheckoutViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CheckoutViewModelTest {
    @get:Rule val mainRule = MainDispatcherRule()

    private val items = listOf(
        CartItem(productId = 1, name = "Cat Tree", imageName = "", unitPrice = 120, quantity = 1, stock = 5),
        CartItem(productId = 2, name = "Catnip Mice", imageName = "", unitPrice = 12, quantity = 3, stock = 50),
    )
    private val shop = FakeShopRepository()
    private val users = FakeUserRepository()
    private fun viewModel() = CheckoutViewModel(items, users, shop)

    @Test
    fun `totals include the delivery method`() {
        val vm = viewModel()
        assertEquals(156, vm.uiState.value.subtotal)
        vm.onEvent(CheckoutEvent.MethodSelected(DeliveryMethod.EXPRESS))
        assertEquals(166, vm.uiState.value.total)
        assertEquals(testUser.credits - 166, vm.uiState.value.balanceAfter)
    }

    @Test
    fun `delivery needs a full address`() {
        val vm = viewModel()
        vm.onEvent(CheckoutEvent.Submit)
        val errors = vm.uiState.value.errors
        assertEquals(setOf(CheckoutField.ADDRESS, CheckoutField.SUBURB, CheckoutField.POSTCODE), errors.keys)
        assertTrue(shop.placedOrders.isEmpty())
    }

    @Test
    fun `click and collect needs no address`() {
        val vm = viewModel()
        vm.onEvent(CheckoutEvent.MethodSelected(DeliveryMethod.CLICK_AND_COLLECT))
        assertFalse(vm.uiState.value.needsAddress)
        vm.onEvent(CheckoutEvent.Submit)
        assertNotNull(vm.uiState.value.placed)
        assertEquals(CheckoutViewModel.STORE_ADDRESS, shop.placedOrders.single().address)
    }

    @Test
    fun `postcode only accepts four digits`() {
        val vm = viewModel()
        vm.onEvent(CheckoutEvent.PostcodeChanged("30a0012"))
        assertEquals("3000", vm.uiState.value.postcode)
    }

    @Test
    fun `order is blocked when the balance is too low`() {
        users.user.value = testUser.copy(credits = 100)
        val vm = viewModel()
        vm.onEvent(CheckoutEvent.MethodSelected(DeliveryMethod.CLICK_AND_COLLECT))
        vm.onEvent(CheckoutEvent.Submit)
        assertNull(vm.uiState.value.placed)
        assertNotNull(vm.uiState.value.formError)
    }

    @Test
    fun `a valid delivery order is placed`() {
        val vm = viewModel()
        vm.onEvent(CheckoutEvent.AddressChanged("12 Whisker Lane"))
        vm.onEvent(CheckoutEvent.SuburbChanged("Carlton"))
        vm.onEvent(CheckoutEvent.PostcodeChanged("3053"))
        vm.onEvent(CheckoutEvent.Submit)
        val order = vm.uiState.value.placed
        assertNotNull(order)
        assertEquals("12 Whisker Lane, Carlton 3053", order!!.address)
        assertEquals(testUser.fullName, order.deliveryName)
    }
}
