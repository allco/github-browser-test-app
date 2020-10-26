package se.allco.githubbrowser.common.network_reporter

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import androidx.annotation.RequiresApi
import io.reactivex.rxjava3.core.Observable

@RequiresApi(Build.VERSION_CODES.N)
internal class NetworkReporterApi24(context: Context) {
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