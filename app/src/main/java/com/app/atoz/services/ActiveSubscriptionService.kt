package com.app.atoz.services

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.Observer
import com.app.atoz.AppApplication
import com.app.atoz.db.database.AppDatabase
import com.app.atoz.models.UserHolder
import com.app.atoz.shareddata.repo.SubscriptionRepo
import com.app.atoz.ui.user.subscription.SubscriptionViewModel
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.github.pwittchen.reactivenetwork.library.rx2.internet.observing.InternetObservingSettings
import com.github.pwittchen.reactivenetwork.library.rx2.internet.observing.strategy.SocketInternetObservingStrategy
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class ActiveSubscriptionService : LifecycleService() {
    companion object {
        fun startService(context: Context) {
            context.startService(Intent(context, ActiveSubscriptionService::class.java))
        }
    }

    private lateinit var mDisposable: CompositeDisposable
    var isInternetConnected: Boolean = true
    private lateinit var mSubscriptionViewModel: SubscriptionViewModel

    @Inject
    lateinit var mSubscriptionRepo: SubscriptionRepo

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
        Timber.d("ActiveSubscription Room service called")
        try {
            mSubscriptionViewModel = SubscriptionViewModel(mSubscriptionRepo)
            mSubscriptionViewModel.getActiveSubscriptionObserver()
                .observe(this, Observer { requestState ->
                    requestState.data?.let {
                        it.data?.let { it1 ->
                            AppDatabase.getInstance(this).activeSubscriptionPlanDao().insert(it1)
                        }
                        Timber.d("ActiveSubscription Room service list  ")
                        stopSelf()
                    }
                })
            mSubscriptionViewModel.getActiveSubscriptionData(isInternetConnected, mDisposable)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return START_NOT_STICKY
    }
}