package se.allco.githubbrowser.common.ui.recyclerview

import androidx.annotation.AnyThread
import androidx.annotation.LayoutRes
import androidx.annotation.MainThread
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import se.allco.githubbrowser.BR

class DataBoundAdapter(items: List<Item>?) : BaseAdapter<ViewDataBinding>() {

    interface Item {
        fun areItemsTheSame(item: Item): Boolean
        fun areContentsTheSame(item: Item): Boolean
        @LayoutRes
        fun getLayoutId(): Int
    }

    private var listItems: List<Item> = items?.toList() ?: emptyList()

    override fun getItemCount(): Int = listItems.size

    /**
     * This method can be called from any thread.
     * Make sure the `this.listItems` is not being changed before the method returns.
     */
    @AnyThread
    fun calculateDiff(items: List<Item>): DiffUtil.DiffResult =

        // TODO Consider using of `AsyncListDiffer`
        DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                listItems[oldItemPosition].areItemsTheSame(items[newItemPosition])

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                listItems[oldItemPosition].areContentsTheSame(items[newItemPosition])

            override fun getOldListSize(): Int = listItems.size
            override fun getNewListSize(): Int = items.size
        })

    @MainThread
    fun updateItems(items: List<Item>, diff: DiffUtil.DiffResult) {
        listItems = items.toList()
        diff.dispatchUpdatesTo(this)
    }

    @MainThread
    fun updateItems(items: List<Item>) =
        updateItems(items, calculateDiff(items))

    override fun bindItem(holder: DataBoundViewHolder<ViewDataBinding>, position: Int, payloads: List<Any>) {
        super.bindItem(holder, position, payloads)
        holder.binding.setVariable(BR.viewModel, listItems[position])
    }

    override fun unbindItem(holder: DataBoundViewHolder<ViewDataBinding>) {
        super.unbindItem(holder)
        holder.binding.setVariable(BR.viewModel, null)
    }

    override fun getItemLayoutId(position: Int): Int =
        listItems[position].getLayoutId()
}
