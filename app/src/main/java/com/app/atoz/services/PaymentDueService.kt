package com.app.atoz.services

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.Observer
import com.app.atoz.AppApplication
import com.app.atoz.db.database.AppDatabase
import com.app.atoz.models.UserHolder
import com.app.atoz.shareddata.repo.PaymentRepo
import com.app.atoz.ui.user.paymentmethod.PaymentMethodViewModel
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.github.pwittchen.reactivenetwork.library.rx2.internet.observing.InternetObservingSettings
import com.github.pwittchen.reactivenetwork.library.rx2.internet.observing.strategy.SocketInternetObservingStrategy
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class PaymentDueService : LifecycleService() {
    companion object {
        fun startService(context: Context) {
            Timber.d("Start service companion called")
            context.startService(Intent(context, PaymentDueService::class.java))
        }
    }

    private lateinit var mDisposable: CompositeDisposable
    var isInternetConnected: Boolean = true
    private lateinit var mPaymentMethodViewModel: PaymentMethodViewModel
    @Inject
    lateinit var mPaymentRepo: PaymentRepo
    @Inject
    lateinit var mUserHolder: UserHolder

    override fun onCreate() {
        super.onCreate()
        (application as AppApplication).mComponent.inject(this)
        initDisposable()
        initConnectivity()
    }

    private fun initDisposable() {
        mDisposable = CompositeDisposable()
    }

    override fun onDestroy() {
        super.onDestroy()
        mDisposable.clear()
        mDisposable.dispose()
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
            .subscribe { isConnectedToHost ->
                isInternetConnected = isConnectedToHost
            }.addTo(mDisposable)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Timber.d("PaymentDue Room service called")
        try {
            val paymentList = AppDatabase.getInstance(this).paymentDueDao().getAllPaymentDue()

            if (paymentList.isNotEmpty()) {
                mPaymentMethodViewModel = PaymentMethodViewModel(mPaymentRepo, mUserHolder)
                mPaymentMethodViewModel.getPaymentRequest().observe(this, Observer { requestState ->
                    requestState.data?.let {
                        AppDatabase.getInstance(this).paymentDueDao().deleteAllData()
                        val dataList = AppDatabase.getInstance(this).paymentDueDao().getAllPaymentDue()
                        Timber.d("PaymentDue Room paymentDue list ${dataList.size}")
                        stopSelf()
                    }
                })
                mPaymentMethodViewModel.callOnlineTransactionApiFromService(
                    paymentList,
                    isInternetConnected,
                    mDisposable
                )
            } else {
                stopSelf()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return START_NOT_STICKY
    }

}