package com.nothing.dynamic

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity

/**
 * Main user interface for the "Nothing Dynamic" system.  The activity presents
 * two controls – one to request the **SYSTEM_ALERT_WINDOW** permission and
 * another to enable notifications.  When the overlay permission is approved
 * the background OverlayService is launched.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var overlayPermissionSwitch: Switch
    private lateinit var requestOverlayButton: Button
    private lateinit var notificationCheckButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        overlayPermissionSwitch = findViewById(R.id.toggle_overlay_switch)
        requestOverlayButton = findViewById(R.id.button_request_overlay)
        notificationCheckButton = findViewById(R.id.button_check_notifications)

        // Update UI state for overlay permission
        updateOverlaySwitch()

        requestOverlayButton.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                startActivity(intent)
            }
        }

        overlayPermissionSwitch.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                startActivity(intent)
            } else {
                // Permission already granted – start OverlayService immediately
                startService(Intent(this, OverlayService::class.java))
            }
        }

        notificationCheckButton.setOnClickListener {
            // Simple feedback – in a real app you might launch the Notification Access settings
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            startActivity(intent)
        }
    }

    private fun updateOverlaySwitch() {
        val canDraw = Settings.canDrawOverlays(this)
        overlayPermissionSwitch.isChecked = canDraw
    }
}
