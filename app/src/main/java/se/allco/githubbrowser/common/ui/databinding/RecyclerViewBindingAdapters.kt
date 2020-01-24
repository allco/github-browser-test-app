package se.allco.githubbrowser.common.ui.databinding

import androidx.databinding.BindingAdapter
import androidx.databinding.adapters.ListenerUtil
import androidx.recyclerview.widget.RecyclerView
import se.allco.githubbrowser.common.ui.recyclerview.DataBoundAdapter
import se.allco.githubbrowser.common.ui.recyclerview.DividerItemDecoration
import se.allco.githubbrowser.common.utils.dpToPx

/**
 * Recycler view
 */
@BindingAdapter("adapter")
fun <T : RecyclerView.Adapter<*>> setRecycleViewAdapter(recyclerView: RecyclerView, adapter: T) {
    recyclerView.adapter = adapter
}

@BindingAdapter("listItems")
fun setRecyclerViewListItems(recyclerView: RecyclerView, listItems: List<DataBoundAdapter.Item>?) {
    listItems?.let {
        when (val adapter = recyclerView.adapter) {
            null -> recyclerView.adapter = DataBoundAdapter(listItems)
            is DataBoundAdapter -> adapter.updateItems(listItems)
            else -> throw IllegalStateException("when the `:listItems` attribute is used no Adapters should be added to the RecyclerView")
        }
    }
}

@BindingAdapter("dividerSize")
fun setRecyclerViewSpacing(recyclerView: RecyclerView, dividerSizePx: Float) {
    recyclerView.addItemDecoration(DividerItemDecoration(dividerSizePx.toInt()))
}

@BindingAdapter("dividerSizeDp")
fun setRecyclerViewSpacingDp(recyclerView: RecyclerView, dividerSizeDp: Float) {
    val spacingPx = recyclerView.context.dpToPx(dividerSizeDp)
    recyclerView.addItemDecoration(DividerItemDecoration(spacingPx))
}

interface BottomRevealListener {
    fun onRevealProgressChanged(progress: Float)
}

@BindingAdapter("bottomRevealListener")
fun setBottomRevealListener(recyclerView: RecyclerView, listener: BottomRevealListener?) {
    val onScrollListener =
        if (listener == null)
            null
        else
            object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

                    val lastItemIndex = recyclerView.adapter?.itemCount?.minus(1) ?: -1

                    val lastViewBottomPosition =
                        recyclerView.layoutManager?.findViewByPosition(lastItemIndex)
                            ?.let { view -> view.top + view.height }

                    if (lastViewBottomPosition != null && recyclerView.measuredHeight >= lastViewBottomPosition) {
                        val paddingScrollOffset = (recyclerView.measuredHeight - lastViewBottomPosition).toFloat()
                        listener.onRevealProgressChanged(paddingScrollOffset.div(recyclerView.paddingBottom))
                    }
                }
            }

    if (onScrollListener != null) {
        recyclerView.addOnScrollListener(onScrollListener)
    }

    ListenerUtil.trackListener(recyclerView, onScrollListener, recyclerView.id)?.let {
        recyclerView.removeOnScrollListener(it)
    }
}
