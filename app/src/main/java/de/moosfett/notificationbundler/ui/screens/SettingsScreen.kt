package de.moosfett.notificationbundler.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.moosfett.notificationbundler.R
import de.moosfett.notificationbundler.settings.SettingsViewModel
import de.moosfett.notificationbundler.settings.SettingsViewModelFactory

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(context)
    )
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
    ) {
        Text(
            text = stringResource(id = R.string.settings),
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = stringResource(id = R.string.include_ongoing))
            Switch(
                checked = state.includeOngoing,
                onCheckedChange = { viewModel.setIncludeOngoing(it) }
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = stringResource(id = R.string.include_low_importance))
            Switch(
                checked = state.includeLowImportance,
                onCheckedChange = { viewModel.setIncludeLowImportance(it) }
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(text = stringResource(id = R.string.retention_days, state.retentionDays))
        Slider(
            value = state.retentionDays.toFloat(),
            onValueChange = { viewModel.setRetentionDays(it.toInt()) },
            valueRange = 1f..90f,
            steps = 88
        )
    }
}
