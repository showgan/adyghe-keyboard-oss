package com.adyghe.keyboard.ime

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.adyghe.keyboard.R
import com.adyghe.keyboard.core.clipboard.ClipboardMonitor
import com.adyghe.keyboard.core.haptics.VibrationProvider
import com.adyghe.keyboard.core.input.InputConnectionController
import com.adyghe.keyboard.core.prefs.KeyboardPreferences
import com.adyghe.keyboard.core.state.ImeStore
import com.adyghe.keyboard.ime.compose.ImeLifecycleOwner
import com.adyghe.keyboard.ui.KeyboardScreen
import com.adyghe.keyboard.ui.configureEdgeToEdge
import com.adyghe.keyboard.ui.theme.AdygheKeyboardTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AdigeImeService : InputMethodService() {

    @Inject lateinit var store: ImeStore
    @Inject lateinit var inputConnectionController: InputConnectionController
    @Inject lateinit var vibration: VibrationProvider
    @Inject lateinit var keyboardPreferences: KeyboardPreferences
    @Inject lateinit var clipboardMonitor: ClipboardMonitor

    private val lifecycleOwner = ImeLifecycleOwner()
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    /**
     * Watch for language changes and force the system to re-show the keyboard.
     * Without this, the system cancels the show request after a language switch
     * because it thinks the IME controls changed, leaving the keyboard hidden.
     *
     * The token must come from [android.view.inputmethod.InputBinding.getConnectionToken]
     * — an [android.view.inputmethod.InputConnection] is not an [android.os.IBinder],
     * so casting it always yields null and the request is silently ignored.
     */
    private fun observeLanguageChanges() {
        serviceScope.launch {
            store.state
                .map { it.languageId }
                .distinctUntilChanged()
                // Skip the initial value; only re-show after an actual language switch.
                .drop(1)
                .collect {
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInputFromInputMethod(
                        currentInputBinding?.connectionToken,
                        InputMethodManager.SHOW_IMPLICIT
                    )
                }
        }
    }

    private fun observeNumberRowPreference() {
        serviceScope.launch {
            keyboardPreferences.numberRowEnabled.collect { enabled ->
                store.setNumberRowEnabled(enabled)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        lifecycleOwner.onCreate()
        store.setEditorInfo(null)
        observeLanguageChanges()
        observeNumberRowPreference()
    }

    /**
     * Force the IME's input view to always be eligible for display. Without this, AOSP's
     * [android.inputmethodservice.InputMethodService.onEvaluateInputViewShown] hides the soft
     * keyboard when a hardware keyboard is present (the emulator/ranchu reports one), so
     * [android.inputmethodservice.InputMethodService.onShowInputRequested] returns false.
     */
    override fun onEvaluateInputViewShown(): Boolean = true

    /**
     * Accept every show request (including implicit ones from the launcher search bar).
     * AOSP's default returns false for non-[android.view.inputmethod.InputMethod.SHOW_EXPLICIT]
     * requests when the device reports a hard keyboard or the IME is in fullscreen mode and
     * not yet shown, which is the root cause of "onFailed at PHASE_IME_ON_SHOW_SOFT_INPUT_TRUE"
     * and the subsequent invalid-height cancellation.
     */
    override fun onShowInputRequested(flags: Int, configChange: Boolean): Boolean = true

    override fun onCreateInputView(): View {
        // Compose locates its Lifecycle/ViewModel/SavedState owners from the window's
        // decor view, not from individual child views, so attach them there.
        window?.window?.let { imeWindow ->
            imeWindow.configureEdgeToEdge()
            imeWindow.decorView.let { decorView ->
                decorView.setViewTreeLifecycleOwner(lifecycleOwner)
                decorView.setViewTreeViewModelStoreOwner(lifecycleOwner)
                decorView.setViewTreeSavedStateRegistryOwner(lifecycleOwner)
            }
        }

        return ComposeView(this).apply {
            // Compose 1.7+ resolves owners by walking the view parent chain, which
            // stops at the IME window's parentPanel and never reaches the decor view,
            // so the owners must live on the ComposeView itself.
            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeViewModelStoreOwner(lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(lifecycleOwner)

            // Layouts store keys in visual LTR order. An RTL system locale (Hebrew/Arabic)
            // would otherwise reverse Compose Row children and AndroidView chrome.
            layoutDirection = View.LAYOUT_DIRECTION_LTR

            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                val themeMode by keyboardPreferences.themeMode.collectAsState(
                    initial = keyboardPreferences.getThemeMode()
                )
                AdygheKeyboardTheme(themeMode = themeMode) {
                    KeyboardScreen(store = store)
                }
            }
        }
    }

    /**
     * Fired whenever the IME attaches to an editor. Reset the composing word so a half-typed
     * word from a previous app/field is never carried into the new one. When the system
     * (re)starts input it abandons any in-flight composing region, so clearing our composer
     * here cannot lose text the user can still see — it only drops our stale copy.
     */
    override fun onStartInput(info: EditorInfo?, restarting: Boolean) {
        super.onStartInput(info, restarting)
        inputConnectionController.setInputConnection(currentInputConnection)
        // Apply EditorInfo (package / inputType) before any keystroke so composing
        // policy is correct for this editor, not leftover from the previous app.
        store.setEditorInfo(info)
        store.onInputStarted()
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        lifecycleOwner.onResume()
        inputConnectionController.setInputConnection(currentInputConnection)
        store.setEditorInfo(info)
        store.reloadEnabledLanguages()
        store.reloadNumberRow()
        // Haptic feedback on key presses.
        store.onKeyAction = { vibration.tick() }
    }

    /**
     * The focused editor moved its caret or selection. Forward to [ImeStore] so the
     * in-memory composing word and the live composing span are dropped when the user
     * taps a new location; otherwise backspace keeps editing the old span.
     */
    override fun onUpdateSelection(
        oldSelStart: Int,
        oldSelEnd: Int,
        newSelStart: Int,
        newSelEnd: Int,
        candidatesStart: Int,
        candidatesEnd: Int
    ) {
        super.onUpdateSelection(
            oldSelStart,
            oldSelEnd,
            newSelStart,
            newSelEnd,
            candidatesStart,
            candidatesEnd
        )
        store.onSelectionUpdated(
            oldSelStart,
            oldSelEnd,
            newSelStart,
            newSelEnd,
            candidatesStart,
            candidatesEnd
        )
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        lifecycleOwner.onPause()
        store.onKeyAction = null
    }

    override fun onCreateCandidatesView(): View? = null

    override fun onWindowShown() {
        super.onWindowShown()
        lifecycleOwner.onStart()
        clipboardMonitor.start()
    }

    override fun onWindowHidden() {
        super.onWindowHidden()
        clipboardMonitor.stop()
        lifecycleOwner.onStop()
    }

    override fun onDestroy() {
        serviceScope.cancel()
        lifecycleOwner.onDestroy()
        super.onDestroy()
    }
}
