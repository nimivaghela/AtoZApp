package com.app.atoz

import android.app.Application
import com.app.atoz.injection.component.AppComponent
import com.app.atoz.injection.component.DaggerAppComponent
import com.app.atoz.injection.module.AppModule
import com.app.atoz.injection.module.UserHolderModule
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import com.google.android.libraries.places.api.Places
import com.google.firebase.FirebaseApp
import io.fabric.sdk.android.Fabric
import timber.log.Timber


class AppApplication : Application() {
    companion object {
        lateinit var instance: AppApplication

        fun getAppContext(): AppApplication {
            return instance
        }
    }

    lateinit var mComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        instance = this
        FirebaseApp.initializeApp(this)
        Timber.plant(Timber.DebugTree())
        initDagger()

        Fabric.with(
            this,
            Crashlytics.Builder().core(CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build()).build()
        )

        Places.initialize(this, getString(R.string.GooglePlaceApiKey))

    }

    private fun initDagger() {
        mComponent = DaggerAppComponent.builder()
            .appModule(AppModule())
            .userHolderModule(UserHolderModule())
            .build()
    }
}