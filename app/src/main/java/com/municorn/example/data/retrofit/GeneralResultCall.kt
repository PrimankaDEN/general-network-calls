package com.municorn.example.data.retrofit

import com.municorn.example.calls.api.entity.ApiResponse
import com.municorn.example.calls.api.entity.ConnectionCallException
import com.municorn.example.calls.api.entity.ConnectionCallExceptionReason
import com.municorn.example.calls.api.entity.HttpCallException
import com.squareup.moshi.Moshi
import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.UnknownHostException

class GeneralResultCall<T>(
    private val moshi: Moshi,
    private val proxy: Call<T>,
) : Call<ApiResponse<T>> {
    override fun enqueue(callback: Callback<ApiResponse<T>>) {
        proxy.enqueue(
            object : Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    callback.onResponse(
                        this@GeneralResultCall,
                        Response.success(handleApi(moshi, response))
                    )
                }

                override fun onFailure(call: Call<T>, t: Throwable) {
                    callback.onResponse(this@GeneralResultCall, Response.success(handleError(t)))
                }
            },
        )
    }

    override fun execute(): Response<ApiResponse<T>> = throw NotImplementedError()
    override fun clone(): Call<ApiResponse<T>> = GeneralResultCall(moshi, proxy.clone())
    override fun request(): Request = proxy.request()
    override fun timeout(): Timeout = proxy.timeout()
    override fun isExecuted(): Boolean = proxy.isExecuted
    override fun isCanceled(): Boolean = proxy.isCanceled
    override fun cancel() = proxy.cancel()
}

private fun <T> handleApi(
    moshi: Moshi,
    response: Response<T>,
): ApiResponse<T> {
    return try {
        val body = response.body()
        if (response.isSuccessful && body != null) {
            ApiResponse.Success(body)
        } else {
            handleHttpError(moshi, response)
        }
    } catch (e: Throwable) {
        handleError(e)
    }
}

private fun <T> handleHttpError(moshi: Moshi, response: Response<T>): ApiResponse.Error<T> {
    return ApiResponse.Error(
        if (response.code() == HttpURLConnection.HTTP_INTERNAL_ERROR) {
            try {
                response.errorBody()
                    ?.let { moshi.adapter(GeneralBusinessError::class.java).fromJson(it.source()) }
                    ?.error
                    ?.let { HttpCallException(it.code, it.description) }
                    ?: HttpCallException(
                        httpErrorCode = response.code(),
                        message = response.message()
                    )
            } catch (e: Exception) {
                HttpCallException(httpErrorCode = response.code(), message = response.message(), e)
            }
        } else {
            HttpCallException(httpErrorCode = response.code(), message = response.message())
        },
    )
}

private fun <T> handleError(e: Throwable): ApiResponse.Error<T> {
    return ApiResponse.Error(
        when (e) {
            is HttpException -> HttpCallException(e.code(), e.message(), e)
            is UnknownHostException, is ConnectException -> {
                ConnectionCallException(
                    ConnectionCallExceptionReason.NoConnection,
                    "No internet connection",
                    e
                )
            }
            else -> {
                ConnectionCallException(
                    ConnectionCallExceptionReason.Unknown,
                    "Unknown network request exception",
                    e
                )
            }
        },
    )
}
