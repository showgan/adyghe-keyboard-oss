package com.adyghe.keyboard.ui

import android.graphics.Color
import android.os.Build
import android.view.Window
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat

/**
 * Enables edge-to-edge drawing behind system bars and display cutouts.
 *
 * On API 30+ this calls [enableEdgeToEdge] and pins the cutout mode to `always`.
 * Older APIs only get transparent system bars — the deprecated `shortEdges` cutout
 * mode is never set, which is what Android 15 / Play Console flag.
 */
fun ComponentActivity.configureEdgeToEdge() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        enableEdgeToEdge()
        window.applyDisplayCutoutAlways()
    } else {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        @Suppress("DEPRECATION")
        window.statusBarColor = Color.TRANSPARENT
        @Suppress("DEPRECATION")
        window.navigationBarColor = Color.TRANSPARENT
    }
}

/** Same cutout / inset policy for non-Activity windows such as the IME. */
fun Window.configureEdgeToEdge() {
    WindowCompat.setDecorFitsSystemWindows(this, false)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        applyDisplayCutoutAlways()
    }
    if (Build.VERSION.SDK_INT < 35) {
        @Suppress("DEPRECATION")
        statusBarColor = Color.TRANSPARENT
        @Suppress("DEPRECATION")
        navigationBarColor = Color.TRANSPARENT
    }
}

private fun Window.applyDisplayCutoutAlways() {
    val attrs = attributes
    attrs.layoutInDisplayCutoutMode =
        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
    attributes = attrs
}
