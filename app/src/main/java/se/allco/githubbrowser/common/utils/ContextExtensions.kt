package se.allco.githubbrowser.common.utils

import android.Manifest
import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.location.LocationManager
import android.os.Build
import android.os.Vibrator
import android.util.TypedValue
import android.view.LayoutInflater
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.FloatRange
import androidx.annotation.PluralsRes
import androidx.annotation.RawRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.math.roundToInt

/**
 * Context related utils
 */

private const val DEFAULT_LOCALE_INDEX = 0

/**
 * Returns the color from context using ContextCompat
 */
@ColorInt
fun Context.getColorCompat(@ColorRes resId: Int): Int =
    ContextCompat.getColor(this, resId)

fun Context.getDrawableCompat(@DrawableRes resId: Int): Drawable =
    ContextCompat.getDrawable(this, resId)
        ?: throw Resources.NotFoundException("Can not find resource with id: ($resId)")

fun Context.getQuantityString(@PluralsRes id: Int, quantity: Int, vararg formatArgs: Any): String =
    resources.getQuantityString(id, quantity, *formatArgs)

fun Context.getColorWithAlpha(
    @ColorRes resId: Int,
    @FloatRange(from = 0.0, to = 1.0) alpha: Float
): Int {
    val eightBit = (255 * alpha).roundToInt()
    return ColorUtils.setAlphaComponent(getColorCompat(resId), eightBit)
}

fun Context.getDefaultLocale(): Locale = resources.configuration.run {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        locales.get(DEFAULT_LOCALE_INDEX)
    } else {
        @Suppress("DEPRECATION")
        locale
    }
}

fun Context.getScreenHeight(): Int = resources.displayMetrics.heightPixels

fun Context.getScreenWidth(): Int = resources.displayMetrics.widthPixels

fun Context.getLayoutInflater(): LayoutInflater = LayoutInflater.from(this)

fun Resources.getRaw(@RawRes rawRes: Int): String =
    openRawResource(rawRes).bufferedReader(StandardCharsets.UTF_8).readText()

fun Context.getRaw(@RawRes rawRes: Int): String = resources.getRaw(rawRes)

/**
 * Check if the permission is granted
 *
 * @param permission e.g in the form of [Manifest.permission.ACCESS_FINE_LOCATION] or similar
 * @return true if it is given
 */
fun Context.hasPermission(permission: String): Boolean {
    val checkedPermission = ActivityCompat.checkSelfPermission(this, permission)
    return (checkedPermission == PackageManager.PERMISSION_GRANTED)
}

fun Context.getVibrator(): Vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator

fun Context.dpToPx(dps: Float): Int =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dps, resources.displayMetrics).toInt()

fun Context.pxToDp(pxs: Int): Float = pxs / resources.displayMetrics.density

fun Context.isLocationEnabled(): Boolean {
    val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
}
