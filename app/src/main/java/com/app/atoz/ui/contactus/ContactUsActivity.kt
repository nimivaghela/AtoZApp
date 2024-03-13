package com.app.atoz.ui.contactus

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.app.atoz.R
import com.app.atoz.common.extentions.callPhoneIntent
import com.app.atoz.common.helper.RxHelper
import com.app.atoz.databinding.ActivityContactUsBinding
import com.app.atoz.shareddata.base.BaseActivity

class ContactUsActivity : BaseActivity() {
    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, ContactUsActivity::class.java))
        }
    }

    private lateinit var mBinding: ActivityContactUsBinding

    override fun getResource(): Int = R.layout.activity_contact_us

    override fun initView() {
        mBinding = getBinding()
        setToolbar(mBinding.contactUsToolbar.toolbar, getString(R.string.text_contact_us), true)
    }

    override fun handleListener() {
        RxHelper.onClick(mBinding.tvContactUsEmail, mDisposable) {
            val emailIntent = Intent(
                Intent.ACTION_SENDTO,
                Uri.fromParts("mailto", getString(R.string.text_contact_us_email), null)
            )
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Contact Us ")
            startActivity(Intent.createChooser(emailIntent, "Send email..."))
        }

        RxHelper.onClick(mBinding.tvContactUsPhone, mDisposable) {
            callPhoneIntent(getString(R.string.text_contact_us_phone))
        }
    }

    override fun displayMessage(message: String) {
    }
}