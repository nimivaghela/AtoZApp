package com.app.atoz.shareddata.base

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import androidx.annotation.NonNull
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import com.app.atoz.R
import com.app.atoz.common.extentions.getWidthOfScreen
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.github.pwittchen.reactivenetwork.library.rx2.internet.observing.InternetObservingSettings
import com.github.pwittchen.reactivenetwork.library.rx2.internet.observing.strategy.SocketInternetObservingStrategy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

abstract class BaseDialogFragment : DialogFragment(), BaseView {

    /**
     * layout resource file
     */
    abstract fun getResource(): Int

    abstract fun initViewModel()

    /**
     * to call API or bind adapter
     */
    abstract fun postInit()

    /**
     * to define all listener
     */
    abstract fun handleListener()

    /**
     * to display error message
     */
    abstract fun displayMessage(message: String)

    var isInternetConnected: Boolean = true
    private lateinit var mBinding: ViewDataBinding
    lateinit var mDisposable: CompositeDisposable


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mDisposable = CompositeDisposable()
        initConnectivity()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mDisposable.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        mDisposable.dispose()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        mBinding = DataBindingUtil
            .inflate(LayoutInflater.from(context), getResource(), null, false)
        dialog.setContentView(mBinding.root)
        initViewModel()
        return dialog

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        postInit()
        handleListener()
    }

    @Suppress("UNCHECKED_CAST")
    @NonNull
    protected fun <T : ViewDataBinding> getBinding(): T {
        return mBinding as T
    }

    override fun onResume() {
        super.onResume()
        /**
         * setup the width of dialog programmatically
         */
        val params = dialog?.window?.attributes
        params?.let { dialogParams ->
            context?.getWidthOfScreen()?.let {
                dialogParams.width = (it * 0.95).toInt()
                dialog?.window?.attributes = params
            }
        }
        /**
         * Prevent dialog dismiss on outside click
         */
        isCancelable = true
    }


    private fun initConnectivity() {
        val settings = InternetObservingSettings.builder()
            .host("www.google.com")
            .strategy(SocketInternetObservingStrategy())
            .interval(3000)
            .build()

        ReactiveNetwork
            .observeInternetConnectivity(settings)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { isConnectedToHost ->
                isInternetConnected = isConnectedToHost
            }.addTo(mDisposable)
    }

    override fun onUnknownError(error: String?) {
        error?.let {
            Timber.d("Base Activity $error")
            displayMessage(error)
        }
    }

    override fun internalServer() {
        Timber.d("Base Activity API Internal server")
        displayMessage(getString(R.string.text_error_internal_server))
    }

    override fun onTimeout() {
        Timber.d("Base Activity API Timeout")
        displayMessage(getString(R.string.text_error_timeout))
    }

    override fun onNetworkError() {
        Timber.d("Base Activity network error")
        displayMessage(getString(R.string.text_error_network))
    }

    override fun onConnectionError() {
        Timber.d("Base Activity internet issue")
        displayMessage(getString(R.string.text_error_connection))
    }
}