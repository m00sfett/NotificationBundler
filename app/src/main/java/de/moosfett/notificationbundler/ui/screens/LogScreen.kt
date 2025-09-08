package de.moosfett.notificationbundler.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.moosfett.notificationbundler.R
import de.moosfett.notificationbundler.log.LogEntry
import de.moosfett.notificationbundler.log.LogViewModel
import de.moosfett.notificationbundler.log.LogViewModelFactory

/**
 * Shows past delivery runs.
 */
@Composable
fun LogScreen() {
    val vm: LogViewModel = viewModel(factory = LogViewModelFactory(LocalContext.current))
    val state by vm.state.collectAsState()
    var showConfirm by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            if (state.entries.isNotEmpty()) {
                FloatingActionButton(onClick = { showConfirm = true }) {
                    Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.clear_log))
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(text = stringResource(id = R.string.log), style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(12.dp))
            if (state.entries.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = stringResource(R.string.no_log_entries))
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.entries, key = { it.id }) { entry ->
                        LogRow(entry)
                    }
                }
            }
        }
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            confirmButton = {
                TextButton(onClick = { showConfirm = false; vm.clear() }) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            text = { Text(text = stringResource(R.string.clear_log_confirm)) }
        )
    }
}

@Composable
private fun LogRow(entry: LogEntry) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = entry.time)
            Text(text = stringResource(R.string.log_count, entry.count))
        }
    }
}
