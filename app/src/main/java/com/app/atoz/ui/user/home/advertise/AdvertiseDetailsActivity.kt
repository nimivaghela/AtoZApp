package com.app.atoz.ui.user.home.advertise

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.method.ScrollingMovementMethod
import android.view.View
import com.app.atoz.common.helper.RxHelper
import com.app.atoz.databinding.ActivityAdvertiseDetailsBinding
import com.app.atoz.models.AdvertisementItem
import com.app.atoz.shareddata.base.BaseActivity
import com.app.atoz.ui.user.home.AdvertiseVideoDialog
import com.google.gson.Gson


class AdvertiseDetailsActivity : BaseActivity() {

    companion object {
        private const val KEY_ADVERTISEMENT_ITEM = "AdvertiseData"

        fun newIntent(context: Context, advertisementItem: AdvertisementItem) {
            context.startActivity(
                Intent(context, AdvertiseDetailsActivity::class.java)
                    .putExtra(KEY_ADVERTISEMENT_ITEM, Gson().toJson(advertisementItem))
            )
        }
    }

    private lateinit var mBinding: ActivityAdvertiseDetailsBinding
    private lateinit var mAdvertisementItem: AdvertisementItem

    override fun getResource(): Int = com.app.atoz.R.layout.activity_advertise_details

    override fun initView() {
        mBinding = getBinding()
        setToolbar(mBinding.includeToolbar.toolbar, getString(com.app.atoz.R.string.text_title_advertise_details), true)
        mAdvertisementItem =
            Gson().fromJson(intent.getStringExtra(KEY_ADVERTISEMENT_ITEM), AdvertisementItem::class.java)
        mBinding.advertise = mAdvertisementItem
        mBinding.tvRedirectUrl.visibility =
            if (mAdvertisementItem.redirectUrl.isNullOrBlank()) View.GONE else View.VISIBLE
        mBinding.tvAdvertiseContent.text =
            if (mAdvertisementItem.description.isNullOrBlank()) getString(com.app.atoz.R.string.text_no_advertise_content)
            else mAdvertisementItem.description
        mBinding.tvAdvertiseContent.movementMethod = ScrollingMovementMethod()
    }

    override fun handleListener() {
        RxHelper.onClick(mBinding.tvRedirectUrl, mDisposable) {
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(mAdvertisementItem.redirectUrl))
                startActivity(browserIntent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        RxHelper.onClick(mBinding.ivAdvertise, mDisposable) {
            if (mAdvertisementItem.mimeType == 2) {
                val fragmentTransaction = supportFragmentManager.beginTransaction()
                val previousFragment =
                    supportFragmentManager.findFragmentByTag(AdvertiseVideoDialog.ADVERTISE_VIDEO_DIALOG_TAG)
                previousFragment?.let {
                    fragmentTransaction.remove(previousFragment)
                }
                fragmentTransaction.addToBackStack(null)
                val advertiseDialog = mAdvertisementItem.mediaUrl?.let { AdvertiseVideoDialog.newInstance(it) }
                fragmentTransaction.let { it1 ->
                    advertiseDialog?.show(it1, AdvertiseVideoDialog.ADVERTISE_VIDEO_DIALOG_TAG)
                }
            }
        }
    }

    override fun displayMessage(message: String) {

    }
}