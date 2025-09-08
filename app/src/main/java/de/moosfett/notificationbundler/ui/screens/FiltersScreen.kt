package de.moosfett.notificationbundler.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.moosfett.notificationbundler.R

/**
 * Screen for managing notification filters. Purely UI with state and events.
 */
@Composable
fun FiltersScreen(
    state: FiltersState = FiltersState(),
    onEvent: (FiltersEvent) -> Unit = {}
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { onEvent(FiltersEvent.AddClicked) }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(id = R.string.add_filter))
            }
        }
    ) { padding ->
        if (state.rules.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = stringResource(id = R.string.no_filters))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.rules) { rule ->
                    FilterRow(rule = rule, onRemove = { onEvent(FiltersEvent.RemoveRule(rule.id)) })
                }
            }
        }
        if (state.showEditor) {
            FilterEditor(state = state.editor, packages = state.packages, onEvent = onEvent)
        }
        if (state.showPackagePicker) {
            PackagePicker(packages = state.packages, onSelect = {
                onEvent(FiltersEvent.PackageSelected(it))
            }, onDismiss = { onEvent(FiltersEvent.PackagePickerDismissed) })
        }
    }
}

@Composable
private fun FilterRow(rule: FilterRule, onRemove: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = rule.packageLabel, style = MaterialTheme.typography.bodyLarge)
                if (rule.channel.isNotBlank()) {
                    Text(text = stringResource(id = R.string.channel_label) + ": " + rule.channel, style = MaterialTheme.typography.bodySmall)
                }
                if (rule.keyword.isNotBlank()) {
                    Text(text = stringResource(id = R.string.keyword_label) + ": " + rule.keyword, style = MaterialTheme.typography.bodySmall)
                }
                Text(text = when (rule.action) {
                    FilterAction.Critical -> stringResource(id = R.string.mark_critical)
                    FilterAction.Exclude -> stringResource(id = R.string.exclude)
                }, style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(id = R.string.remove))
            }
        }
    }
}

@Composable
private fun FilterEditor(
    state: FilterEditorState,
    packages: List<PackageOption>,
    onEvent: (FiltersEvent) -> Unit
) {
    AlertDialog(
        onDismissRequest = { onEvent(FiltersEvent.CancelEdit) },
        confirmButton = {
            TextButton(onClick = { onEvent(FiltersEvent.SaveClicked) }) {
                Text(text = stringResource(id = R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = { onEvent(FiltersEvent.CancelEdit) }) {
                Text(text = stringResource(id = R.string.cancel))
            }
        },
        title = { Text(text = stringResource(id = R.string.filters)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.packageLabel,
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    label = { Text(stringResource(id = R.string.package_label)) },
                    trailingIcon = {
                        IconButton(onClick = { onEvent(FiltersEvent.PackageFieldClicked) }) {
                            Icon(Icons.Default.Add, contentDescription = stringResource(id = R.string.select_package))
                        }
                    }
                )
                OutlinedTextField(
                    value = state.channel,
                    onValueChange = { onEvent(FiltersEvent.ChannelChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(id = R.string.channel_label)) }
                )
                OutlinedTextField(
                    value = state.keyword,
                    onValueChange = { onEvent(FiltersEvent.KeywordChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(id = R.string.keyword_label)) }
                )
                Text(text = stringResource(id = R.string.action_label))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = state.action == FilterAction.Critical,
                        onClick = { onEvent(FiltersEvent.ActionChanged(FilterAction.Critical)) }
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(text = stringResource(id = R.string.mark_critical))
                    Spacer(Modifier.width(16.dp))
                    RadioButton(
                        selected = state.action == FilterAction.Exclude,
                        onClick = { onEvent(FiltersEvent.ActionChanged(FilterAction.Exclude)) }
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(text = stringResource(id = R.string.exclude))
                }
            }
        }
    )
}

@Composable
private fun PackagePicker(
    packages: List<PackageOption>,
    onSelect: (PackageOption) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = { Text(text = stringResource(id = R.string.select_package)) },
        text = {
            if (packages.isEmpty()) {
                Text(text = "-")
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    items(packages) { pkg ->
                        TextButton(onClick = { onSelect(pkg); onDismiss() }) {
                            Text(text = pkg.label)
                        }
                    }
                }
            }
        }
    )
}

/** UI state for the Filters screen. */
data class FiltersState(
    val rules: List<FilterRule> = emptyList(),
    val packages: List<PackageOption> = emptyList(),
    val showEditor: Boolean = false,
    val editor: FilterEditorState = FilterEditorState(),
    val showPackagePicker: Boolean = false
)

/** A single filter rule displayed on screen. */
data class FilterRule(
    val id: Long,
    val packageLabel: String,
    val channel: String,
    val keyword: String,
    val action: FilterAction
)

/** Editor state while creating or editing a rule. */
data class FilterEditorState(
    val packageLabel: String = "",
    val channel: String = "",
    val keyword: String = "",
    val action: FilterAction = FilterAction.Exclude
)

/** Selection of an installed package. */
data class PackageOption(val id: String, val label: String)

/** Possible filter actions. */
enum class FilterAction { Critical, Exclude }

/** User intents from the Filters screen. */
sealed interface FiltersEvent {
    data object AddClicked : FiltersEvent
    data object CancelEdit : FiltersEvent
    data object SaveClicked : FiltersEvent
    data class RemoveRule(val id: Long) : FiltersEvent
    data object PackageFieldClicked : FiltersEvent
    data class PackageSelected(val pkg: PackageOption) : FiltersEvent
    data object PackagePickerDismissed : FiltersEvent
    data class ChannelChanged(val value: String) : FiltersEvent
    data class KeywordChanged(val value: String) : FiltersEvent
    data class ActionChanged(val action: FilterAction) : FiltersEvent
}
