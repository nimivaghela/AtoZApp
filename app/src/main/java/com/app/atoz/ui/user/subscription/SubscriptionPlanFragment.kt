package com.app.atoz.ui.user.subscription

import android.os.Bundle
import android.view.View
import com.app.atoz.R
import com.app.atoz.common.extentions.snack
import com.app.atoz.common.helper.RxHelper
import com.app.atoz.databinding.FragmentSubscriptionPlanBinding
import com.app.atoz.models.SubscriptionModel
import com.app.atoz.shareddata.base.BaseFragment
import com.app.atoz.ui.user.subscriptionpayment.SubscriptionPaymentActivity
import com.google.gson.Gson

class SubscriptionPlanFragment : BaseFragment() {

    companion object {
        private const val KEY_SUBSCRIPTION_MODEL = "SubscriptionModel"
        private const val KEY_CURRENT_PLAN_EXIST = "CurrentPlanExist"

        fun newInstance(subscriptionModel: SubscriptionModel, isCurrentPlanExist: Boolean): SubscriptionPlanFragment {
            val fragment = SubscriptionPlanFragment()
            val bundle = Bundle()
            bundle.putString(KEY_SUBSCRIPTION_MODEL, Gson().toJson(subscriptionModel))
            bundle.putBoolean(KEY_CURRENT_PLAN_EXIST, isCurrentPlanExist)
            fragment.arguments = bundle
            return fragment
        }
    }

    private var isCurrentPlanExist: Boolean = false
    lateinit var mSubscriptionModel: SubscriptionModel

    override fun getInflateResource(): Int = R.layout.fragment_subscription_plan

    private lateinit var mBinding: FragmentSubscriptionPlanBinding

    override fun initView() {
        mBinding = getBinding()
    }

    override fun postInit() {
        arguments?.let {
            mSubscriptionModel = Gson().fromJson(it.getString(KEY_SUBSCRIPTION_MODEL), SubscriptionModel::class.java)
            mBinding.subscription = mSubscriptionModel
            mSubscriptionModel.subscriptionServiceList?.apply {
                var servicesNames = ""
                for (serviceItem in this) {
                    servicesNames = "$servicesNames  • ${serviceItem.serviceName}"
                }
                mBinding.tvServicesName.text = servicesNames
            }
            isCurrentPlanExist = it.getBoolean(KEY_CURRENT_PLAN_EXIST, false)
            mBinding.btnBuy.visibility = if (isCurrentPlanExist) View.GONE else View.VISIBLE
            mBinding.tvPlanPrice.visibility = if (isCurrentPlanExist) View.VISIBLE else View.GONE
            mBinding.tvPlanPriceValue.visibility = if (isCurrentPlanExist) View.VISIBLE else View.GONE
        }
    }

    override fun handleListener() {
        RxHelper.onClick(mBinding.btnBuy, mDisposable) {
            activity?.let { SubscriptionPaymentActivity.start(it, mSubscriptionModel) }
        }
    }

    override fun displayMessage(message: String) {
        mBinding.llRootView.snack(message)
    }
}