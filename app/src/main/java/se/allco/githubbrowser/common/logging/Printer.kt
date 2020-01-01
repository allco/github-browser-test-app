package se.allco.githubbrowser.common.logging

import android.text.TextUtils
import okhttp3.Request
import okio.Buffer
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.LinkedList

internal class Printer(private val logger: Logger) {
    private val logList = LinkedList<String>()

    private fun flush(builder: LoggingInterceptor.Builder, isRequest: Boolean) {
        val type = builder.type
        val tag = builder.getTag(isRequest)
        var randomFactor = false
        synchronized(Printer::class.java) {
            for (line in logList) {
                val tagToPrint =
                    when {
                        builder.randomizeTags -> {
                            randomFactor = !randomFactor
                            tag + ("\\".takeIf { randomFactor } ?: "/")
                        }
                        else -> tag
                    }
                logger.invoke(type, tagToPrint, line)
            }
            logList.clear()
        }
    }

    @Synchronized
    private fun log(message: String) {
        logList.add(message)
    }

    private fun logLines(lines: Array<String>, decoration: Boolean, withLineSize: Boolean) {
        for (line in lines) {
            val lineLength = line.length
            val maxLongSize = if (withLineSize) 110 else lineLength
            for (i in 0..lineLength / maxLongSize) {
                val start = i * maxLongSize
                var end = (i + 1) * maxLongSize
                end = if (end > line.length) line.length else end
                var message = line.substring(start, end)
                if (decoration) message = DEFAULT_LINE + message
                log(message)
            }
        }
    }

    fun printJsonRequest(builder: LoggingInterceptor.Builder, request: Request) {
        val requestBody = LINE_SEPARATOR + BODY_TAG + LINE_SEPARATOR + bodyToString(request)

        if (builder.decoration) {
            log(REQUEST_UP_LINE)
        }

        logLines(arrayOf(URL_TAG + request.url), builder.decoration, false)
        logLines(getRequest(request, builder.level), builder.decoration, true)
        if (builder.level == LoggingInterceptor.Level.BASIC || builder.level == LoggingInterceptor.Level.BODY) {
            logLines(
                requestBody.split(LINE_SEPARATOR.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray(),
                builder.decoration,
                true
            )
        }

        if (builder.decoration) {
            log(END_LINE)
        }

        flush(builder, true)
    }

    fun printJsonResponse(
        builder: LoggingInterceptor.Builder,
        chainMs: Long,
        isSuccessful: Boolean,
        code: Int,
        headers: String,
        bodyString: String,
        segments: List<String>,
        message: String,
        responseUrl: String
    ) {

        val responseBody = LINE_SEPARATOR + BODY_TAG + LINE_SEPARATOR + getJsonString(bodyString)
        val urlLine = arrayOf(URL_TAG + responseUrl, N)
        val response = getResponse(
            headers, chainMs, code, isSuccessful,
            builder.level, segments, message
        )

        if (builder.decoration) {
            log(RESPONSE_UP_LINE)
        }

        logLines(urlLine, builder.decoration, true)
        logLines(response, builder.decoration, true)

        if (builder.level == LoggingInterceptor.Level.BASIC || builder.level == LoggingInterceptor.Level.BODY) {
            logLines(
                responseBody.split(LINE_SEPARATOR.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray(),
                builder.decoration,
                true
            )
        }
        if (builder.decoration) {
            log(END_LINE)
        }

        flush(builder, false)
    }

    fun printFileRequest(builder: LoggingInterceptor.Builder, request: Request) {
        if (builder.decoration) {
            log(REQUEST_UP_LINE)
        }

        logLines(arrayOf(URL_TAG + request.url), builder.decoration, false)
        logLines(getRequest(request, builder.level), builder.decoration, true)
        if (builder.level == LoggingInterceptor.Level.BASIC || builder.level == LoggingInterceptor.Level.BODY) {
            logLines(OMITTED_REQUEST, builder.decoration, true)
        }
        if (builder.decoration) {
            log(END_LINE)
        }

        flush(builder, true)
    }

    fun printFileResponse(
        builder: LoggingInterceptor.Builder,
        chainMs: Long,
        isSuccessful: Boolean,
        code: Int,
        headers: String,
        segments: List<String>,
        message: String
    ) {
        if (builder.decoration) {
            log(RESPONSE_UP_LINE)
        }
        logLines(
            getResponse(
                headers, chainMs, code, isSuccessful,
                builder.level, segments, message
            ), builder.decoration, true
        )
        logLines(OMITTED_RESPONSE, builder.decoration, true)
        if (builder.decoration) {
            log(END_LINE)
        }

        flush(builder, false)
    }

    companion object {

        private const val JSON_INDENT = 3

        private val LINE_SEPARATOR = System.getProperty("line.separator") ?: "\n"
        private val DOUBLE_SEPARATOR = LINE_SEPARATOR + LINE_SEPARATOR

        private val OMITTED_RESPONSE = arrayOf(LINE_SEPARATOR, "Omitted response body")
        private val OMITTED_REQUEST = arrayOf(LINE_SEPARATOR, "Omitted request body")

        private const val N = "\n"
        private const val T = "\t"
        private const val REQUEST_UP_LINE =
            "┌────── Request ────────────────────────────────────────────────────────────────────────"
        private const val END_LINE =
            "└───────────────────────────────────────────────────────────────────────────────────────"
        private const val RESPONSE_UP_LINE =
            "┌────── Response ───────────────────────────────────────────────────────────────────────"
        private const val BODY_TAG = "Body:"
        private const val URL_TAG = "URL: "
        private const val METHOD_TAG = "Method: @"
        private const val HEADERS_TAG = "Headers:"
        private const val STATUS_CODE_TAG = "Status Code: "
        private const val RECEIVED_TAG = "Received in: "
        private const val CORNER_UP = "┌ "
        private const val CORNER_BOTTOM = "└ "
        private const val CENTER_LINE = "├ "
        private const val DEFAULT_LINE = "│ "

        private fun isEmpty(line: String): Boolean {
            return TextUtils.isEmpty(line) || N == line || T == line || TextUtils.isEmpty(line.trim { it <= ' ' })
        }

        fun getRequest(request: Request, level: LoggingInterceptor.Level): Array<String> {
            val log: String
            val header = request.headers.toString()
            val loggableHeader = level == LoggingInterceptor.Level.HEADERS || level == LoggingInterceptor.Level.BASIC
            log = METHOD_TAG + request.method + DOUBLE_SEPARATOR +
                if (isEmpty(header)) "" else if (loggableHeader) HEADERS_TAG + LINE_SEPARATOR + dotHeaders(header) else ""
            return log.split(LINE_SEPARATOR.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        }

        fun getResponse(
            header: String,
            tookMs: Long,
            code: Int,
            isSuccessful: Boolean,
            level: LoggingInterceptor.Level,
            segments: List<String>,
            message: String
        ): Array<String> {
            val log: String
            val loggableHeader = level == LoggingInterceptor.Level.HEADERS || level == LoggingInterceptor.Level.BASIC
            val segmentString = slashSegments(segments)
            val addon = when {
                isEmpty(header) -> ""
                loggableHeader -> HEADERS_TAG + LINE_SEPARATOR +
                    dotHeaders(header)
                else -> ""
            }
            log = ((if (!TextUtils.isEmpty(segmentString)) "$segmentString - " else "") + "is success : " +
                isSuccessful + " - " + RECEIVED_TAG + tookMs + "ms" + DOUBLE_SEPARATOR + STATUS_CODE_TAG +
                code + " / " + message + DOUBLE_SEPARATOR + addon)

            return log.split(LINE_SEPARATOR.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        }

        private fun slashSegments(segments: List<String>): String {
            val segmentString = StringBuilder()
            for (segment in segments) {
                segmentString.append("/").append(segment)
            }
            return segmentString.toString()
        }

        private fun dotHeaders(header: String): String {
            val headers = header.split(LINE_SEPARATOR.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val builder = StringBuilder()
            var tag = "─ "
            if (headers.size > 1) {
                for (i in headers.indices) {
                    tag = when (i) {
                        0 -> CORNER_UP
                        headers.size - 1 -> CORNER_BOTTOM
                        else -> CENTER_LINE
                    }
                    builder.append(tag).append(headers[i]).append("\n")
                }
            } else {
                for (item in headers) {
                    builder.append(tag).append(item).append("\n")
                }
            }
            return builder.toString()
        }

        fun bodyToString(request: Request): String {
            try {
                val copy = request.newBuilder().build()
                val buffer = Buffer()
                val body = copy.body ?: return ""
                body.writeTo(buffer)
                return getJsonString(buffer.readUtf8())
            } catch (e: IOException) {
                return "{\"err\": \"" + e.message + "\"}"
            }
        }

        fun getJsonString(msg: String): String {
            val message: String
            message = try {
                when {
                    msg.startsWith("{") -> {
                        val jsonObject = JSONObject(msg)
                        jsonObject.toString(JSON_INDENT)
                    }
                    msg.startsWith("[") -> {
                        val jsonArray = JSONArray(msg)
                        jsonArray.toString(JSON_INDENT)
                    }
                    else -> msg
                }
            } catch (e: JSONException) {
                msg
            }

            return message
        }
    }
}
