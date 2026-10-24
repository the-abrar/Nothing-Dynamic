package com.nothing.dynamic

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.view.View
import android.graphics.Rect
import android.content.Intent

import android.os.IBinder
import android.text.Layout
import android.view.*
import android.widget.TextView
import androidx.core.content.ContextCompat

/**
 * A very small, n‑op service that demonstrates how to query the `DisplayCutout`
 * API.  The real overlay logic would create a little window with the
 * `SOFT_INPUT_ADJUST_RESIZE` flag and then compute the bounds that wrap the
 * cut‑out area.  For now we only expose the plumbing and log the bounds.
 */
class OverlayService : Service() {

    private var windowManager: WindowManager? = null
    private val statusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            super.onReceive(context, intent)
            val title = intent?.getStringExtra("TITLE") ?: ""
            val text = intent?.getStringExtra("TEXT") ?: ""
            overlayView?.findViewById<TextView>(R.id.app_title)?.text = title
            overlayView?.findViewById<TextView>(R.id.app_subtitle)?.text = text
        }
    }
    private var overlayView: View? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_PHONE,
            flags,
            PixelFormat.TRANSLUCENT
        )
        overlayView = inflater.inflate(R.layout.dynamic_island_bubble, null).apply {
            setBackgroundColor(ContextCompat.getColor(this@OverlayService, android.R.color.transparent))
            layoutParams = params
        }
        // Position the overlay directly at the cut‑out bounds
        val bounds = computeOverlayBounds()
        params.x = bounds.left
        params.y = bounds.top
        // Let gravity take precedence for padding around the cut‑out
        // keep the default WRAP_CONTENT size.
        params.width = bounds.width()
        params.height = bounds.height()
        params.gravity = Gravity.TOP or Gravity.START
        // Update the existing view if it already existed
        // Register the status receiver
        val filter = IntentFilter("com.nothing.dynamic.UPDATE_STATUS")
        registerReceiver(statusReceiver, filter)
    }

    /**
     * Computes bounds that surround the display cut‑out.
     * This is a no‑op placeholder; callers can fill the implementation.
     */
    private fun computeOverlayBounds(): Rect {
        val display = windowManager?.defaultDisplay ?: return Rect(0, 0, 0, 0)
        val metrics = android.util.DisplayMetrics()
        display.getRealMetrics(metrics)
        val cutout: DisplayCutout? = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            display.getRectInsets()
        } else { null }
        return if (cutout != null) {
            // A very naive bounds calculation – just hook up the cut‑out bounds
            Rect(cutout.safeInsetLeft, cutout.safeInsetTop,
                metrics.widthPixels - cutout.safeInsetRight,
                metrics.heightPixels - cutout.safeInsetBottom)
        } else {
            // Full‑screen; nothing to adjust
            Rect(0, 0, metrics.widthPixels, metrics.heightPixels)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // In a real service we would schedule periodic updates or listen for
        // notification changes.  For now we just log the computed bounds.
        val bounds = computeOverlayBounds()
        android.util.Log.d("OverlayService", "Cut‑out bounds: $bounds")
        return START_STICKY
    }

    override fun onDestroy() {
        // Cleanup: unregister receiver before removing the view
        unregisterReceiver(statusReceiver)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
