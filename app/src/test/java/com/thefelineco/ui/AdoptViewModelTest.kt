package com.thefelineco.ui

import com.thefelineco.domain.model.AgeGroup
import com.thefelineco.domain.testCat
import com.thefelineco.testing.FakeCatRepository
import com.thefelineco.testing.FakeUserRepository
import com.thefelineco.testing.MainDispatcherRule
import com.thefelineco.ui.adopt.AdoptEvent
import com.thefelineco.ui.adopt.AdoptViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AdoptViewModelTest {
    @get:Rule val mainRule = MainDispatcherRule()

    private val repo = FakeCatRepository(
        listOf(
            testCat(id = 1, name = "Mochi", ageMonths = 3, listedAt = 3),
            testCat(id = 2, name = "Luna", ageMonths = 10, listedAt = 2),
            testCat(id = 3, name = "Archie", ageMonths = 156, listedAt = 1),
        )
    )

    private val users = FakeUserRepository()

    /** Keeps the WhileSubscribed state flow active for the test. */
    private fun kotlinx.coroutines.test.TestScope.subscribe(vm: AdoptViewModel) =
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }

    private fun AdoptViewModel.names() = uiState.value.results.map { it.name }

    @Test
    fun `opening from the free banner shows only free cats`() = runTest {
        val vm = AdoptViewModel(initialFreeOnly = true, catRepository = repo, userRepository = users)
        subscribe(vm)
        assertEquals(listOf("Archie"), vm.names())
    }

    @Test
    fun `search and filters update the results`() = runTest {
        val vm = AdoptViewModel(initialFreeOnly = false, catRepository = repo, userRepository = users)
        subscribe(vm)
        assertEquals(listOf("Mochi", "Luna", "Archie"), vm.names())

        vm.onEvent(AdoptEvent.ToggleAgeGroup(AgeGroup.KITTEN))
        assertEquals(listOf("Mochi"), vm.names())

        vm.onEvent(AdoptEvent.ToggleAgeGroup(AgeGroup.KITTEN))
        vm.onEvent(AdoptEvent.SearchChanged("lu"))
        assertEquals(listOf("Luna"), vm.names())
    }

    @Test
    fun `clearing filters keeps the search text`() = runTest {
        val vm = AdoptViewModel(initialFreeOnly = false, catRepository = repo, userRepository = users)
        subscribe(vm)
        vm.onEvent(AdoptEvent.SearchChanged("a"))
        vm.onEvent(AdoptEvent.ToggleFreeOnly)
        vm.onEvent(AdoptEvent.ClearFilters)
        assertEquals("a", vm.uiState.value.query.text)
        assertEquals(0, vm.uiState.value.query.activeFilterCount)
    }

    @Test
    fun `new listings appear without reloading`() = runTest {
        val vm = AdoptViewModel(initialFreeOnly = false, catRepository = repo, userRepository = users)
        subscribe(vm)
        repo.cats.value = repo.cats.value + testCat(id = 4, name = "Nala", listedAt = 9)
        assertEquals("Nala", vm.names().first())
        assertEquals(4, vm.uiState.value.totalCount)
    }

    @Test
    fun `hearting a cat adds it to favourites and the favourites filter`() = runTest {
        val vm = AdoptViewModel(initialFreeOnly = false, catRepository = repo, userRepository = users)
        subscribe(vm)
        vm.onEvent(AdoptEvent.ToggleFavourite(2))
        assertEquals(setOf(2L), vm.uiState.value.favourites)

        vm.onEvent(AdoptEvent.ToggleFavouritesOnly)
        assertEquals(listOf("Luna"), vm.names())

        vm.onEvent(AdoptEvent.ToggleFavourite(2))
        assertEquals(emptyList<String>(), vm.names())
    }
}
