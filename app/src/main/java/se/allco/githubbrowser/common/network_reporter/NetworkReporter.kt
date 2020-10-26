package se.allco.githubbrowser.common.network_reporter

import android.content.Context
import android.os.Build
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

interface NetworkReporter {
    /**
     * @returns a stream which emits `true` if the device is/gets online or `false` otherwise,
     */
    fun states(): Observable<Boolean>
}

class NetworkReporterImpl @Inject constructor(context: Context) : NetworkReporter {

    private val connectivityStatesStream: Observable<Boolean> by lazy {
        when {
            Build.VERSION.SDK_INT >= 24 -> NetworkReporterApi24(context).connectivityStatesStream
            else -> NetworkReporterApi23(context).connectivityStatesStream
        }
            .startWithItem(false)
            .distinctUntilChanged()
            .debounce(200, TimeUnit.MILLISECONDS, Schedulers.io())
            .replay(1)
            .refCount()
    }

    override fun states(): Observable<Boolean> = connectivityStatesStream
}
