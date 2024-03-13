package com.app.atoz.ui.notification

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.app.atoz.AppApplication
import com.app.atoz.R
import com.app.atoz.databinding.ActivityNotificationsBinding
import com.app.atoz.shareddata.base.BaseActivity
import com.app.atoz.ui.myorders.orderdetails.OrderDetailsActivity
import com.app.atoz.utils.Config
import com.app.atoz.utils.DividerItemDecoration
import timber.log.Timber
import javax.inject.Inject

class NotificationsActivity : BaseActivity() {
    override fun getResource(): Int {
        return R.layout.activity_notifications
    }

    companion object {
        fun start(
            context: Context
        ) {
            val categoryIntent = Intent(context, NotificationsActivity::class.java)
            context.startActivity(categoryIntent)
        }
    }

    private val mViewModel: NotificationViewModel by lazy {
        ViewModelProviders
            .of(this, viewModelFactory)[NotificationViewModel::class.java]
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var mBinding: ActivityNotificationsBinding
    override fun initView() {
        (application as AppApplication).mComponent.inject(this)
        mBinding = getBinding()
        setToolbar(mBinding.includeToolbar.toolbar, getString(R.string.notification), true)

    }

    override fun handleListener() {

        mViewModel.getNotificationObserver().observe(this, Observer { requestState ->
            showLoadingIndicator(requestState.progress)

            requestState.data?.let { data ->
                Timber.d("Data rendered ")
                data.data?.let { response ->

                    mBinding.rcvNotification.adapter = response.data?.let {
                        NotificationAdapter(
                            it,
                            object : NotificationAdapter.NotificationClickListener {
                                override fun onClick(orderID: String) {
                                    if (orderID.isEmpty().not()) {
                                        OrderDetailsActivity.start(this@NotificationsActivity, orderID)
                                    }
                                }

                            })
                    }
                    mBinding.rcvNotification.addItemDecoration(DividerItemDecoration(this, R.drawable.item_divider))
                }
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

        mViewModel.populateNotificationData(isInternetConnected, this, mDisposable)
    }

    private fun showLoadingIndicator(isShow: Boolean) {
        mBinding.progressBar.visibility = if (isShow) View.VISIBLE else View.GONE
    }

    override fun displayMessage(message: String) {

    }


}
