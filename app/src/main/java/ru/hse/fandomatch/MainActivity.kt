package ru.hse.fandomatch

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import ru.hse.fandomatch.navigation.MainView
import ru.hse.fandomatch.ui.theme.FandoMatchTheme

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        Log.i("MainActivity", "Notification permission granted: $isGranted")
    }

    private var showNotificationRationale by mutableStateOf(false)

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                val channelId = "default_channel"
                val channelName = "Default Channel"
                val channelDescription = "General notifications"
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel(channelId, channelName, importance).apply {
                    description = channelDescription
                }
                val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.createNotificationChannel(channel)
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                showNotificationRationale = true
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // todo прокинуть в compose
        val navigateTo = intent.getStringExtra("navigateTo")
        val userId = intent.getLongExtra("userId", -1)

        askNotificationPermission()

        enableEdgeToEdge()
        setContent {
            FandoMatchTheme {
                if (showNotificationRationale && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    AlertDialog(
                        onDismissRequest = { showNotificationRationale = false },
                        title = { Text(stringResource(R.string.notification_allow_popup_name)) },
                        text = { Text(stringResource(R.string.notification_allow_popup_description)) },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showNotificationRationale = false
                                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            ) { Text(stringResource(R.string.ok)) }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showNotificationRationale = false }
                            ) { Text(stringResource(R.string.no_thanks)) }
                        }
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainView()
                }
            }
        }
    }
}
