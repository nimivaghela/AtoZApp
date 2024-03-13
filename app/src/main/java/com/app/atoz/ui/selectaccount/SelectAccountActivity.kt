package com.app.atoz.ui.selectaccount

import android.content.Context
import android.content.Intent
import android.text.method.LinkMovementMethod
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.app.atoz.R
import com.app.atoz.common.extentions.changeTextStyle
import com.app.atoz.common.extentions.clickEvent
import com.app.atoz.common.extentions.highlightText
import com.app.atoz.common.helper.RxHelper
import com.app.atoz.databinding.ActivitySelectAccountBinding
import com.app.atoz.shareddata.base.BaseActivity
import com.app.atoz.ui.auth.signin.SignInActivity
import com.app.atoz.ui.auth.signup.SignUpActivity

class SelectAccountActivity : BaseActivity() {
    companion object {
        const val EXTRA_SOCIAL_USER = "EXTRA_SOCIAL_USER"

        fun start(context: Context) {
            context.startActivity(
                Intent(context, SelectAccountActivity::class.java)

            )
        }

        fun start(context: Context, soicalUserString: String) {
            context.startActivity(
                Intent(context, SelectAccountActivity::class.java)
                    .putExtra(EXTRA_SOCIAL_USER, soicalUserString)
            )
        }
    }

    override fun getResource(): Int = R.layout.activity_select_account

    private lateinit var mBinding: ActivitySelectAccountBinding

    override fun initView() {
        mBinding = getBinding()


        setToolbar(mBinding.selectAccountToolbar.toolbar, "", true, R.color.white)
        mBinding.tvAlreadyAccount.text =
            getString(R.string.text_already_account)
                .highlightText(25, 32, ContextCompat.getColor(this, R.color.colorPrimary))
                .changeTextStyle(25, 32, ResourcesCompat.getFont(this, R.font.proximanova_bold)!!)
                .clickEvent(25, 32, false) {
                    SignInActivity.start(this@SelectAccountActivity)
                    finish()
                }
        mBinding.tvAlreadyAccount.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun handleListener() {
        RxHelper.onClick(mBinding.chbUser, mDisposable) {
            mBinding.chbProvider.isChecked = false


            callNextActivity(SignUpActivity.TYPE_USER)

        }

        RxHelper.onClick(mBinding.chbProvider, mDisposable) {
            mBinding.chbUser.isChecked = false
            callNextActivity(SignUpActivity.TYPE_PROVIDER)

        }
    }

    private fun callNextActivity(userType: String) {
        if (intent.hasExtra(SignUpActivity.EXTRA_SOCIAL_USER)) {
            SignUpActivity.start(this@SelectAccountActivity, userType, intent.getStringExtra(EXTRA_SOCIAL_USER)!!)
        } else {
            SignUpActivity.start(this@SelectAccountActivity, userType)
        }
    }

    override fun displayMessage(message: String) {

    }
}