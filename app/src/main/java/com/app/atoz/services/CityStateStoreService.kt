package com.app.atoz.services

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.Observer
import com.app.atoz.AppApplication
import com.app.atoz.db.database.AppDatabase
import com.app.atoz.models.UserHolder
import com.app.atoz.shareddata.repo.SignUpRepo
import com.app.atoz.ui.auth.signup.SignUpViewModel
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.github.pwittchen.reactivenetwork.library.rx2.internet.observing.InternetObservingSettings
import com.github.pwittchen.reactivenetwork.library.rx2.internet.observing.strategy.SocketInternetObservingStrategy
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class CityStateStoreService : LifecycleService() {
    companion object {
        fun startService(context: Context) {
            Timber.d("Start service companion called")
            context.startService(Intent(context, CityStateStoreService::class.java))
        }
    }

    private lateinit var mSignUpViewModel: SignUpViewModel
    @Inject
    lateinit var mUserHolder: UserHolder
    private lateinit var mDisposable: CompositeDisposable
    var isInternetConnected: Boolean = true

    @Inject
    lateinit var mSignUpRepo: SignUpRepo

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        mSignUpViewModel = SignUpViewModel(mSignUpRepo)
        mSignUpViewModel.getCitiesLiveData().observe(this, Observer { requestState ->
            requestState.data?.let {
                it.data?.cities?.let { cityList ->
                    AppDatabase.getInstance(applicationContext).cityStateDao().insertAll(cityList)
                }
                stopSelf()
            }
        })
        mSignUpViewModel.mTimerRequestModel.observe(this, Observer {
           // no need to call stop self
        })

        mSignUpViewModel.fetchTimerValue(isInternetConnected, mDisposable)
        mSignUpViewModel.provideCities(isInternetConnected, null, mDisposable)

        return START_NOT_STICKY
    }

    private fun initDisposable() {
        mDisposable = CompositeDisposable()
    }


    override fun onCreate() {
        super.onCreate()
        (application as AppApplication).mComponent.inject(this)
        initDisposable()
        initConnectivity()
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

}