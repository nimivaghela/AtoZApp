package com.app.atoz.ui.splash

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.app.atoz.AppApplication
import com.app.atoz.R
import com.app.atoz.common.extentions.getPrefInstance
import com.app.atoz.models.UserHolder
import com.app.atoz.services.ActiveSubscriptionService
import com.app.atoz.services.CityStateStoreService
import com.app.atoz.services.PaymentDueService
import com.app.atoz.ui.main.MainActivity
import com.app.atoz.ui.welcome.WelcomeActivity
import com.app.atoz.utils.Config
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

class SplashActivity : AppCompatActivity() {
    private lateinit var mCoroutineScope: CoroutineScope

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        MobileAds.initialize(this, getString(R.string.AddMobAppId))

        MobileAds.initialize(this)
    }

    override fun onResume() {
        super.onResume()
        mCoroutineScope = CoroutineScope(Dispatchers.Main)
        mCoroutineScope.launch {
            CityStateStoreService.startService(applicationContext)
            /**
             * delay added for 2000 milliseconds to show splash screen
             */

            subscribeToTopic("All")
            delay(TimeUnit.SECONDS.toMillis(2))
            val userHolder: UserHolder? =
                UserHolder(
                    AppApplication.getAppContext().getPrefInstance(Config.USER_SHARED_PREFERENCE)
                )
            userHolder?.let {
                if (userHolder.isVerified) {
                    PaymentDueService.startService(this@SplashActivity)
                    ActiveSubscriptionService.startService(this@SplashActivity)
                    MainActivity.start(this@SplashActivity)
                } else {
                    userHolder.clearData()
                    WelcomeActivity.start(this@SplashActivity)
                }
            } ?: let {
                WelcomeActivity.start(this@SplashActivity)
            }
            finish()
            /**
             * to disable the transition animation
             */
            overridePendingTransition(0, 0)
        }
    }

    override fun onStop() {
        super.onStop()
        mCoroutineScope.cancel()
    }

    /**
     * Subscribe to a
     */
    private fun subscribeToTopic(topic: String) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
    }
}