package se.allco.githubbrowser.common.ui.recyclerview

import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.databinding.OnRebindCallback
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import se.allco.githubbrowser.common.ui.childIterator

/**
 * It is based on a google sample:
 * https://github.com/google/android-ui-toolkit-demos/tree/master/DataBinding/DataBoundRecyclerView/app/src/main/java/com/example/android/databoundrecyclerview
 *
 * A reference implementation for an adapter that wants to use data binding "the right way". It
 * works with {@link DataBoundViewHolder}.
 * <p>
 * It listens for layout invalidation and notifies RecyclerView about them before views refresh
 * themselves. It also avoids invalidating the full item when data in the bound item dispatches
 * proper notify events.
 * <p>
 * This class uses layout id as the item type.
 * <p>
 * It can be used for both single type lists and multiple type lists.
 *
 * @param <T> The type of the ViewDataBinding class. Can be omitted in multiple-binding-type use case.
 */
abstract class BaseAdapter<T : ViewDataBinding> : RecyclerView.Adapter<DataBoundViewHolder<T>>() {

    companion object {
        private val DB_PAYLOAD = Any()
    }

    private val attachStateListener = object : View.OnAttachStateChangeListener {
        override fun onViewDetachedFromWindow(view: View) {
            (view as? RecyclerView)?.also { recyclerView ->
                recyclerView.childIterator().forEach {
                    @Suppress("UNCHECKED_CAST")
                    val holder = recyclerView.getChildViewHolder(it) as DataBoundViewHolder<T>
                    unbindItem(holder)
                }
            }
        }

        override fun onViewAttachedToWindow(view: View) {
            (view as? RecyclerView)?.also { recyclerView ->
                recyclerView.childIterator().forEach {
                    @Suppress("UNCHECKED_CAST")
                    val holder = recyclerView.getChildViewHolder(it) as DataBoundViewHolder<T>
                    when (recyclerView.getChildAdapterPosition(it)) {
                        -1 -> unbindItem(holder)
                        else -> bindItem(holder, recyclerView.getChildAdapterPosition(it), emptyList())
                    }
                }
            }
        }
    }

    /**
     * This is used to block items from updating themselves. RecyclerView wants to know when an
     * item is invalidated and it prefers to refresh it via onRebind. It also helps with performance
     * since data binding will not update views that are not changed.
     */

    private val mOnRebindCallback = object : OnRebindCallback<T>() {
        override fun onPreBind(binding: T?): Boolean {
            val recyclerView = (binding?.root?.parent as? RecyclerView) ?: return true

            if (recyclerView.isComputingLayout) {
                return true
            }

            val childAdapterPosition = recyclerView.getChildAdapterPosition(binding.root)
            if (childAdapterPosition == RecyclerView.NO_POSITION) {
                return true
            }

            notifyItemChanged(
                childAdapterPosition,
                DB_PAYLOAD
            )
            return false
        }
    }

    @CallSuper
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataBoundViewHolder<T> =
        DataBoundViewHolder.create<T>(parent, viewType).also { holder ->
            holder.binding.addOnRebindCallback(mOnRebindCallback)
        }

    final override fun onBindViewHolder(holder: DataBoundViewHolder<T>, position: Int) {
        throw IllegalArgumentException("just overridden to make final.")
    }

    override fun onBindViewHolder(holder: DataBoundViewHolder<T>, position: Int, payloads: List<Any>) {
        if (!payloads.contains(DB_PAYLOAD)) {
            // When a VH is just created only then call the setters
            bindItem(holder, position, payloads)
        }
        holder.binding.executePendingBindings()
    }

    override fun onViewRecycled(holder: DataBoundViewHolder<T>) {
        super.onViewRecycled(holder)
        unbindItem(holder)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.addOnAttachStateChangeListener(attachStateListener)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        recyclerView.removeOnAttachStateChangeListener(attachStateListener)
    }

    override fun getItemViewType(position: Int): Int = getItemLayoutId(position)

    @LayoutRes
    protected abstract fun getItemLayoutId(position: Int): Int

    /**
     * Override this method to handle binding your items into views
     *
     * @param holder The ViewHolder that has the binding instance
     * @param position The position of the item in the adapter
     * @param payloads The payloads that were passed into the onBind method
     */
    @CallSuper
    protected open fun bindItem(holder: DataBoundViewHolder<T>, position: Int, payloads: List<Any>) {
        holder.onBind()
    }

    /**
     * Override this method to handle unbinding your items from views
     *
     * @param holder The ViewHolder that has the binding instance
     */
    @CallSuper
    protected open fun unbindItem(holder: DataBoundViewHolder<T>) {
        holder.onUnbind()
    }
}
