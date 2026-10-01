@file:Suppress("unused", "UNUSED_PARAMETER")
package androidx.lifecycle
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.CoroutineScope
import kotlin.reflect.KClass
abstract class ViewModel {
    // Same as AndroidX: a supervisor scope on Dispatchers.Main.immediate.
    internal val scope: CoroutineScope by lazy { CoroutineScope(kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.Main.immediate) }
    protected open fun onCleared() {}
}
val ViewModel.viewModelScope: CoroutineScope get() = scope
class ViewModelProvider {
    interface Factory { fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T = TODO() }
    class AndroidViewModelFactory {
        companion object { val APPLICATION_KEY: CreationExtras.Key<android.app.Application> = object : CreationExtras.Key<android.app.Application> {} }
    }
}
class SavedStateHandle
fun CreationExtras.createSavedStateHandle(): SavedStateHandle = TODO()
