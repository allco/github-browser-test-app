package se.allco.githubbrowser.common.logging

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException
import java.util.concurrent.TimeUnit

class LoggingInterceptorImpl internal constructor(private val builder: LoggingInterceptor.Builder) :
    LoggingInterceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {

        val printer = Printer(builder.loggerFactory.invoke())
        val request = chain.request()
        val requestBody = request.body
        val rSubtype: String? = requestBody?.contentType()?.subtype

        if (builder.level == LoggingInterceptor.Level.NONE) {
            return chain.proceed(request)
        }

        if (isNotFileRequest(rSubtype)) {
            printer.printJsonRequest(builder, request)
        } else {
            printer.printFileRequest(builder, request)
        }

        val st = System.nanoTime()
        val response = chain.proceed(request)
        val chainMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - st)

        val segmentList = request.url.encodedPathSegments
        val header = response.headers.toString()
        val code = response.code
        val isSuccessful = response.isSuccessful
        val message = response.message
        val responseBody = response.body
        val contentType = responseBody!!.contentType()

        var subtype: String? = null
        val body: ResponseBody

        if (contentType != null) {
            subtype = contentType.subtype
        }

        if (isNotFileRequest(subtype)) {
            val bodyString = Printer.getJsonString(responseBody.string())
            val url = response.request.url.toString()

            printer.printJsonResponse(
                builder, chainMs, isSuccessful, code, header, bodyString,
                segmentList, message, url
            )
            body = bodyString.toResponseBody(contentType)
        } else {
            printer.printFileResponse(
                builder,
                chainMs,
                isSuccessful,
                code,
                header,
                segmentList,
                message
            )
            return response
        }

        return response.newBuilder().body(body).build()
    }

    private fun isNotFileRequest(subtype: String?): Boolean {
        return subtype != null && (subtype.contains("json") ||
                subtype.contains("xml") ||
                subtype.contains("plain") ||
                subtype.contains("html"))
    }
}
