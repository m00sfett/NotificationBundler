package de.moosfett.notificationbundler.ui

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import de.moosfett.notificationbundler.R
import de.moosfett.notificationbundler.ui.nav.AppNav

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request POST_NOTIFICATIONS on Android 13+.
        if (Build.VERSION.SDK_INT >= 33) {
            val permission = android.Manifest.permission.POST_NOTIFICATIONS
            val launcher = registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { /* ignore result, optional UX later */ }
            if (ContextCompat.checkSelfPermission(this, permission)
                != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                launcher.launch(permission)
            }
        }

        setContent {
            MaterialTheme {
                AppNav()
            }
        }
    }
}
