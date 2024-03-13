package com.app.atoz.common.helper

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.LifecycleObserver
import com.app.atoz.common.listeners.SocailCallBackListener
import com.app.atoz.models.SocialLogin
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import timber.log.Timber


object GooglePlusManager : LifecycleObserver {

//    {"provider":"google","deviceToken":"123456","deviceType":"android","email":"android.testapps@gmail.com","firstname":"Terry Android","lastname":"Terry","socialID":"117864610105474088738"}


    const val SOCIAL_GOOGLE_PLUS = 2
    val RC_SIGN_IN = 1001
    var mListener: SocailCallBackListener? = null
    var mGoogleSignInClient: GoogleSignInClient? = null

    private val gso: GoogleSignInOptions by lazy {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
    }

    fun doLogin(mContext: Activity) {
        mListener = mContext as SocailCallBackListener
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        mGoogleSignInClient = GoogleSignIn.getClient(mContext, gso)
        val acct = GoogleSignIn.getLastSignedInAccount(mContext)
        if (acct != null) {
            getProfileData(acct)
        } else {

            mGoogleSignInClient?.let {
                val signInIntent = it.signInIntent
                mContext.startActivityForResult(signInIntent, RC_SIGN_IN)
            }

        }

    }

    fun doLogout() {
        mGoogleSignInClient?.signOut()?.addOnCompleteListener(OnCompleteListener<Void> { task ->
            mListener?.signout(task.isSuccessful)

        })
    }

    fun onActivityResult(requestCode: Int, data: Intent) {
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)

            // Signed in successfully, show authenticated UI.
            getProfileData(account)
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Timber.tag("GooglePlusManager").w("signInResult:failed code=" + e.statusCode)
            if (!e.localizedMessage.contains("${e.statusCode}"))
                mListener?.let {
                    it.onSocialFailed(
                        SOCIAL_GOOGLE_PLUS, e.statusCode,
                        e.localizedMessage
                    )
                }
        }
    }

    fun getProfileData(account: GoogleSignInAccount?) {
        account?.run {
            /* val personName = this.displayName
             val personGivenName = this.givenName
             val personFamilyName = this.familyName
             val personEmail = this.email
             val personId = this.id
             val personPhoto = this.photoUrl*/

            val mDisplayName:String = if (this.displayName.isNullOrEmpty())  "" else this.displayName!!
            val mGivenName:String = if (this.givenName.isNullOrEmpty())  "" else this.givenName!!


            val user = SocialLogin(
                email = this.email!!, firstName =mDisplayName,lastName= mGivenName,
                socialType = "google", socialId = this.id!!, url = this.photoUrl.toString()
            )

            mListener?.onSocialSuccess(SOCIAL_GOOGLE_PLUS, 200, user)
        }
    }
}