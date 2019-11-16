package se.allco.githubbrowser.common.ui.databinding

import android.view.View
import androidx.databinding.BindingAdapter

@BindingAdapter("goneUnless")
fun setGoneUnless(view: View, isVisible: Boolean?) {
    view.visibility = if (isVisible == true) View.VISIBLE else View.GONE
}
