package com.app.atoz.ui.provider.selectservice

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.app.atoz.AppApplication
import com.app.atoz.R
import com.app.atoz.common.extentions.snack
import com.app.atoz.common.helper.RxHelper
import com.app.atoz.databinding.ActivitySelectServiceBinding
import com.app.atoz.models.ServiceModel
import com.app.atoz.models.SignUpRequestModel
import com.app.atoz.shareddata.base.BaseActivity
import com.app.atoz.ui.auth.verification.VerificationActivity
import com.app.atoz.utils.Config
import timber.log.Timber
import javax.inject.Inject

class SelectServiceActivity : BaseActivity(), SelectServiceAdapter.OnServiceItemClickListener {

    companion object {
        private const val KEY_SIGN_UP_REQUEST_DATA = "SignUpRequestData"

        fun start(context: Context, signUpRequestModel: SignUpRequestModel) {
            context.startActivity(
                Intent(context, SelectServiceActivity::class.java)
                    .putExtra(KEY_SIGN_UP_REQUEST_DATA, signUpRequestModel)
            )
        }
    }

    override fun getResource(): Int = R.layout.activity_select_service

    private val mViewModel: SelectServiceViewModel by lazy {
        ViewModelProviders
            .of(this, viewModelFactory)[SelectServiceViewModel::class.java]
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var mBinding: ActivitySelectServiceBinding
    private lateinit var mAdapter: SelectCategoryAdapter

    override fun initView() {
        (application as AppApplication).mComponent.inject(this)
        mBinding = getBinding()
        setToolbar(
            mBinding.selectedServicesToolbar.toolbar,
            getString(R.string.text_select_service),
            true
        )
        getSignUpRequestModel()
        initObserverOfViewModel()
        showCounterValue(0)

        getCategoryData()

    }

    private fun getCategoryData() {
        mViewModel.getCategoryList(isInternetConnected, this, mDisposable)
    }

    private fun getSignUpRequestModel() {
        mViewModel.mSignUpRequestModel = intent.getParcelableExtra(KEY_SIGN_UP_REQUEST_DATA)
    }

    private fun initObserverOfViewModel() {
        mViewModel.getSignUpRequest().observe(this, Observer { requestState ->
            showLoadingIndicator(requestState.progress)
            requestState.data?.let {
                VerificationActivity.start(this@SelectServiceActivity)
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

        mViewModel.getCategoryRequest().observe(this, Observer { requestState ->
            showLoadingIndicator(requestState.progress)
            requestState.data?.let {
                Timber.d("Data rendered ")
                it.data?.let {
                    mAdapter = SelectCategoryAdapter(it.service, this, true)
                    mBinding.rvCategory.adapter = mAdapter
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
    }

    override fun handleListener() {
        RxHelper.onClick(mBinding.btnSignUp, mDisposable) {
            if (mViewModel.mSignUpRequestModel != null) {
                if (mViewModel.mSelectedServicesId.size == 0) {
                    displayMessage(getString(R.string.text_error_select_service))
                } else {
                    mViewModel.doSignUpForProvider(isInternetConnected, this, mDisposable)
                }
            } else {
                displayMessage(getString(R.string.text_error_internal_server))
            }
        }
    }

    private fun showCounterValue(counter: Int) {
        mBinding.tvServicesSelected.text =
            String.format(getString(R.string.text_services_selected), counter)
    }

    override fun updateCounter() {
        showCounterValue(mViewModel.countCheckedServices())
    }

    override fun scrollAtPosition(position: Int) {
        mBinding.rvCategory.smoothScrollToPosition(position)
    }

    override fun displayMessage(message: String) {
        mBinding.rvRootView.snack(message)
    }

    private fun showLoadingIndicator(isShow: Boolean) {
        mBinding.progressBar.visibility = if (isShow) View.VISIBLE else View.GONE
    }

    override fun onItemClick(view: View, position: Int, serviceModel: ServiceModel) {
        //No need to implement
    }
}