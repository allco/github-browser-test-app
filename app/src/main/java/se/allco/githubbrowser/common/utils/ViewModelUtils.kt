package se.allco.githubbrowser.common.utils

import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.IntegerRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import javax.inject.Provider

/**
 *  SAM conversion does not work well for ViewModelProvider.Factory since
 *  it is not declared as generic but it has a generic method.
 *
 *  This method lets to pass a lambda as a factory to [androidx.lifecycle.ViewModelProviders.of]
 *
 *  Example:
 *  ```
 *     ViewModelProviders.of(this,
 *             createViewFactoryFactory {Application.appComponent(context).createLoginViewModel() })
 *             .get(LoginViewModel::class.java)
 *   ```
 *  instead of:
 *  ```
 *     ViewModelProviders.of(this,
 *             object : ViewModelProvider.Factory{
 *             override fun <T : ViewModel?> create(modelClass: Class<T>): T {
 *                   return  Application.appComponent(context).createLoginViewModel() as T
 *                 }
 *             })
 *             .get(LoginViewModel::class.java)
 * ```
 *
 *  @see <a href="https://kotlinlang.org/docs/reference/java-interop.html#sam-conversions">SAM conversion</a>
 */
fun <T : ViewModel> createViewFactoryFactory(factory: () -> T): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = factory() as T
    }
}

/**
 * Returns an instance of the view model, it either creates an instance or fetches one if it exists for this fragment in
 * the ViewModelStorage
 */
inline fun <reified T : ViewModel> Fragment.getViewModel(viewModelProvider: Provider<T>): T =
    ViewModelProviders.of(this, createViewFactoryFactory { viewModelProvider.get() }).get(T::class.java)

inline fun <reified T : ViewModel> Fragment.getViewModel(crossinline viewModelProvider: () -> T): T =
    ViewModelProviders.of(this, createViewFactoryFactory { viewModelProvider() }).get(T::class.java)

inline fun <reified T : ViewModel> FragmentActivity.getViewModel(viewModelProvider: Provider<T>): T =
    ViewModelProviders.of(this, createViewFactoryFactory { viewModelProvider.get() }).get(T::class.java)

inline fun <reified T : ViewModel> FragmentActivity.getViewModel(crossinline viewModelProvider: () -> T): T =
    ViewModelProviders.of(this, createViewFactoryFactory { viewModelProvider() }).get(T::class.java)

fun <T : ViewModel> Fragment.getViewModel(
    viewModelProvider: Provider<T>,
    clazz: Class<T>,
    initializer: (T.() -> Unit)? = null
): T =
    ViewModelProviders.of(this, createViewFactoryFactory {
        viewModelProvider.get().also { initializer?.invoke(it) }
    }).get(clazz)

fun <T : ViewModel> FragmentActivity.getViewModel(
    viewModelProvider: Provider<T>,
    clazz: Class<T>,
    initializer: (T.() -> Unit)? = null
): T =
    ViewModelProviders.of(this, createViewFactoryFactory {
        viewModelProvider.get().also { initializer?.invoke(it) }
    }).get(clazz)

fun AndroidViewModel.getString(@StringRes res: Int): String = this.getApplication<Application>().getString(res)

fun AndroidViewModel.getString(@StringRes res: Int, vararg args: Any): String =
    this.getApplication<Application>().getString(res, *args)

fun AndroidViewModel.getInteger(@IntegerRes res: Int) =
    this.getApplicationContext().resources.getInteger(res)

/**
 * Convenience method for fetching a [Drawable], Uses the [ContextCompat] for backwards compatibility
 *
 * @param res The resource id
 * @throws Resources.NotFoundException when the resource is not found
 */
fun AndroidViewModel.getDrawable(@DrawableRes res: Int): Drawable =
    ContextCompat.getDrawable(this.getApplication(), res)
        ?: throw Resources.NotFoundException("Can not find resource ($res)")

/**
 * Convenience method for fetching a color, Uses the [ContextCompat] for backwards compatibility
 *
 * @param res The resource id
 * @throws Resources.NotFoundException when the resource is not found
 */
@Suppress("unused")
fun AndroidViewModel.getColor(@ColorRes res: Int): Int =
    ContextCompat.getColor(this.getApplication(), res)

/**
 * Convenience method for [Resources.getDimensionPixelSize]
 */
fun AndroidViewModel.getDimensionPixelSize(@DimenRes res: Int): Int =
    this.getApplication<Application>().resources.getDimensionPixelSize(res)

/**
 * Convenience method for getting the context inside a [AndroidViewModel]
 */
fun AndroidViewModel.getApplicationContext(): Context =
    getApplication<Application>().applicationContext
