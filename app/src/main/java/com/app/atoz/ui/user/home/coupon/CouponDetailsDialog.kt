package com.app.atoz.ui.user.home.coupon

import com.app.atoz.R
import com.app.atoz.databinding.DialogCouponDetailsBinding
import com.app.atoz.models.homescreen.CouponItem
import com.app.atoz.shareddata.base.BaseDialogFragment

class CouponDetailsDialog(private val couponItem: CouponItem) : BaseDialogFragment() {

    companion object {
        const val COUPON_DETAILS_DIALOG_TAG = "CouponDetailsDialog"
        fun newInstance(couponItem: CouponItem): CouponDetailsDialog {
            return CouponDetailsDialog(couponItem)
        }
    }

    private lateinit var mBinding: DialogCouponDetailsBinding

    override fun getResource(): Int = R.layout.dialog_coupon_details

    override fun initViewModel() {
        // no need to implement it
    }

    override fun postInit() {
        mBinding = getBinding()
        mBinding.coupon = couponItem
    }

    override fun handleListener() {
        // no need to implement it
    }

    override fun displayMessage(message: String) {
        // no need to implement it
    }
}