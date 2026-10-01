package com.thefelineco.ui.common

import com.thefelineco.data.repository.CatRepository
import com.thefelineco.data.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

/** The signed-in user's favourite cat ids, live. Empty when signed out. Shared by Home, Adopt and Cat Detail. */
@OptIn(ExperimentalCoroutinesApi::class)
fun favouriteIds(userRepository: UserRepository, catRepository: CatRepository): Flow<Set<Long>> =
    userRepository.currentUser.flatMapLatest { user ->
        if (user == null) flowOf(emptySet()) else catRepository.observeFavouriteIds(user.id)
    }

/** Hearts or un-hearts [catId] for the signed-in user. Returns the new state, or null if signed out. */
suspend fun toggleFavourite(userRepository: UserRepository, catRepository: CatRepository, catId: Long): Boolean? {
    val user = userRepository.currentUser.first() ?: return null
    return catRepository.toggleFavourite(user.id, catId)
}
