package com.app.atoz.common.helper

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.app.atoz.AppApplication
import com.app.atoz.R
import com.app.atoz.common.listeners.SocailCallBackListener
import com.app.atoz.models.SocialLogin
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import org.json.JSONException
import java.util.*

/*
    test facebook user
    email : open_hjralhe_user@tfbnw.net
    password : test1234
 */
object FacebookManager : LifecycleObserver
{

    const val SOCIAL_FACEBOOK =1
    private const val EMAIL = "email"
    private const val PUBLIC_PROFILE = "public_profile"
    private var callbackManager :CallbackManager?=null
    var mListener : SocailCallBackListener?=null
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun registerFacebookCallBackManager()
    {
        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().registerCallback(callbackManager,

                object: FacebookCallback<LoginResult>{
                    override fun onSuccess(result: LoginResult) {

                        if(result.accessToken!=null)
                        {
                            val deniedPermissions  =result.recentlyDeniedPermissions
                            if(deniedPermissions.contains(EMAIL))
                            {

                                mListener?.let {
                                    it.onSocialFailed(SOCIAL_FACEBOOK,500,AppApplication.getAppContext().getString(R.string.err_msg_email_permision_denied))
                                }
                            }else
                            {
                                getProfileData()
                            }

                        }
                    }

                    override fun onCancel() {
                        mListener?.let {
                            it.onSocialCancel(SOCIAL_FACEBOOK,400,AppApplication.getAppContext().getString(R.string.err_msg_email_permision_denied))
                        }
                    }

                    override fun onError(error: FacebookException?) {
                        mListener?.let {
                            if (error != null) {
                                it.onSocialFailed(SOCIAL_FACEBOOK,500,error.localizedMessage)
                            }
                        }
                    }

                }
            )
    }

    private fun getProfileData() {
        makeUserRequest(GraphRequest.GraphJSONObjectCallback { `object`, _ ->
            run {
                try {
                    val firstName = `object`.optString("first_name")
                    val lastName = `object`.optString("last_name")
                    val email = `object`.optString("email")
                    val url = `object`.getJSONObject("picture").getJSONObject("data").optString("url")
                    val userId = `object`.getString("id")

                    val user = SocialLogin(email =email,firstName =firstName, lastName =  lastName,
                        socialType = "facebook", socialId= userId,url = url
                    )

//                    loginListener.onSuccess(user)
                    mListener?.let {
                        it.onSocialSuccess(SOCIAL_FACEBOOK,500,user)
                    }

                } catch (e: JSONException) {
//                    loginListener.onError(loginListener.FB_ERROR, e.message ?: "")
                }


            }
        })


    }
    private fun makeUserRequest(callback: GraphRequest.GraphJSONObjectCallback) {
        val accessToken = AccessToken.getCurrentAccessToken()
        val request = GraphRequest.newMeRequest(
            accessToken, callback)
        val parameters = Bundle()
        parameters.putString("fields", "picture.width(500).height(500),name,id,email,first_name,last_name")
        request.parameters = parameters
        request.executeAsync()

    }
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun unRegisterFacebookCallManager()
    {
        LoginManager.getInstance().unregisterCallback(callbackManager)
    }

    fun doLogin(mContext:Activity)
    {
        doLogOut()
        LoginManager.getInstance().logInWithReadPermissions(mContext, Arrays.asList(EMAIL,PUBLIC_PROFILE))
    }

    fun doLogOut()
    {
        LoginManager.getInstance().logOut()
    }
    fun onActivityResult(requestCode:Int,  resultCode:Int,  data: Intent) {
        callbackManager?.onActivityResult(requestCode,resultCode,data)
    }
}