package se.allco.githubbrowser.common.ui.recyclerview

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class DividerItemDecoration(
    private val paddingPx: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        val adapter = parent.adapter ?: return
        val orientation = (parent.layoutManager as? LinearLayoutManager)?.orientation
            ?: throw IllegalStateException("Only LinearLayoutManager supported")
        val index = parent.getChildAdapterPosition(view)
        val lastIndex = adapter.itemCount - 1
        if (index == lastIndex) return
        when (orientation) {
            RecyclerView.HORIZONTAL -> outRect.right = paddingPx
            RecyclerView.VERTICAL -> outRect.bottom = paddingPx
        }
    }
}
