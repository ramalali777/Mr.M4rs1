package az.saha.app.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import az.saha.app.R
import az.saha.app.data.repository.UserSession
import az.saha.app.di.AppContainer
import az.saha.app.ui.history.HistoryScreen
import az.saha.app.ui.history.HistoryViewModel
import az.saha.app.ui.map.MapScreen
import az.saha.app.ui.map.MapViewModel
import az.saha.app.ui.profile.ProfileScreen
import az.saha.app.ui.theme.Clay
import az.saha.app.ui.theme.Ink
import az.saha.app.ui.theme.Mist
import az.saha.app.ui.theme.Olive
import az.saha.app.ui.theme.OliveDeep
import kotlinx.coroutines.launch

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
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    fun openDrawer() = scope.launch { drawerState.open() }
    fun go(route: String) {
        scope.launch {
            drawerState.close()
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.fillMaxWidth(0.82f),
                drawerContainerColor = Mist
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.bg_soft_hills),
                        contentDescription = null,
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0x661A2420), Color(0xCC1A2420))
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(20.dp)
                    ) {
                        Text(
                            "SAHƏ",
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            session.displayName,
                            color = Clay,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                Dest.entries.forEach { dest ->
                    NavigationDrawerItem(
                        label = { Text(dest.label) },
                        selected = current == dest.route,
                        onClick = { go(dest.route) },
                        icon = { Icon(dest.icon, contentDescription = null) },
                        modifier = Modifier.padding(horizontal = 12.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = Olive.copy(alpha = 0.18f),
                            selectedIconColor = OliveDeep,
                            selectedTextColor = OliveDeep
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp))
                NavigationDrawerItem(
                    label = { Text("Çıxış") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            onSignOut()
                        }
                    },
                    icon = { Icon(Icons.Outlined.Logout, contentDescription = null) },
                    modifier = Modifier.padding(horizontal = 12.dp),
                    shape = RoundedCornerShape(14.dp)
                )
            }
        }
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                NavigationBar(containerColor = Ink, contentColor = Color.White) {
                    Dest.entries.forEach { dest ->
                        NavigationBarItem(
                            selected = current == dest.route,
                            onClick = { go(dest.route) },
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
                    MapScreen(vm, onOpenMenu = { openDrawer() })
                }
                composable(Dest.History.route) {
                    val vm: HistoryViewModel = viewModel(
                        factory = HistoryViewModel.factory(
                            container.measurementRepository,
                            isGuest = session.isGuest
                        ) { session.userId }
                    )
                    HistoryScreen(
                        vm,
                        onOpenMenu = { openDrawer() },
                        onNewMeasurement = { go(Dest.Map.route) }
                    )
                }
                composable(Dest.Profile.route) {
                    ProfileScreen(
                        session = session,
                        onSignOut = onSignOut,
                        onOpenMenu = { openDrawer() }
                    )
                }
            }
        }
    }
}

@Composable
fun SaheMenuButton(onClick: () -> Unit, tint: Color = Color.White) {
    IconButton(onClick = onClick) {
        Icon(Icons.Outlined.Menu, contentDescription = "Menyu", tint = tint)
    }
}
