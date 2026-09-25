package com.thefelineco.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.thefelineco.domain.model.User
import com.thefelineco.ui.common.LocalSnackbarHostState
import com.thefelineco.ui.common.isWideLayout

/**
 * The signed-in app frame: a navigation rail on tablets or a bottom bar on phones, the NavHost,
 * and an app-wide snackbar. Customers and admins see different destinations.
 */
@Composable
fun FelineAppShell(user: User, basketCount: Int, onLogout: () -> Unit) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val destinations = if (user.isAdmin) TopLevelDestination.admin else TopLevelDestination.customer
    val wide = isWideLayout()
    val snackbarHostState = remember { SnackbarHostState() }

    val colors = MaterialTheme.colorScheme
    val itemColors = NavigationSuiteDefaults.itemColors(
        navigationBarItemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = colors.onPrimary,
            selectedTextColor = colors.primary,
            indicatorColor = colors.primary,
            unselectedIconColor = colors.onSurfaceVariant,
            unselectedTextColor = colors.onSurfaceVariant,
        ),
        navigationRailItemColors = NavigationRailItemDefaults.colors(
            selectedIconColor = colors.onPrimary,
            selectedTextColor = colors.primary,
            indicatorColor = colors.primary,
            unselectedIconColor = colors.onSurfaceVariant,
            unselectedTextColor = colors.onSurfaceVariant,
        ),
    )

    NavigationSuiteScaffold(
        layoutType = if (wide) NavigationSuiteType.NavigationRail else NavigationSuiteType.NavigationBar,
        navigationSuiteColors = NavigationSuiteDefaults.colors(
            navigationBarContainerColor = colors.surfaceContainerLow,
            navigationRailContainerColor = colors.surfaceContainerLow,
        ),
        containerColor = colors.background,
        navigationSuiteItems = {
            destinations.forEach { destination ->
                val selected = currentDestination?.hierarchy?.any { it.hasRoute(destination.routeClass) } == true
                item(
                    selected = selected,
                    onClick = { navController.navigateToTopLevel(destination.route) },
                    icon = {
                        val icon = if (selected) destination.selectedIcon else destination.unselectedIcon
                        if (destination == TopLevelDestination.BASKET && basketCount > 0) {
                            BadgedBox(badge = { Badge { Text(basketCount.toString()) } }) {
                                Icon(icon, contentDescription = null)
                            }
                        } else {
                            Icon(icon, contentDescription = null)
                        }
                    },
                    label = { Text(destination.label) },
                    colors = itemColors,
                )
            }
        },
    ) {
        CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
            Box(
                Modifier
                    .fillMaxSize()
                    // The bottom bar already handles the system navigation inset. The rail does not.
                    .then(
                        if (wide) Modifier.windowInsetsPadding(
                            WindowInsets.navigationBars.only(WindowInsetsSides.Bottom + WindowInsetsSides.End)
                        ) else Modifier
                    )
            ) {
                FelineNavHost(
                    navController = navController,
                    user = user,
                    onLogout = onLogout,
                )
                SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter).padding(16.dp))
            }
        }
    }
}

/**
 * Opens a top-level destination the standard way: one copy on the back stack, with each tab's
 * scroll and screen state saved and restored.
 */
fun NavHostController.navigateToTopLevel(route: Any) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
