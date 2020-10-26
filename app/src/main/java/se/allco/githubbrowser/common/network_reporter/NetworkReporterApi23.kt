package se.allco.githubbrowser.common.network_reporter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkInfo
import io.reactivex.rxjava3.core.Observable

internal class NetworkReporterApi23(context: Context) {
    companion object {
        @Suppress("DEPRECATION")
        private val INTENT_FILTER = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
    }

    val connectivityStatesStream: Observable<Boolean> = Observable.create { emitter ->
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when {
                    intent.hasExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY) -> emitter.onNext(
                        false
                    )
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