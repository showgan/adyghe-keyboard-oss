package com.adyghe.keyboard.core.clipboard

import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import com.adyghe.keyboard.core.state.ImeStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Observes the system clipboard while the IME window is visible and forwards text clips
 * into [ImeStore] history for the Paste chip.
 */
@Singleton
class ClipboardMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val store: ImeStore
) {
    private val clipboard: ClipboardManager?
        get() = context.getSystemService(CLIPBOARD_SERVICE) as? ClipboardManager

    private val listener = ClipboardManager.OnPrimaryClipChangedListener {
        pushPrimaryClip()
    }

    private var listening = false

    fun start() {
        val cm = clipboard ?: return
        if (!listening) {
            cm.addPrimaryClipChangedListener(listener)
            listening = true
        }
        pushPrimaryClip()
    }

    fun stop() {
        val cm = clipboard ?: return
        if (listening) {
            cm.removePrimaryClipChangedListener(listener)
            listening = false
        }
    }

    private fun pushPrimaryClip() {
        val cm = clipboard ?: return
        val clip = cm.primaryClip ?: return
        if (clip.itemCount <= 0) return
        val text = clip.getItemAt(0).coerceToText(context)?.toString() ?: return
        store.onClipboardText(text)
    }
}
