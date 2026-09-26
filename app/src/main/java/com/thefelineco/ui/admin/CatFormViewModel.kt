package com.thefelineco.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thefelineco.data.repository.CatRepository
import com.thefelineco.domain.CreditRules
import com.thefelineco.domain.model.AdoptionStatus
import com.thefelineco.domain.model.Cat
import com.thefelineco.domain.model.CoatLength
import com.thefelineco.domain.model.Sex
import com.thefelineco.domain.validation.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class CatField { NAME, BREED, AGE_YEARS, AGE_MONTHS, COLOUR, WEIGHT, PERSONALITY, DESCRIPTION, IMAGE }

/** Result of a save or delete, so the screen can navigate back with a message. */
sealed interface FormOutcome {
    data class Saved(val message: String) : FormOutcome
    data class Deleted(val message: String) : FormOutcome
}

/** Everything in the add/edit cat form. Numbers are kept as text while typing and parsed on save. */
data class CatFormState(
    val isLoading: Boolean = false,
    val id: Long = 0,
    val name: String = "",
    val breed: String = "",
    val ageYears: String = "",
    val ageMonths: String = "0",
    val sex: Sex = Sex.FEMALE,
    val coat: CoatLength = CoatLength.SHORT,
    val colour: String = "",
    val weight: String = "",
    val personality: String = "",
    val description: String = "",
    val imageName: String = "",
    val goodWithKids: Boolean = true,
    val goodWithCats: Boolean = true,
    val goodWithDogs: Boolean = false,
    val indoorOnly: Boolean = true,
    val vaccinated: Boolean = true,
    val desexed: Boolean = true,
    val microchipped: Boolean = true,
    val specialNeeds: String = "",
    val status: AdoptionStatus = AdoptionStatus.AVAILABLE,
    val listedAt: Long = System.currentTimeMillis(),
    val errors: Map<CatField, String> = emptyMap(),
    val formError: String? = null,
    val isSaving: Boolean = false,
    val outcome: FormOutcome? = null,
) {
    val isNew: Boolean get() = id == 0L

    /** Total age in months, or null while the age fields are invalid. */
    val totalMonths: Int? get() {
        val years = ageYears.trim().toIntOrNull() ?: return null
        val months = ageMonths.trim().ifEmpty { "0" }.toIntOrNull() ?: return null
        return years * 12 + months
    }

    /** Live fee preview as the admin types the age. */
    val feePreview: Int? get() = totalMonths?.let(CreditRules::adoptionFee)

    val traits: List<String> get() = personality.split(',').map { it.trim() }.filter { it.isNotEmpty() }
}

/** Add a new cat (catId = 0) or edit an existing one. */
class CatFormViewModel(
    catId: Long,
    private val catRepository: CatRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CatFormState(isLoading = catId != 0L))
    val state: StateFlow<CatFormState> = _state.asStateFlow()

    init {
        if (catId != 0L) {
            viewModelScope.launch {
                val cat = catRepository.getCat(catId)
                _state.value = cat?.toFormState() ?: CatFormState(formError = "This cat no longer exists")
            }
        }
    }

    /** Applies an edit and clears the error for [field], if any. */
    fun update(field: CatField? = null, change: (CatFormState) -> CatFormState) {
        _state.update { current ->
            change(current).copy(errors = if (field == null) current.errors else current.errors - field, formError = null)
        }
    }

    internal fun validate(s: CatFormState): Map<CatField, String> = buildMap {
        Validators.name(s.name)?.let { put(CatField.NAME, it) }
        Validators.required(s.breed, "Breed")?.let { put(CatField.BREED, it) }
        Validators.intInRange(s.ageYears, "Years", 0..25)?.let { put(CatField.AGE_YEARS, it) }
        Validators.intInRange(s.ageMonths.ifBlank { "0" }, "Months", 0..11)?.let { put(CatField.AGE_MONTHS, it) }
        if (s.totalMonths == 0) put(CatField.AGE_MONTHS, "Kittens must be at least 1 month old")
        Validators.required(s.colour, "Colour")?.let { put(CatField.COLOUR, it) }
        Validators.decimalInRange(s.weight, "Weight", 0.3..15.0)?.let { put(CatField.WEIGHT, it) }
        when {
            s.traits.isEmpty() -> put(CatField.PERSONALITY, "Add at least one trait")
            s.traits.size > 5 -> put(CatField.PERSONALITY, "Keep it to 5 traits or fewer")
        }
        if (s.description.trim().length < 20) put(CatField.DESCRIPTION, "Write at least a sentence (20+ characters)")
        validateImageName(s.imageName)?.let { put(CatField.IMAGE, it) }
    }

    fun save() {
        val s = _state.value
        if (s.isSaving) return
        val errors = validate(s)
        if (errors.isNotEmpty()) {
            _state.update { it.copy(errors = errors, formError = "Please fix the highlighted fields") }
            return
        }
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            runCatching { catRepository.saveCat(s.toCat()) }
                .onSuccess {
                    val verb = if (s.isNew) "Added" else "Saved"
                    _state.update { it.copy(isSaving = false, outcome = FormOutcome.Saved("$verb ${s.name.trim()}")) }
                }
                .onFailure { e -> _state.update { it.copy(isSaving = false, formError = e.message ?: "Couldn't save") } }
        }
    }

    fun delete() {
        val s = _state.value
        viewModelScope.launch {
            catRepository.deleteCat(s.id)
                .onSuccess { _state.update { it.copy(outcome = FormOutcome.Deleted("Removed ${s.name}")) } }
                .onFailure { e -> _state.update { it.copy(formError = e.message ?: "Couldn't delete") } }
        }
    }
}

private fun Cat.toFormState() = CatFormState(
    id = id, name = name, breed = breed, ageYears = (ageMonths / 12).toString(), ageMonths = (ageMonths % 12).toString(),
    sex = sex, coat = coat, colour = colour, weight = weightKg.toString(), personality = personality.joinToString(", "),
    description = description, imageName = imageName, goodWithKids = goodWithKids, goodWithCats = goodWithCats,
    goodWithDogs = goodWithDogs, indoorOnly = indoorOnly, vaccinated = vaccinated, desexed = desexed,
    microchipped = microchipped, specialNeeds = specialNeeds.orEmpty(), status = status, listedAt = listedAt,
)

/** Only call after validation passed. */
private fun CatFormState.toCat() = Cat(
    id = id, name = name.trim(), breed = breed.trim(), ageMonths = checkNotNull(totalMonths), sex = sex, coat = coat,
    colour = colour.trim(), weightKg = weight.trim().toDouble(), personality = traits, description = description.trim(),
    imageName = imageName.trim(), goodWithKids = goodWithKids, goodWithCats = goodWithCats, goodWithDogs = goodWithDogs,
    indoorOnly = indoorOnly, vaccinated = vaccinated, desexed = desexed, microchipped = microchipped,
    specialNeeds = specialNeeds.trim().ifBlank { null }, status = status, listedAt = listedAt,
)
