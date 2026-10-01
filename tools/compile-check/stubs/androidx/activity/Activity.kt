@file:Suppress("unused", "UNUSED_PARAMETER")
package androidx.activity
import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
abstract class ComponentActivity : Activity()
class SystemBarStyle private constructor() {
    companion object {
        fun dark(scrim: Int): SystemBarStyle = SystemBarStyle()
        fun light(scrim: Int, darkScrim: Int): SystemBarStyle = SystemBarStyle()
        fun auto(lightScrim: Int, darkScrim: Int): SystemBarStyle = SystemBarStyle()
    }
}
fun ComponentActivity.enableEdgeToEdge(
    statusBarStyle: SystemBarStyle = SystemBarStyle.auto(0, 0),
    navigationBarStyle: SystemBarStyle = SystemBarStyle.auto(0, 0),
) {}
inline fun <reified VM : ViewModel> ComponentActivity.viewModels(
    noinline factoryProducer: (() -> ViewModelProvider.Factory)? = null,
): Lazy<VM> = TODO()
