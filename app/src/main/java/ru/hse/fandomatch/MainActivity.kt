package ru.hse.fandomatch

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
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
import org.koin.android.ext.android.inject
import ru.hse.fandomatch.domain.usecase.auth.GetPermissionShownUseCase
import ru.hse.fandomatch.domain.usecase.auth.SetPermissionShownUseCase
import ru.hse.fandomatch.navigation.MainView
import ru.hse.fandomatch.ui.theme.FandoMatchTheme

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        Log.i("MainActivity", "Notification permission granted: $isGranted")
    }

    private var showNotificationRationale by mutableStateOf(false)
    private var navigateToTarget by mutableStateOf<String?>(null)
    private var targetId by mutableStateOf<String?>(null)

    private val getPermissionShownUseCase: GetPermissionShownUseCase by inject()
    private val setPermissionShownUseCase: SetPermissionShownUseCase by inject()

    private fun updateNotificationTarget(intent: Intent?) {
        navigateToTarget = intent?.getStringExtra("navigateTo")
        targetId = intent?.getStringExtra("id")
    }

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
                val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
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
        updateNotificationTarget(intent)

        if (!getPermissionShownUseCase.execute()) {
            askNotificationPermission()
            setPermissionShownUseCase.execute(true)
        }

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
                    MainView(
                        navigateTo = navigateToTarget,
                        id = targetId,
                        onNotificationConsumed = {
                            navigateToTarget = null
                            targetId = null
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        updateNotificationTarget(intent)
    }
}
