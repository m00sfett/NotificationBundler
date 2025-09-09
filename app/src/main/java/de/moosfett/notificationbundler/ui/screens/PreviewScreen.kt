package de.moosfett.notificationbundler.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.moosfett.notificationbundler.R

/**
 * Preview screen showing the next bundled notification batch.
 * State and events are provided externally (ViewModel).
 */
@Composable
fun PreviewScreen(
    state: PreviewState = PreviewState(),
    onEvent: (PreviewEvent) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
    ) {
        Text(
            text = stringResource(id = R.string.preview),
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(Modifier.height(24.dp))

        if (state.lines.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = stringResource(id = R.string.no_pending_notifications))
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f, fill = true)
            ) {
                items(state.lines) { line ->
                    Text(text = line, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                }
            }
            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(onClick = { onEvent(PreviewEvent.DeliverNow) }) {
                    Text(text = stringResource(id = R.string.deliver_now))
                }
                Button(onClick = { onEvent(PreviewEvent.Snooze15m) }) {
                    Text(text = stringResource(id = R.string.snooze_15m))
                }
                val context = LocalContext.current
                Button(onClick = {
                    Toast.makeText(
                        context,
                        context.getString(R.string.skip_confirmed),
                        Toast.LENGTH_SHORT
                    ).show()
                    onEvent(PreviewEvent.Skip)
                }) {
                    Text(text = stringResource(id = R.string.skip))
                }
            }
        }
    }
}

/** UI state for the preview screen. */
data class PreviewState(
    val lines: List<String> = emptyList(),
)

/**
 * User intents originating from the preview screen.
 * ViewModels can react to these and perform work.
 */
sealed interface PreviewEvent {
    data object DeliverNow : PreviewEvent
    data object Snooze15m : PreviewEvent
    data object Skip : PreviewEvent
}

