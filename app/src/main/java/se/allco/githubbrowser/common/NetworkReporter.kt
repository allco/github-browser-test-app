@file:Suppress("DEPRECATION")

package se.allco.githubbrowser.common

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkInfo
import android.os.Build
import androidx.annotation.RequiresApi
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

interface NetworkReporter {
    /**
     * @returns a stream which emits `true` if the device is/gets online or `false` otherwise,
     */
    fun states(): Observable<Boolean>
}

class NetworkReporterImpl @Inject constructor(context: Context) : NetworkReporter {

    @RequiresApi(Build.VERSION_CODES.N)
    private class NetworkReporterApi24(context: Context) {
        val connectivityStatesStream: Observable<Boolean> = Observable.create<Boolean> { emitter ->
            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network?) = emitter.onNext(true)
                override fun onLost(network: Network?) = emitter.onNext(false)
            }

            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            cm.registerDefaultNetworkCallback(callback)
            emitter.setCancellable { cm.unregisterNetworkCallback(callback) }
        }
    }

    private class NetworkReporterApi23(context: Context) {
        companion object {
            @Suppress("DEPRECATION")
            private val INTENT_FILTER = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        }

        val connectivityStatesStream: Observable<Boolean> = Observable.create<Boolean> { emitter ->
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    when {
                        intent.hasExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY) -> emitter.onNext(false)
                        else -> {
                            @Suppress("DEPRECATION")
                            (intent.extras?.get(ConnectivityManager.EXTRA_NETWORK_INFO) as? NetworkInfo)
                                ?.also { emitter.onNext(it.isConnected) }
                        }
                    }
                }
            }

            context.registerReceiver(receiver, INTENT_FILTER)
            emitter.setCancellable { context.unregisterReceiver(receiver) }
        }
    }

    private val connectivityStatesStream by lazy {
        when {
            Build.VERSION.SDK_INT >= 24 -> NetworkReporterApi24(context).connectivityStatesStream
            else -> NetworkReporterApi23(context).connectivityStatesStream
        }
            .startWith(false)
            .distinctUntilChanged()
            .debounce(200, TimeUnit.MILLISECONDS, Schedulers.io())
            .replay(1)
            .refCount()
    }

    override fun states(): Observable<Boolean> = connectivityStatesStream
}
