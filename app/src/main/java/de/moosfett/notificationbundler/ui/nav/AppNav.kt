package de.moosfett.notificationbundler.ui.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import de.moosfett.notificationbundler.ui.screens.*

sealed class Dest(val route: String, val labelRes: Int) {
    data object Dashboard : Dest("dashboard", de.moosfett.notificationbundler.R.string.dashboard)
    data object Preview : Dest("preview", de.moosfett.notificationbundler.R.string.preview)
    data object Schedules : Dest("schedules", de.moosfett.notificationbundler.R.string.schedules)
    data object Filters : Dest("filters", de.moosfett.notificationbundler.R.string.filters)
    data object Log : Dest("log", de.moosfett.notificationbundler.R.string.log)
    data object Settings : Dest("settings", de.moosfett.notificationbundler.R.string.settings)
}

@Composable
fun AppNav() {
    val nav = rememberNavController()
    val items = listOf(Dest.Dashboard, Dest.Preview, Dest.Schedules, Dest.Filters, Dest.Log, Dest.Settings)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val backStackEntry by nav.currentBackStackEntryAsState()
                val destination = backStackEntry?.destination
                items.forEach { dest ->
                    val selected = destination?.hierarchy?.any { it.route == dest.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = { nav.navigate(dest.route) { launchSingleTop = true } },
                        label = { Text(text = stringResource(dest.labelRes)) },
                        icon = { /* keep minimal */ }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(navController = nav, startDestination = Dest.Dashboard.route, modifier = Modifier.padding(padding)) {
            composable(Dest.Dashboard.route) { DashboardScreen() }
            composable(Dest.Preview.route) { PreviewScreen() }
            composable(Dest.Schedules.route) { SchedulesScreen() }
            composable(Dest.Filters.route) { FiltersScreen() }
            composable(Dest.Log.route) { LogScreen() }
            composable(Dest.Settings.route) { SettingsScreen() }
        }
    }
}
