package com.app.atoz.ui.provider.existservice

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.app.atoz.AppApplication
import com.app.atoz.R
import com.app.atoz.common.extentions.showAlert
import com.app.atoz.common.extentions.snack
import com.app.atoz.models.ServiceModel
import com.app.atoz.shareddata.base.BaseActivity
import com.app.atoz.ui.provider.addservice.AddServicesActivity
import com.app.atoz.ui.provider.addservice.AddServicesActivity.Companion.KEY_ADD_SERVICE_ACTIVITY
import com.app.atoz.ui.provider.existservice.editservice.EditPersonalServiceActivity
import com.app.atoz.ui.provider.selectservice.SelectCategoryAdapter
import com.app.atoz.ui.provider.selectservice.SelectServiceAdapter
import com.app.atoz.utils.Config
import com.google.gson.Gson
import timber.log.Timber
import javax.inject.Inject

class ExistServiceActivity : BaseActivity(), SelectServiceAdapter.OnServiceItemClickListener {

    companion object {
        const val KEY_EXIST_SERVICE = 6000
        fun start(context: Activity) {
            context.startActivityForResult(Intent(context, ExistServiceActivity::class.java), KEY_EXIST_SERVICE)
        }
    }

    private var isServicesChanged = false

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var mBinding: com.app.atoz.databinding.ActivityExistServiceBinding

    private val mViewModel: ExistServiceViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory)[ExistServiceViewModel::class.java]
    }

    override fun getResource(): Int = R.layout.activity_exist_service

    override fun initView() {
        (application as AppApplication).mComponent.inject(this)
        mBinding = getBinding()
        setToolbar(mBinding.addServicesToolbar.toolbar, getString(R.string.text_your_services), true)

        initObserverOfViewModel()
        callGetService()
    }

    private fun callGetService() {
        mViewModel.getCategoryList(isInternetConnected, this, mDisposable)
    }

    override fun handleListener() {
    }

    private fun initObserverOfViewModel() {
        mViewModel.getCategoryRequest().observe(this, Observer { response ->
            response?.let { requestState ->
                showLoadingIndicator(requestState.progress)
                requestState.data?.let {
                    Timber.d("Successfully done")
                    it.data?.providerExistService?.let {
                        mBinding.rvServices.adapter = SelectCategoryAdapter(
                            it, this, false
                        )
                    }
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

        mViewModel.getDeleteServiceObserver().observe(this, Observer { response ->
            response?.let { requestState ->
                showLoadingIndicator(requestState.progress)
                requestState.data?.let {
                    Timber.d("Successfully done")
                    isServicesChanged = true
                    displayMessage(getString(R.string.text_delete_service_success))
                    callGetService()
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_exist_service, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menuAdd -> {
                val response = mViewModel.getCategoryRequest().value?.data?.data
                response?.let {
                    val responseJson = Gson().toJson(response)
                    AddServicesActivity.start(this, responseJson)
                }
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            KEY_ADD_SERVICE_ACTIVITY -> {
                if (resultCode == Activity.RESULT_OK) {
                    isServicesChanged = true
                    callGetService()
                }
            }
        }
    }

    override fun displayMessage(message: String) {
        mBinding.root.snack(message)
    }

    override fun updateCounter() {
        //No need to implement
    }

    override fun scrollAtPosition(position: Int) {
        mBinding.rvServices.smoothScrollToPosition(position)
    }

    override fun onItemClick(view: View, position: Int, serviceModel: ServiceModel) {
        when (view.id) {
            R.id.ivDelete -> {
                this@ExistServiceActivity.showAlert(getString(R.string.dialog_title_confirmation),
                    getString(R.string.confirm_msg_delete_service),
                    getString(R.string.text_yes),
                    getString(R.string.text_no),
                    DialogInterface.OnClickListener { dialogInterface, button ->
                        if (button == DialogInterface.BUTTON_POSITIVE) {
                            dialogInterface?.dismiss()
                            mViewModel.callDeleteService(
                                serviceModel.serviceId.toString(),
                                isInternetConnected,
                                this,
                                mDisposable
                            )
                        } else {
                            dialogInterface?.dismiss()
                        }
                    })
            }
            else -> {
                EditPersonalServiceActivity.start(this@ExistServiceActivity, serviceModel.serviceId.toString())
            }
        }
    }

    override fun onBackPressed() {
        if (isServicesChanged) {
            setResult(Activity.RESULT_OK)
            finish()
        } else {
            super.onBackPressed()
        }
    }
}
