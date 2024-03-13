package com.app.atoz.ui.provider.addservice


import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.app.atoz.AppApplication
import com.app.atoz.R
import com.app.atoz.common.extentions.resToast
import com.app.atoz.common.extentions.snack
import com.app.atoz.common.helper.RxHelper
import com.app.atoz.models.ServiceModel
import com.app.atoz.shareddata.base.BaseActivity
import com.app.atoz.ui.provider.selectservice.SelectCategoryAdapter
import com.app.atoz.ui.provider.selectservice.SelectServiceAdapter
import com.app.atoz.utils.Config
import timber.log.Timber
import javax.inject.Inject

class AddServicesActivity : BaseActivity(), SelectServiceAdapter.OnServiceItemClickListener {

    companion object {
        private const val KEY_EXIST_AND_ALL_SERVICE_LIST = "ExistAndAllServiceList"
        const val KEY_ADD_SERVICE_ACTIVITY = 5000

        fun start(context: Activity, responseJson: String) {
            context.startActivityForResult(
                Intent(context, AddServicesActivity::class.java)
                    .putExtra(KEY_EXIST_AND_ALL_SERVICE_LIST, responseJson),
                KEY_ADD_SERVICE_ACTIVITY
            )
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var mBinding: com.app.atoz.databinding.ActivityAddServicesBinding

    private val mViewModel: AddServiceViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory)[AddServiceViewModel::class.java]
    }

    override fun getResource(): Int = R.layout.activity_add_services

    override fun initView() {
        (application as AppApplication).mComponent.inject(this)
        mBinding = getBinding()
        setToolbar(mBinding.addServicesToolbar.toolbar, getString(R.string.text_title_add_service), true)
        initObserverOfViewModel()
        mViewModel.setCategoryResponseModel(intent.getStringExtra(KEY_EXIST_AND_ALL_SERVICE_LIST))
    }

    override fun handleListener() {
        RxHelper.onClick(mBinding.btnSave, mDisposable) {
            if (mViewModel.checkSelectedItems()) {
                mViewModel.callAddService(isInternetConnected, this, mDisposable)
            } else {
                displayMessage(getString(R.string.text_error_select_service))
            }
        }
    }

    private fun initObserverOfViewModel() {
        mViewModel.mNonSelectedServiceList.observe(this, Observer {
            mBinding.rvNewServices.adapter = SelectCategoryAdapter(it, this, true)
        })

        mViewModel.getAddServiceObserver().observe(this, Observer { response ->
            response?.let { requestState ->
                showLoadingIndicator(requestState.progress)
                requestState.data?.let {
                    Timber.d("Successfully done")
                    resToast(
                        resources.getQuantityString(
                            R.plurals.services_success,
                            mViewModel.mSelectedServicesId.size
                        )
                    )
                    setResult(Activity.RESULT_OK)
                    finish()
                }
                requestState.error?.let { errorObj ->
                    Timber.d("Successfully Error showing")
                    when (errorObj.errorState) {
                        Config.NETWORK_ERROR ->
                            displayMessage(getString(R.string.text_error_network))
                        else -> {
                            errorObj.customMessage
                                ?.let { displayMessage(it) }
                        }
                    }
                }
            }
        })
    }

    private fun showLoadingIndicator(isShow: Boolean) {
        mBinding.progressBar.visibility = if (isShow) View.VISIBLE else View.GONE
    }

    override fun displayMessage(message: String) {
        mBinding.root.snack(message)
    }

    override fun updateCounter() {
        // no need to implement
    }

    override fun scrollAtPosition(position: Int) {
        mBinding.rvNewServices.smoothScrollToPosition(position)
    }

    override fun onItemClick(view: View, position: Int, serviceModel: ServiceModel) {
        // no need to implement
    }
}
