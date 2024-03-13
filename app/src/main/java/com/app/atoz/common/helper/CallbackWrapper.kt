package com.app.atoz.common.helper

import com.app.atoz.models.AtoZResponseModel
import com.app.atoz.shareddata.base.BaseView
import io.reactivex.observers.DisposableObserver
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException

/**
 *  To handle error code from one place
 */
abstract class CallbackWrapper<T : AtoZResponseModel<*>>(
    private val view: BaseView?
) : DisposableObserver<T>() {

    override fun onNext(response: T) {
        when {
            response.isSuccess == true -> {
                onApiSuccess(response)
            }
            response.status == 200 -> {
                onApiSuccess(response)
            }
            else -> {
                Timber.d("Unknown Error repo ${response.message}")
                response.message?.let {
                    view?.onUnknownError(response.message)
                } ?: let {
                    view?.internalServer()
                }
                onApiError(null)
            }
        }

    }

    override fun onError(e: Throwable) {
        Timber.d(
            "Error in API call ${e.printStackTrace()}"
        )
        when (e) {
            is HttpException -> {
                val responseBody = e.response()?.errorBody()
                view?.onUnknownError(getErrorMessage(responseBody))
            }
            is SocketTimeoutException -> view?.onTimeout()
            is ConnectException -> view?.onConnectionError()
            is IOException -> view?.onNetworkError()
            else -> {
                //TODO bifurcate user specific error
                view?.onUnknownError(e.message)
            }
        }
        onApiError(e)
    }

    override fun onComplete() {

    }

    internal abstract fun onApiSuccess(response: T)

    internal abstract fun onApiError(e: Throwable?)

    private fun getErrorMessage(responseBody: ResponseBody?): String? {
        return try {
            val jsonObject = JSONObject(responseBody!!.string())
            jsonObject.getString("message")
        } catch (e: Exception) {
            e.message
        }
    }
}