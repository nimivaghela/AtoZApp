package com.app.atoz.ui.provider.existservice.editservice

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import com.app.atoz.AppApplication
import com.app.atoz.R
import com.app.atoz.common.extentions.resToast
import com.app.atoz.common.extentions.snack
import com.app.atoz.databinding.ActivityEditPersonalServiceBinding
import com.app.atoz.models.CategoryModel
import com.app.atoz.shareddata.base.BaseActivity
import com.app.atoz.utils.Config
import timber.log.Timber
import javax.inject.Inject


class EditPersonalServiceActivity : BaseActivity(), EditServiceAdapter.OnServiceItemClickListener {
    companion object {
         const val KEY_SERVICE_ID = "ServiceId"
        fun start(context: Context, serviceId: String) {
            context.startActivity(
                Intent(context, EditPersonalServiceActivity::class.java)
                    .putExtra(KEY_SERVICE_ID, serviceId)
            )
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val mViewModel: EditPersonalServiceViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory)[EditPersonalServiceViewModel::class.java]
    }

    override fun getResource(): Int = R.layout.activity_edit_personal_service

    private lateinit var mBinding: ActivityEditPersonalServiceBinding

    override fun initView() {
        (application as AppApplication).mComponent.inject(this)
        mBinding = getBinding()
        mViewModel.setServiceId(intent.getStringExtra(KEY_SERVICE_ID)!!)
        setToolbar(mBinding.includeToolbar.toolbar, getString(com.app.atoz.R.string.text_title_edit_service), true)
        mBinding.rvEditPersonalService.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        initObservable()
        callServiceList()
    }

    private fun callServiceList() {
        mViewModel.callChildService(isInternetConnected, this, mDisposable)
    }

    private fun initObservable() {
        mViewModel.getChildServiceObserver().observe(this, Observer { requestState ->
            showLoadingIndicator(requestState.progress)
            requestState.data?.let {
                Timber.d("Success")
                it.data?.service?.let {
                    mBinding.rvEditPersonalService.adapter = EditServiceAdapter(it, this)
                }
            }
            requestState.error?.let { errorObj ->
                Timber.d("Successfully Error showing")
                when (errorObj.errorState) {
                    Config.NETWORK_ERROR ->
                        displayMessage(getString(com.app.atoz.R.string.text_error_network))
                    else -> {
                        errorObj.customMessage
                            ?.let { displayMessage(it) }
                    }
                }
            }
        })

        mViewModel.getPriceChangeObserver().observe(this, Observer { requestState ->
            showLoadingIndicator(requestState.progress)
            requestState.data?.let {
                Timber.d("Success")
                callServiceList()
            }
            requestState.error?.let { errorObj ->
                Timber.d("Successfully Error showing")
                when (errorObj.errorState) {
                    Config.NETWORK_ERROR ->
                        displayMessage(getString(com.app.atoz.R.string.text_error_network))
                    else -> {
                        errorObj.customMessage
                            ?.let { displayMessage(it) }
                    }
                }
            }
        })
    }

    private fun showLoadingIndicator(isShow: Boolean) {
        mBinding.progressBar.visibility = if (isShow) View.VISIBLE else View.GONE
    }

    override fun handleListener() {

    }

    private fun showChangePriceDialog(categoryModel: CategoryModel) {
        val builder = AlertDialog.Builder(this@EditPersonalServiceActivity)
            .setTitle("Enter new price")

            .setCancelable(false)

        val view = layoutInflater.inflate(R.layout.item_change_price, null)
        builder.setView(view)

        val etPrice = view.findViewById(R.id.et_price) as EditText
        val saveButton = view.findViewById(R.id.btnSave) as Button
        val cancelButton = view.findViewById(R.id.btnCancel) as Button

        val dialog = builder.create()
        saveButton.setOnClickListener {
            when {
                etPrice.text.toString().isBlank() -> resToast(getString(com.app.atoz.R.string.text_error_enter_new_price))
                etPrice.text.trim().toString() == "0" -> resToast(getString(com.app.atoz.R.string.text_error_price_not_zero))
                else -> {
                    dialog.dismiss()
                    mViewModel.changePriceOfServiceItem(
                        categoryModel.categoryId.toString(), etPrice.text.toString(),
                        categoryModel.locationId, isInternetConnected, this, mDisposable
                    )
                }
            }
        }

        cancelButton.setOnClickListener {
            dialog.cancel()
        }

        dialog.show()
    }

    override fun displayMessage(message: String) {
        mBinding.root.snack(message)
    }

    override fun onClick(categoryModel: CategoryModel) {
        showChangePriceDialog(categoryModel)
    }

}
