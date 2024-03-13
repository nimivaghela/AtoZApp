package com.app.atoz.ui.user.subscription

import android.os.Bundle
import com.app.atoz.R
import com.app.atoz.databinding.FragmentActiveSubscriptionPlanBinding
import com.app.atoz.models.CurrentPlanModel
import com.app.atoz.shareddata.base.BaseFragment
import com.google.gson.Gson

class ActiveSubscriptionPlanFragment : BaseFragment() {
    companion object {
        private const val KEY_ACTIVE_SUBSCRIPTION_PLAN_MODEL = "ActiveSubscriptionPlanModel"

        fun newInstance(subscriptionModel: CurrentPlanModel): ActiveSubscriptionPlanFragment {
            val fragment = ActiveSubscriptionPlanFragment()
            val bundle = Bundle()
            bundle.putString(KEY_ACTIVE_SUBSCRIPTION_PLAN_MODEL, Gson().toJson(subscriptionModel))
            fragment.arguments = bundle
            return fragment
        }
    }

    lateinit var mCurrentPlanModel: CurrentPlanModel

    override fun getInflateResource(): Int = R.layout.fragment_active_subscription_plan

    private lateinit var mBinding: FragmentActiveSubscriptionPlanBinding

    override fun displayMessage(message: String) {

    }

    override fun initView() {
        mBinding = getBinding()
    }

    override fun postInit() {
        arguments?.let {
            mCurrentPlanModel =
                Gson().fromJson(it.getString(KEY_ACTIVE_SUBSCRIPTION_PLAN_MODEL), CurrentPlanModel::class.java)
            mBinding.currentPlan = mCurrentPlanModel
            mCurrentPlanModel.subscriptionServiceList?.apply {
                var servicesNames = ""
                for (serviceItem in this) {
                    servicesNames = "$servicesNames  • ${serviceItem.serviceName}"
                }
                mBinding.tvActiveServicesName.text = servicesNames
            }
        }
    }

    override fun handleListener() {

    }
}