package com.app.atoz.ui.auth.signin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.atoz.models.IsSocialMediaExistResponse
import com.app.atoz.models.RequestState
import com.app.atoz.models.SignUpSignInResponse
import com.app.atoz.models.SocialLogin
import com.app.atoz.shareddata.base.BaseView
import com.app.atoz.shareddata.repo.SignInRepo
import com.google.gson.JsonObject
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class SignInViewModel @Inject constructor(private val mSignInRepo: SignInRepo) : ViewModel() {

    private val mLDSignInRequest = MutableLiveData<RequestState<SignUpSignInResponse>>()

    private val mLDSocialIdExist = MutableLiveData<RequestState<IsSocialMediaExistResponse>>()

    fun getSignInRequest(): LiveData<RequestState<SignUpSignInResponse>> = mLDSignInRequest

    fun getSocialIdExist(): LiveData<RequestState<IsSocialMediaExistResponse>> = mLDSocialIdExist

    var isSocialIdExist = false
    var socialType: Int = -1
    var socialLoginModel: SocialLogin? = null


    fun doSignIn(
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable,
        email: String,
        password: String,
        deviceToken: String
    ) {

        val signInJson = JsonObject()
        signInJson.addProperty("email", email)
        signInJson.addProperty("password", password)
        signInJson.addProperty("device_token", deviceToken)
        signInJson.addProperty("deviceType", "android")


        mSignInRepo.signIn(signInJson, isInternetConnected, baseView, disposable, mLDSignInRequest)
    }


    fun doSocialLogin(
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable,
        socialLogin: SocialLogin

    ) {
        mSignInRepo.socialLogin(socialLogin, isInternetConnected, baseView, disposable, mLDSignInRequest)
    }

    fun doCheckingSocialIdExist(
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable
    ) {
        val checkSocialInJson = JsonObject()
        checkSocialInJson.addProperty("provider", socialLoginModel?.socialType)
        checkSocialInJson.addProperty("social_id", socialLoginModel?.socialId)
        mSignInRepo.doCheckIsSocialIDExist(
            checkSocialInJson,
            isInternetConnected,
            baseView,
            disposable,
            mLDSocialIdExist
        )
    }
}