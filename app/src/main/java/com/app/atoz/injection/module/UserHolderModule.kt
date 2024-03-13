package com.app.atoz.injection.module

import com.app.atoz.AppApplication
import com.app.atoz.common.extentions.getPrefInstance
import com.app.atoz.models.UserHolder
import com.app.atoz.utils.Config
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class UserHolderModule {
    @Provides
    @Singleton
    fun provideUserHolder(): UserHolder =
        UserHolder(AppApplication.getAppContext().getPrefInstance(Config.USER_SHARED_PREFERENCE))
}