package kr.co.yogiyo.simplesociallogin.internal

import android.util.Log
import io.reactivex.Single
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

object HttpRequestHelper {
    private val okHttpClient = OkHttpClient()

    private fun getResponse(url: String, authorization: String): String {
        val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", authorization)
                .build()

        try {
            val response = okHttpClient.newCall(request).execute()
            return response.body()!!.string()
        } catch (e: IOException) {
            Log.e(HttpRequestHelper::class.java.simpleName, e.message)
            throw e
        }
    }

    @JvmStatic
    fun createRequest(url: String, authorization: String): Single<String> {
        return Single.create { it.onSuccess(getResponse(url, authorization)) }
    }
}