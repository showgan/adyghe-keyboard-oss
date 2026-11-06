package com.adyghe.keyboard.ime.compose

import android.annotation.SuppressLint
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

/**
 * Compose requires a LifecycleOwner, ViewModelStoreOwner, and SavedStateRegistryOwner.
 * An InputMethodService does not provide these, so we supply minimal implementations
 * and attach them to the view tree via the ViewTree* owners so Compose functions work.
 */
@SuppressLint("WrongThread")
class ImeLifecycleOwner : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private val registry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val vmStore = ViewModelStore()

    init {
        savedStateRegistryController.performRestore(null)
        registry.currentState = Lifecycle.State.CREATED
    }

    override val lifecycle: Lifecycle get() = registry

    override val viewModelStore: ViewModelStore get() = vmStore

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    fun onCreate() {
        // LifecycleRegistry is created in CREATED state already.
    }

    fun onStart() {
        registry.currentState = Lifecycle.State.STARTED
    }

    fun onResume() {
        registry.currentState = Lifecycle.State.RESUMED
    }

    fun onPause() {
        registry.currentState = Lifecycle.State.STARTED
    }

    fun onStop() {
        registry.currentState = Lifecycle.State.CREATED
    }

    fun onDestroy() {
        registry.currentState = Lifecycle.State.DESTROYED
        vmStore.clear()
    }
}
