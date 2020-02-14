package se.allco.githubbrowser.common.ui

import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment

inline fun <reified T> Fragment.findListenerOrThrow(): T =
    T::class.java.let { clazz: Class<T> ->
        when {
            clazz.isInstance(parentFragment) -> clazz.cast(parentFragment)!!
            clazz.isInstance(parentFragment?.parentFragment) -> clazz.cast(parentFragment?.parentFragment)!!
            clazz.isInstance(activity) -> clazz.cast(activity)!!
            else -> throw IllegalStateException("the parent Fragment or grandparent Fragment or Activity has to implement `${clazz.name}` interface")
        }
    }

fun Fragment.overrideOnBackPress(onBack: OnBackPressedCallback.() -> Unit): OnBackPressedCallback =
    object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            with(this, onBack)
        }
    }.also {
        requireActivity().onBackPressedDispatcher.addCallback(this, it)
    }
