package se.allco.githubbrowser.common.logging

import android.util.Log
import okhttp3.Interceptor
import okhttp3.internal.platform.Platform
import javax.inject.Inject

typealias Logger = (level: Int, tag: String, msg: String) -> Unit

fun createLoggerInterceptor(tag: String): LoggingInterceptor = LoggingInterceptor.Builder()
    .apply {
        decoration = true
        type = Log.DEBUG
        level = LoggingInterceptor.Level.BASIC
        responseTag = tag
        requestTag = tag
    }
    .build()

interface LoggingInterceptor : Interceptor {
    enum class Level {
        /**
         * No logs.
         */
        NONE,
        /**
         *
         * Example:
         * <pre>`- URL
         * - Method
         * - Headers
         * - Body
        `</pre> *
         */
        BASIC,
        /**
         *
         * Example:
         * <pre>`- URL
         * - Method
         * - Headers
        `</pre> *
         */
        HEADERS,
        /**
         *
         * Example:
         * <pre>`- URL
         * - Method
         * - Body
        `</pre> *
         */
        BODY
    }

    class Builder @Inject constructor() {

        companion object {
            private var TAG = "OkHttp"
        }

        var type = Platform.INFO
        var requestTag: String = TAG
        var responseTag: String = TAG
        var level = Level.BASIC
        var loggerFactory: () -> Logger = { DEFAULT_LOGGER }
        var randomizeTags = false
        var decoration = true

        internal fun getTag(isRequest: Boolean): String = if (isRequest) requestTag else responseTag

        @Suppress("unused")
        fun build(): LoggingInterceptor {
            return LoggingInterceptorImpl(this)
        }
    }

    companion object {
        val DEFAULT_LOGGER: Logger = { level, tag, msg -> Log.println(level, tag, msg) }
    }
}
