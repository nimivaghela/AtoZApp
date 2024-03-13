package com.app.atoz.ui.myorders

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.app.atoz.AppApplication
import com.app.atoz.R
import com.app.atoz.common.extentions.gone
import com.app.atoz.common.extentions.snack
import com.app.atoz.common.extentions.visible
import com.app.atoz.databinding.ChildFragmentMyOrderBinding
import com.app.atoz.models.UserServiceStatusList
import com.app.atoz.shareddata.base.BaseFragment
import com.app.atoz.ui.myorders.orderdetails.OrderDetailsActivity
import com.app.atoz.ui.myorders.orderdetails.OrderDetailsActivity.Companion.KEY_ORDER_DETAILS_ACTIVITY
import com.app.atoz.utils.Config
import timber.log.Timber
import javax.inject.Inject

class MyOrdersChildFragment : BaseFragment(), MyOrdersAdapter.OnOrderItemClickListener {


    /*1 - Pending
      2 - Confirmed
      3 - In Progress
      4 - Completed
      5 - Cancelled
      0- All
  */

    companion object {
        private const val KEY_ORDER_TYPE = "OrderType"
        fun newInstance(type: String): MyOrdersChildFragment {
            val bundle = Bundle()
            bundle.putString(KEY_ORDER_TYPE, type)
            val fragment = MyOrdersChildFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory

    private val mViewModel: MyOrdersViewModel by lazy {
        ViewModelProviders.of(this, mViewModelFactory)[MyOrdersViewModel::class.java]
    }


    override fun getInflateResource(): Int = R.layout.child_fragment_my_order


    private lateinit var mBinding: ChildFragmentMyOrderBinding

    override fun initView() {
        (activity!!.application as AppApplication).mComponent.inject(this)
        mBinding = getBinding()
        mBinding.viewModel = mViewModel
        mBinding.swMyOrders.setOnRefreshListener {
            refreshPage()
        }
        mBinding.swMyOrders.setColorSchemeResources(
            R.color.colorPrimaryDark,
            R.color.colorAccent,
            R.color.colorPrimaryDark,
            R.color.colorAccent
        )

        initObserver()
    }

    private fun refreshPage() {
        mBinding.swMyOrders.isRefreshing = true
        postInit()
    }

    override fun postInit() {
        mViewModel.mOrderType = arguments?.getString(KEY_ORDER_TYPE)!!
        mViewModel.getOrderStatusList(mViewModel.mOrderType, isInternetConnected, this, mDisposable)
    }

    override fun handleListener() {

    }

    override fun displayMessage(message: String) {
        mBinding.llRootView.snack(message)
    }

    private fun showLoadingIndicator(progress: Boolean) {

        if (progress) {

            if (!mBinding.swMyOrders.isRefreshing) {
                mBinding.progressBar.visibility = if (progress) View.VISIBLE else View.GONE
            }
        } else {
            mBinding.swMyOrders.isRefreshing = false
            mBinding.progressBar.visibility = if (progress) View.VISIBLE else View.GONE
        }
    }

    private fun initObserver() {
        mViewModel.getOrderStatusList().observe(this, Observer { requestState ->
            showLoadingIndicator(requestState.progress)
            requestState.data?.let {
                Timber.d("Successfully done")
                it.let {
                    mViewModel.mOrderList = it.data?.serviceRequest as ArrayList<UserServiceStatusList.ServiceResponse>
                }
                bindViewData()
            }
            requestState.error?.let { errorObj ->
                Timber.d("Successfully Error showing")
                when (errorObj.errorState) {
                    Config.NETWORK_ERROR ->
                        displayMessage(getString(R.string.text_error_network))
                    else ->
                        errorObj.customMessage
                            ?.let { displayMessage(it) }
                }
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun bindViewData() {
        mBinding.rvMyOrders.adapter = MyOrdersAdapter(
            mViewModel.mOrderList,
            mViewModel.mUserHolder,
            this
        )

        if (mViewModel.mOrderList.size > 0) {
            mBinding.tvErrorEmpty.gone()
        } else {
            mBinding.tvErrorEmpty.visible()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == KEY_ORDER_DETAILS_ACTIVITY) {
            if (resultCode == Activity.RESULT_OK) {
                refreshPage()
            }
        }
    }

    override fun onClick(data: UserServiceStatusList.ServiceResponse) {
        activity?.let {
            data.id?.let { it1 -> OrderDetailsActivity.start(it, it1.toString()) }
        }

    }

}