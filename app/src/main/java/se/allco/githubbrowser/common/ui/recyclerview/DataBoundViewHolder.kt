package se.allco.githubbrowser.common.ui.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.recyclerview.widget.RecyclerView
import timber.log.Timber

/**
 *
 * A generic ViewHolder that wraps a generated ViewDataBinding class.
 *
 * @param <T> The type of the ViewDataBinding class
 * @see <a href="https://github.com/google/android-ui-toolkit-demos/tree/master/DataBinding/DataBoundRecyclerView/app/src/main/java/com/example/android/databoundrecyclerview">Google sample</a>
 * @see <a href="https://developer.android.com/topic/libraries/architecture/lifecycle#lc">LifecycleOwner lifecycle</a>
 */
class DataBoundViewHolder<T : ViewDataBinding> private constructor(val binding: T) :
    RecyclerView.ViewHolder(binding.root), LifecycleOwner {

    private val lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)

    init {
        binding.lifecycleOwner = this
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
    }

    fun onBind() {
        checkState(
            expectedState = Lifecycle.State.CREATED,
            message = "DataBoundViewHolder.onBind error:"
        )
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    fun onUnbind() {
        checkState(
            expectedState = Lifecycle.State.RESUMED,
            message = "DataBoundViewHolder.onUnbind error:"
        )
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
    }

    override fun getLifecycle(): Lifecycle = lifecycleRegistry

    private fun checkState(expectedState: Lifecycle.State, message: String) {
        if (lifecycleRegistry.currentState != expectedState) {
            Timber.e("$message currentState[${lifecycleRegistry.currentState}] expectedState[$expectedState]")
        }
    }

    companion object {

        /**
         * Creates a new ViewHolder for the given layout file.
         *
         *
         * The provided layout must be using data binding.
         *
         * @param parent The RecyclerView
         * @param layoutId The layout id that should be inflated. Must use data binding
         * @param <T> The type of the Binding class that will be generated for the `layoutId`.
         * @return A new ViewHolder that has a reference to the binding class
        </T> */
        fun <T : ViewDataBinding> create(
            parent: ViewGroup,
            @LayoutRes layoutId: Int
        ): DataBoundViewHolder<T> {
            val binding = DataBindingUtil.inflate<T>(
                LayoutInflater.from(parent.context),
                layoutId,
                parent,
                false
            )
            return DataBoundViewHolder(binding)
        }
    }
}
