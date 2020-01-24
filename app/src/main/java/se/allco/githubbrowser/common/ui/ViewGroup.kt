package se.allco.githubbrowser.common.ui

import android.view.View
import android.view.ViewGroup

fun ViewGroup.childIterator(): Iterator<View> = object : Iterator<View> {
    private var index = 0
    override fun hasNext(): Boolean = index < childCount
    override fun next(): View = getChildAt(index++)
}
