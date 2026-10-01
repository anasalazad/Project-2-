@file:Suppress("unused", "UNUSED_PARAMETER")
package androidx.navigation
import kotlin.reflect.KClass
open class NavDestination {
    val id: Int = 0
    val route: String? = null
    companion object {
        val NavDestination.hierarchy: Sequence<NavDestination> get() = TODO()
        fun <T : Any> NavDestination.hasRoute(route: KClass<T>): Boolean = TODO()
    }
}
class NavGraph : NavDestination() {
    companion object { fun NavGraph.findStartDestination(): NavDestination = TODO() }
}
class NavBackStackEntry { val destination: NavDestination get() = TODO() }
class PopUpToBuilder { var inclusive: Boolean = false; var saveState: Boolean = false }
class NavOptionsBuilder {
    var launchSingleTop: Boolean = false
    var restoreState: Boolean = false
    fun popUpTo(id: Int, popUpToBuilder: PopUpToBuilder.() -> Unit = {}) {}
}
class NavOptions
open class NavController {
    val graph: NavGraph get() = TODO()
    fun <T : Any> navigate(route: T, builder: NavOptionsBuilder.() -> Unit) {}
    fun <T : Any> navigate(route: T, navOptions: NavOptions? = null) {}
    fun popBackStack(): Boolean = true
}
class NavHostController : NavController()
class NavGraphBuilder
inline fun <reified T : Any> androidx.lifecycle.SavedStateHandle.toRoute(): T = TODO()
