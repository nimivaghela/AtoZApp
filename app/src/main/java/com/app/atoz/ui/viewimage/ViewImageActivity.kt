package com.app.atoz.ui.viewimage

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import com.app.atoz.common.extentions.loadImage
import com.app.atoz.common.extentions.loadImageWithProgressBar
import com.app.atoz.common.extentions.snack
import com.app.atoz.common.helper.RxHelper
import com.app.atoz.databinding.ActivityViewImageBinding
import com.app.atoz.shareddata.base.BaseActivity
import com.bogdwellers.pinchtozoom.ImageMatrixTouchHandler
import java.io.File


class ViewImageActivity : BaseActivity() {
    companion object {
        private const val KEY_IMAGE_URL = "ImageUrl"
        private const val KEY_FILE = "File"
        const val KEY_VIEW_IMAGE = 7000
        fun start(context: Context, url: String) {
            context.startActivity(
                Intent(context, ViewImageActivity::class.java)
                    .putExtra(KEY_IMAGE_URL, url)
            )
        }

        fun start(context: Activity, file: File) {
            context.startActivityForResult(
                Intent(context, ViewImageActivity::class.java)
                    .putExtra(KEY_FILE, file),
                KEY_VIEW_IMAGE
            )
        }
    }

    override fun getResource(): Int = com.app.atoz.R.layout.activity_view_image

    private lateinit var mBinding: ActivityViewImageBinding

    override fun initView() {
        mBinding = getBinding()
        if (intent.hasExtra(KEY_IMAGE_URL))
            mBinding.ivViewImage.loadImageWithProgressBar(intent.getStringExtra(KEY_IMAGE_URL)!!, mBinding.progressBar)
        else if (intent.hasExtra(KEY_FILE))
            intent.extras?.get(KEY_FILE)?.let {
                val file: File = it as File
                mBinding.ivViewImage.loadImage(file)
            }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun handleListener() {
        RxHelper.onClick(mBinding.ibClose, mDisposable) {
            setResult(Activity.RESULT_OK)
            finish()
        }
        mBinding.ivViewImage.setOnTouchListener(ImageMatrixTouchHandler(this@ViewImageActivity))
    }

    override fun displayMessage(message: String) {
        mBinding.root.snack(message)
    }

    override fun onBackPressed() {
        if (intent.hasExtra(KEY_FILE)) {
            setResult(Activity.RESULT_OK)
            finish()
        } else {
            super.onBackPressed()
        }
    }
}