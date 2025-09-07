package de.moosfett.notificationbundler.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import de.moosfett.notificationbundler.R

@Composable
fun DashboardScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(id = R.string.dashboard), style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        Text("This screen is intentionally minimal. Add logic and state later.")
    }
}
