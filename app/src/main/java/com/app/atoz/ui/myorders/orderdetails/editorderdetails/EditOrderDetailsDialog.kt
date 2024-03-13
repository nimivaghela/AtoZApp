package com.app.atoz.ui.myorders.orderdetails.editorderdetails

import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.app.atoz.AppApplication
import com.app.atoz.common.extentions.hideKeyboardFromDialog
import com.app.atoz.common.extentions.resToast
import com.app.atoz.common.helper.RxHelper
import com.app.atoz.databinding.DialogEditOrderDetailsBinding
import com.app.atoz.shareddata.base.BaseDialogFragment
import com.app.atoz.utils.Config
import timber.log.Timber
import javax.inject.Inject


class EditOrderDetailsDialog(
    private var mOrderId: String,
    private var mExistingOrderDetails: String
) : BaseDialogFragment() {

    override fun getResource(): Int = com.app.atoz.R.layout.dialog_edit_order_details

    companion object {
        const val EDIT_ORDER_DETAILS_DIALOG_TAG = "EditOrderDetailsDialog"
        fun newInstance(id: String, existingOrderDetails: String): EditOrderDetailsDialog {
            return EditOrderDetailsDialog(id, existingOrderDetails)
        }
    }

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private val mViewModel: EditOrderDetailsViewModel by lazy {
        ViewModelProviders.of(this, mViewModelFactory)[EditOrderDetailsViewModel::class.java]
    }
    private lateinit var mBinding: DialogEditOrderDetailsBinding
    private var mListener: OnEditOrderDetailsListener? = null

    override fun initViewModel() {
        (activity!!.application as AppApplication).mComponent.inject(this)
    }

    override fun postInit() {
        mBinding = getBinding()
        mViewModel.mOrderId = mOrderId
        initObserver()
        mBinding.etOrderDetails.setText(mExistingOrderDetails)
        mBinding.etOrderDetails.setSelection(mBinding.etOrderDetails.length())
    }

    fun setOnEditOrderDetailsListener(listener: OnEditOrderDetailsListener) {
        this.mListener = listener
    }

    private fun initObserver() {
        mViewModel.getEditOrderNote().observe(this, Observer { requestState ->
            showLoadingIndicator(requestState.progress)
            requestState.data?.let {
                Timber.d("Successfully done")
                mListener?.onSuccess(mBinding.etOrderDetails.text.toString())
                dialog?.dismiss()
            }
            requestState.error?.let { errorObj ->
                Timber.d("Successfully Error showing")
                when (errorObj.errorState) {
                    Config.NETWORK_ERROR ->
                        displayMessage(getString(com.app.atoz.R.string.text_error_network))
                    else ->
                        errorObj.customMessage
                            ?.let { displayMessage(it) }
                }
            }
        })
    }

    private fun showLoadingIndicator(isShow: Boolean) {
        mBinding.includeProgress.visibility = if (isShow) View.VISIBLE else View.GONE
    }

    override fun handleListener() {
        RxHelper.onClick(mBinding.btnSubmit, mDisposable) {
            dialog?.hideKeyboardFromDialog()
            if (mBinding.etOrderDetails.text.toString().isNotBlank()) {
                mBinding.tilOrderDetails.isErrorEnabled = true
                mViewModel.callEditOrderNote(
                    mBinding.etOrderDetails.text.toString(),
                    isInternetConnected,
                    this,
                    mDisposable
                )
            } else {
                mBinding.tilOrderDetails.error = getString(com.app.atoz.R.string.text_error_empty_note)
                mBinding.tilOrderDetails.isErrorEnabled = true
            }
        }

        RxHelper.onClick(mBinding.ibClose, mDisposable) {
            dialog?.hideKeyboardFromDialog()
            dialog?.dismiss()
        }
    }


    override fun displayMessage(message: String) {
        activity?.resToast(message)
    }


    interface OnEditOrderDetailsListener {
        fun onSuccess(orderDetails: String)
    }
}