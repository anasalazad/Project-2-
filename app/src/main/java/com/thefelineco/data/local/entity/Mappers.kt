package com.thefelineco.data.local.entity

import com.thefelineco.domain.model.Booking
import com.thefelineco.domain.model.CartItem
import com.thefelineco.domain.model.Cat
import com.thefelineco.domain.model.CreditTransaction
import com.thefelineco.domain.model.Order
import com.thefelineco.domain.model.Product
import com.thefelineco.domain.model.User

// Extension functions that convert between Room entities and domain models.

fun UserEntity.toDomain() = User(id, fullName, email, role, credits)

fun CatEntity.toDomain() = Cat(
    id = id, name = name, breed = breed, ageMonths = ageMonths, sex = sex, coat = coat,
    colour = colour, weightKg = weightKg, personality = personality, description = description,
    imageName = imageName, goodWithKids = goodWithKids, goodWithCats = goodWithCats,
    goodWithDogs = goodWithDogs, indoorOnly = indoorOnly, vaccinated = vaccinated,
    desexed = desexed, microchipped = microchipped, specialNeeds = specialNeeds,
    status = status, listedAt = listedAt,
)

fun Cat.toEntity() = CatEntity(
    id = id, name = name.trim(), breed = breed.trim(), ageMonths = ageMonths, sex = sex, coat = coat,
    colour = colour.trim(), weightKg = weightKg, personality = personality, description = description.trim(),
    imageName = imageName.trim(), goodWithKids = goodWithKids, goodWithCats = goodWithCats,
    goodWithDogs = goodWithDogs, indoorOnly = indoorOnly, vaccinated = vaccinated,
    desexed = desexed, microchipped = microchipped, specialNeeds = specialNeeds?.trim()?.ifBlank { null },
    status = status, listedAt = listedAt,
)

fun BookingEntity.toDomain() = Booking(
    id = id, userId = userId, catId = catId, catName = catName, catImageName = catImageName,
    dateEpochDay = dateEpochDay, timeSlot = timeSlot, fullName = fullName, email = email,
    phone = phone, homeType = homeType, hasOtherPets = hasOtherPets, hasChildren = hasChildren,
    notes = notes, feeCredits = feeCredits, status = status, createdAt = createdAt,
)

fun Booking.toEntity() = BookingEntity(
    id = id, userId = userId, catId = catId, catName = catName, catImageName = catImageName,
    dateEpochDay = dateEpochDay, timeSlot = timeSlot, fullName = fullName.trim(), email = email.trim(),
    phone = phone.trim(), homeType = homeType, hasOtherPets = hasOtherPets, hasChildren = hasChildren,
    notes = notes.trim(), feeCredits = feeCredits, status = status, createdAt = createdAt,
)

fun ProductEntity.toDomain() = Product(id, name, brand, category, priceCredits, description, imageName, stock)

fun Product.toEntity(sortOrder: Int = 0) =
    ProductEntity(id, name.trim(), brand.trim(), category, priceCredits, description.trim(), imageName.trim(), stock, sortOrder)

fun CartLine.toDomain() = CartItem(productId, name, imageName, unitPrice, quantity, stock)

fun OrderWithItems.toDomain() = Order(
    id = order.id,
    userId = order.userId,
    items = items.map { CartItem(it.productId, it.name, it.imageName, it.unitPrice, it.quantity, stock = 0) },
    subtotal = order.subtotal,
    deliveryMethod = order.deliveryMethod,
    deliveryName = order.deliveryName,
    address = order.address,
    createdAt = order.createdAt,
)

fun CreditTransactionEntity.toDomain() = CreditTransaction(id, userId, amount, type, description, createdAt)
