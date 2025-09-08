package de.moosfett.notificationbundler.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import de.moosfett.notificationbundler.R
import de.moosfett.notificationbundler.schedules.SchedulesViewModel
import de.moosfett.notificationbundler.schedules.SchedulesViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchedulesScreen() {
    val vm: SchedulesViewModel = viewModel(factory = SchedulesViewModelFactory(LocalContext.current))
    val state by vm.state.collectAsState()

    var showPicker by remember { mutableStateOf(false) }
    val pickerState = rememberTimePickerState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showPicker = true }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_time))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.schedules),
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(Modifier.height(12.dp))
            if (state.times.isEmpty()) {
                Text(text = stringResource(R.string.no_times))
            } else {
                LazyColumn {
                    items(state.times) { time ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(time)
                            IconButton(onClick = { vm.removeTime(time) }) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = stringResource(R.string.remove_time)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPicker) {
        AlertDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showPicker = false
                    vm.addTime(pickerState.hour, pickerState.minute)
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            text = { TimePicker(state = pickerState) }
        )
    }
}
