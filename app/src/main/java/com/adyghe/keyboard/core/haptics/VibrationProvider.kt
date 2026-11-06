package com.adyghe.keyboard.core.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import com.adyghe.keyboard.core.prefs.KeyboardPreferences

/**
 * Provides short, crisp haptic feedback on key presses, mimicking Gboard's tactile feel.
 */
class VibrationProvider(
    private val context: Context,
    private val keyboardPreferences: KeyboardPreferences
) {

    private val vibrator: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    @Suppress("DEPRECATION")
    fun tick(amplitude: Int = 40, durationMs: Long = 12) {
        if (!keyboardPreferences.isHapticEnabled()) return
        if (Settings.System.getInt(
                context.contentResolver,
                Settings.System.HAPTIC_FEEDBACK_ENABLED,
                1
            ) == 0
        ) {
            return
        }
        if (!vibrator.hasVibrator()) return
        val effect = VibrationEffect.createOneShot(durationMs, amplitude)
        vibrator.vibrate(effect)
    }
}
