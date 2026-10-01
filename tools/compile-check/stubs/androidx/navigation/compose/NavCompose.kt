@file:Suppress("unused", "UNUSED_PARAMETER")
package androidx.navigation.compose
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
@Composable fun rememberNavController(): NavHostController = TODO()
@Composable fun NavController.currentBackStackEntryAsState(): State<NavBackStackEntry?> = TODO()
@Composable fun NavHost(navController: NavHostController, startDestination: Any, modifier: Modifier = Modifier, builder: NavGraphBuilder.() -> Unit) {}
inline fun <reified T : Any> NavGraphBuilder.composable(noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit) {}
