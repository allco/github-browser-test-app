package se.allco.githubbrowser.common.ui.databinding

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

@BindingAdapter("imageUrl")
fun setImageUrl(view: ImageView, imageUrl: String?) {
    if (imageUrl == null) return
    Glide.with(view).load(imageUrl).centerCrop().into(view)
}
