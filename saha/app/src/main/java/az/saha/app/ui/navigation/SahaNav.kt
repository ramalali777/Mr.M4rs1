package az.saha.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import az.saha.app.data.repository.UserSession
import az.saha.app.di.AppContainer
import az.saha.app.ui.history.HistoryScreen
import az.saha.app.ui.history.HistoryViewModel
import az.saha.app.ui.map.MapScreen
import az.saha.app.ui.map.MapViewModel
import az.saha.app.ui.profile.ProfileScreen
import az.saha.app.ui.theme.Clay
import az.saha.app.ui.theme.Ink
import az.saha.app.ui.theme.Olive

private enum class Dest(val route: String, val label: String, val icon: ImageVector) {
    Map("map", "Xəritə", Icons.Outlined.Map),
    History("history", "Ölçülər", Icons.Outlined.History),
    Profile("profile", "Profil", Icons.Outlined.Person)
}

@Composable
fun SahaMainNav(
    container: AppContainer,
    session: UserSession,
    onSignOut: () -> Unit
) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val current = backStack?.destination?.route

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            NavigationBar(containerColor = Ink, contentColor = Color.White) {
                Dest.entries.forEach { dest ->
                    NavigationBarItem(
                        selected = current == dest.route,
                        onClick = {
                            navController.navigate(dest.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(dest.icon, contentDescription = dest.label) },
                        label = { Text(dest.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Clay,
                            selectedTextColor = Clay,
                            unselectedIconColor = Color.White.copy(alpha = 0.7f),
                            unselectedTextColor = Color.White.copy(alpha = 0.7f),
                            indicatorColor = Olive.copy(alpha = 0.35f)
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Dest.Map.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Dest.Map.route) {
                val vm: MapViewModel = viewModel(
                    factory = MapViewModel.factory(
                        container.measurementRepository,
                        container.locationTracker
                    ) { session.userId }
                )
                MapScreen(vm)
            }
            composable(Dest.History.route) {
                val vm: HistoryViewModel = viewModel(
                    factory = HistoryViewModel.factory(
                        container.measurementRepository,
                        isGuest = session.isGuest
                    ) { session.userId }
                )
                HistoryScreen(vm)
            }
            composable(Dest.Profile.route) {
                ProfileScreen(session = session, onSignOut = onSignOut)
            }
        }
    }
}
