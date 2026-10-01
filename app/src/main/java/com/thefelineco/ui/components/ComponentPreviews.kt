package com.thefelineco.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.thefelineco.domain.model.AdoptionStatus
import com.thefelineco.domain.model.BookingStatus
import com.thefelineco.domain.model.Cat
import com.thefelineco.domain.model.CoatLength
import com.thefelineco.domain.model.Product
import com.thefelineco.domain.model.ProductCategory
import com.thefelineco.domain.model.Sex
import com.thefelineco.ui.theme.FelineTheme

/*
 * Android Studio previews for the reusable components, so they can be checked in the Design pane
 * without running the app. Photos aren't available in previews, so the branded placeholder shows.
 */

private val previewKitten = Cat(
    id = 1, name = "Mochi", breed = "Ragdoll mix", ageMonths = 3, sex = Sex.FEMALE, coat = CoatLength.LONG,
    colour = "Seal point", weightKg = 1.4, personality = listOf("Cuddly", "Gentle"), description = "",
    imageName = "cat_mochi", goodWithKids = true, goodWithCats = true, goodWithDogs = true, indoorOnly = true,
)

private val previewSenior = previewKitten.copy(
    id = 2, name = "Archie", breed = "Maine Coon", ageMonths = 156, sex = Sex.MALE, status = AdoptionStatus.PENDING,
)

private val previewProduct = Product(
    id = 1, name = "Kitten Formula Dry Food 2kg", brand = "Royal Feline", category = ProductCategory.FOOD,
    priceCredits = 60, description = "", imageName = "product_kitten_formula", stock = 3,
)

@Preview(name = "Cat cards", widthDp = 520)
@Composable
private fun CatCardPreview() {
    FelineTheme {
        Row(
            Modifier.background(MaterialTheme.colorScheme.background).padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CatCard(previewKitten, onClick = {}, modifier = Modifier.width(220.dp))
            CatCard(previewSenior, onClick = {}, modifier = Modifier.width(220.dp))
        }
    }
}

@Preview(name = "Product cards", widthDp = 460)
@Composable
private fun ProductCardPreview() {
    FelineTheme {
        Row(
            Modifier.background(MaterialTheme.colorScheme.background).padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ProductCard(previewProduct, onClick = {}, onAddToBasket = {}, modifier = Modifier.width(200.dp))
            ProductCard(previewProduct.copy(stock = 0), onClick = {}, onAddToBasket = {}, modifier = Modifier.width(200.dp))
        }
    }
}

@Preview(name = "Badges and chips")
@Composable
private fun BadgesPreview() {
    FelineTheme {
        Column(
            Modifier.background(MaterialTheme.colorScheme.background).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FeeBadge(0)
                FeeBadge(150)
                CreditBalanceChip(400)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AdoptionStatus.entries.forEach { AdoptionStatusChip(it) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BookingStatus.entries.take(3).forEach { BookingStatusChip(it) }
            }
        }
    }
}
