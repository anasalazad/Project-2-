@file:Suppress("unused", "UNUSED_PARAMETER")
package androidx.lifecycle.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
abstract class CreationExtras {
    interface Key<T>
    abstract operator fun <T> get(key: Key<T>): T?
}
class InitializerViewModelFactoryBuilder
inline fun <reified VM : ViewModel> InitializerViewModelFactoryBuilder.initializer(noinline initializer: CreationExtras.() -> VM) {}
inline fun viewModelFactory(builder: InitializerViewModelFactoryBuilder.() -> Unit): ViewModelProvider.Factory = TODO()
