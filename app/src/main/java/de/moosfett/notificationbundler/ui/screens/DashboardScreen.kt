package de.moosfett.notificationbundler.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import de.moosfett.notificationbundler.R

/**
 * Dashboard showing quick status information and actions.
 * Business logic lives in ViewModels; this composable exposes events via [DashboardEvent].
 */
@Composable
fun DashboardScreen(
    state: DashboardState = DashboardState(),
    onEvent: (DashboardEvent) -> Unit = {}
) {
    val context = LocalContext.current
    val listenerEnabled = NotificationManagerCompat
        .getEnabledListenerPackages(context)
        .contains(context.packageName)

    var notificationsGranted by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < 33 ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationsGranted = granted
        if (granted) onEvent(DashboardEvent.PostNotificationsGranted)
    }

    when {
        !listenerEnabled -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = stringResource(id = R.string.listener_not_enabled))
                Spacer(Modifier.height(12.dp))
                Button(onClick = {
                    context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                }) {
                    Text(text = stringResource(id = R.string.open_settings))
                }
            }
        }

        !notificationsGranted -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = stringResource(id = R.string.notifications_not_allowed))
                Spacer(Modifier.height(12.dp))
                Button(onClick = {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }) {
                    Text(text = stringResource(id = R.string.grant_permission))
                }
            }
        }

        else -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.next_delivery_at, state.nextDeliveryTime),
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = { onEvent(DashboardEvent.DeliverNow) }) {
                        Text(text = stringResource(id = R.string.deliver_now))
                    }
                    Button(onClick = { onEvent(DashboardEvent.Snooze15m) }) {
                        Text(text = stringResource(id = R.string.snooze_15m))
                    }
                    Button(onClick = { onEvent(DashboardEvent.Skip) }) {
                        Text(text = stringResource(id = R.string.skip))
                    }
                }
                Spacer(Modifier.height(24.dp))
                Text(
                    text = stringResource(
                        id = R.string.status_counters,
                        state.todayCount,
                        state.pendingCount,
                        state.criticalCount
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/** UI state for the dashboard. */
data class DashboardState(
    val nextDeliveryTime: String = "--:--",
    val todayCount: Int = 0,
    val pendingCount: Int = 0,
    val criticalCount: Int = 0
)

/**
 * User intents originating from the dashboard.
 * ViewModels can react to these and perform work.
 */
sealed interface DashboardEvent {
    data object DeliverNow : DashboardEvent
    data object Snooze15m : DashboardEvent
    data object Skip : DashboardEvent
    data object PostNotificationsGranted : DashboardEvent
}
