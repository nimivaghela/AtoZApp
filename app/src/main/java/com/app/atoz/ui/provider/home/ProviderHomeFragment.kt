package com.app.atoz.ui.provider.home

import android.app.Activity
import android.content.Intent
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.app.atoz.AppApplication
import com.app.atoz.R
import com.app.atoz.common.extentions.snack
import com.app.atoz.databinding.FragmentProviderHomeBinding
import com.app.atoz.models.UserHolder
import com.app.atoz.models.providerhome.ServiceRequestItem
import com.app.atoz.shareddata.base.BaseFragment
import com.app.atoz.ui.notification.NotificationsActivity
import com.app.atoz.ui.provider.jobdetails.JobDetailsActivity
import com.app.atoz.ui.provider.jobdetails.JobDetailsActivity.Companion.KEY_JOB_DETAILS
import com.app.atoz.ui.user.home.ItemClickListener
import com.app.atoz.utils.Config
import timber.log.Timber
import javax.inject.Inject

class ProviderHomeFragment : BaseFragment(), ItemClickListener {
    override fun getInflateResource(): Int {
        return R.layout.fragment_provider_home
    }

    companion object {
        fun newInstance(): ProviderHomeFragment {
            return ProviderHomeFragment()
        }
    }

    private lateinit var mBinding: FragmentProviderHomeBinding

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var mUserHolder: UserHolder

    private val mProviderViewModel: ProviderHomeViewModel by lazy {
        ViewModelProviders
            .of(this, mViewModelFactory)[ProviderHomeViewModel::class.java]
    }

    override fun initView() {
        (requireActivity().application as AppApplication).mComponent.inject(this)
        mBinding = getBinding()
        setTitle(getString(R.string.title_home))
        setHasOptionsMenu(true)
        mBinding.userName = mUserHolder.firstName?.capitalize()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.provider_home_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        NotificationsActivity.start(requireContext())
        return super.onOptionsItemSelected(item)
    }

    override fun onItemClick(view: View, position: Int, data: Any?) {
        activity?.let {
            val model = data as ServiceRequestItem
            JobDetailsActivity.start(it, model.id.toString(), model.createdAt)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            KEY_JOB_DETAILS -> {
                if (resultCode == Activity.RESULT_OK) {
                    activity?.let {
                        populateHomeData()
                    }
                }
            }
        }
    }

    private fun showLoadingIndicator(progress: Boolean) {
        mBinding.progressBar.visibility = if (progress && !mBinding.pullToRefresh.isRefreshing) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    override fun postInit() {
        mBinding.rcvProviderPendingService.adapter = ProviderHomeAdapter(
            null, this, mUserHolder.timer
        )

        mProviderViewModel.getProviderRequestObserver().observe(this, Observer { request ->
            request?.let { requestState ->
                showLoadingIndicator(requestState.progress)
                requestState.data?.let { data ->
                    data.data?.serviceRequest?.let {
                        val providerHomeAdapter: ProviderHomeAdapter =
                            mBinding.rcvProviderPendingService.adapter as ProviderHomeAdapter
                        if (it.isNotEmpty()) {
                            providerHomeAdapter.setServiceRequestList(it)
                            mBinding.tvNoService.visibility = View.GONE
                        } else {
                            providerHomeAdapter.setServiceRequestList(null)
                            mBinding.tvNoService.visibility = View.VISIBLE
                        }
                    }
                }
                requestState.error?.let { errorObj ->
                    Timber.d("Successfully Error showing")
                    when (errorObj.errorState) {
                        Config.NETWORK_ERROR ->
                            displayMessage(getString(R.string.text_error_network))
                        Config.CUSTOM_ERROR ->
                            errorObj.customMessage
                                ?.let { it1 -> displayMessage(it1) }
                        else -> {
                        }
                    }
                    showLoadingIndicator(requestState.progress)

                }
                mBinding.pullToRefresh.isRefreshing = false
            }
        })

        populateHomeData()

    }

    private fun populateHomeData() {
        mProviderViewModel.populateProviderRequest(isInternetConnected, this, mDisposable)
    }

    override fun handleListener() {
        mBinding.pullToRefresh.setOnRefreshListener {
            populateHomeData()
        }
    }

    override fun displayMessage(message: String) {
        mBinding.llRootView.snack(message)
    }
}