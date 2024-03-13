package com.app.atoz.services

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.Observer
import com.app.atoz.AppApplication
import com.app.atoz.models.UserHolder
import com.app.atoz.shareddata.repo.SignUpRepo
import com.app.atoz.ui.auth.signup.SignUpViewModel
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.github.pwittchen.reactivenetwork.library.rx2.internet.observing.InternetObservingSettings
import com.github.pwittchen.reactivenetwork.library.rx2.internet.observing.strategy.SocketInternetObservingStrategy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class UpdateFCMTokenService : LifecycleService() {

    companion object {
        private const val FCM_TOKEN = "fcm_token"
        fun startService(context: Context, fcmToken: String) {
            val intent = Intent(context, UpdateFCMTokenService::class.java)
            intent.putExtra(FCM_TOKEN, fcmToken)
            context.startService(intent)
        }
    }


    private lateinit var mSignUpViewModel: SignUpViewModel
    @Inject
    lateinit var mUserHolder: UserHolder
    private lateinit var mDisposable: CompositeDisposable
    var isInternetConnected: Boolean = true

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        mSignUpViewModel = SignUpViewModel(mSignUpRepo)
        mSignUpViewModel.mProfileRequestModel.observe(this, Observer { t ->
            t?.let {
                stopSelf()
            }

        })


        intent?.extras?.getString(FCM_TOKEN).let {
            if (it != null) {
                mSignUpViewModel.updateFCMToken(isInternetConnected, mDisposable, it)
            }
        }




        return START_NOT_STICKY
    }

    @Inject
    lateinit var mSignUpRepo: SignUpRepo

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
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { isConnectedToHost ->
                isInternetConnected = isConnectedToHost
            }.addTo(mDisposable)
    }
}