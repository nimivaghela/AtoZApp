package com.app.atoz.ui.user.address

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import com.app.atoz.AppApplication
import com.app.atoz.R
import com.app.atoz.common.extentions.snack
import com.app.atoz.common.helper.RxHelper
import com.app.atoz.databinding.ActivityAddressPikerBinding
import com.app.atoz.models.address.AddressListItem
import com.app.atoz.shareddata.base.BaseActivity
import com.app.atoz.ui.user.address.AddAddressActivity.Companion.ADDRESS_DATA
import com.app.atoz.ui.user.address.AddAddressActivity.Companion.ADD_ADDRESS_REQUEST_CODE
import com.app.atoz.ui.user.home.ItemClickListener
import com.app.atoz.utils.Config
import timber.log.Timber
import javax.inject.Inject


class AddressPikerActivity : BaseActivity() {

    companion object {
        const val KEY_ADDRESS_PICKER = 100
        const val KEY_IS_FROM_SETTING = "IsFromSetting"
        fun start(context: Activity) {
            context.startActivityForResult(Intent(context, AddressPikerActivity::class.java), KEY_ADDRESS_PICKER)
        }

        fun start(context: Context, isFromSetting: Boolean) {
            context.startActivity(
                Intent(context, AddressPikerActivity::class.java)
                    .putExtra(KEY_IS_FROM_SETTING, isFromSetting)
            )
        }
    }

    override fun getResource() = R.layout.activity_address_piker

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var mViewModel: AddressViewModel

    private lateinit var mBinding: ActivityAddressPikerBinding
    override fun initView() {
        mBinding = getBinding()

        (application as AppApplication).mComponent.inject(this)

        setToolbar(mBinding.includeToolbar.toolbar, "Manage addresses", true)


        mViewModel = ViewModelProviders
            .of(this, viewModelFactory)[AddressViewModel::class.java]


        mBinding.rcvAddress.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

    }


    override fun handleListener() {

        initObserver()

        mBinding.rcvAddress.adapter = AddressPikerAdapter(object : ItemClickListener {
            override fun onItemClick(view: View, position: Int, data: Any?) {

                if (view.id == R.id.tv_delete) {

                    val addressListItem: AddressListItem = data as AddressListItem

                    mViewModel.deleteAddress(
                        isInternetConnected,
                        this@AddressPikerActivity,
                        mDisposable,
                        addressListItem
                    )

                } else {
                    if (!intent.hasExtra(KEY_IS_FROM_SETTING)) {
                        setResult(Activity.RESULT_OK, Intent().putExtra(ADDRESS_DATA, data as AddressListItem))
                        finish()
                    }
                }
            }
        })

        mViewModel.provideAddresses(isInternetConnected, this, mDisposable)

        RxHelper.onClick(mBinding.fabAddAddress, mDisposable, onRxClick = {
            AddAddressActivity.start(this)
        })

    }

    private fun initObserver() {

        mViewModel.getAddressLiveData().observe(this, Observer { requestState ->


            requestState?.let { response ->
                showLoadingIndicator(response.progress)
                response.data?.let {
                    Timber.d("Successfully done")
                    val addressPikerAdapter: AddressPikerAdapter = mBinding.rcvAddress.adapter as AddressPikerAdapter

                    addressPikerAdapter.setAddressList(it.data!!.addressList as MutableList<AddressListItem?>)
                    addressPikerAdapter.notifyDataSetChanged()


                }
                response.error?.let { errorObj ->
                    Timber.d("Successfully Error showing")
                    when (errorObj.errorState) {
                        Config.NETWORK_ERROR ->
                            displayMessage(getString(R.string.text_error_network))
                        Config.CUSTOM_ERROR ->
                            errorObj.customMessage
                                ?.let { displayMessage(it) }!!
                        else -> {
                        }
                    }
                }
            }
        })

        mViewModel.getDeleteAddressLiveData().observe(this, Observer { requestState ->


            requestState?.let { response ->
                showLoadingIndicator(response.progress)
                response.data?.let {

                    val addressPikerAdapter: AddressPikerAdapter = mBinding.rcvAddress.adapter as AddressPikerAdapter

                    it.data?.let { it1 -> addressPikerAdapter.removeData(it1) }
                    it.message?.let { it1 -> displayMessage(it1) }

                }
                response.error?.let { errorObj ->
                    Timber.d("Successfully Error showing")
                    when (errorObj.errorState) {
                        Config.NETWORK_ERROR ->
                            displayMessage(getString(R.string.text_error_network))
                        Config.CUSTOM_ERROR ->
                            errorObj.customMessage
                                ?.let { displayMessage(it) }!!
                        else -> {
                        }
                    }
                }
            }
        })
    }

    private fun showLoadingIndicator(progress: Boolean) {
        mBinding.progressBar.visibility = if (progress) View.VISIBLE else View.GONE
    }


    override fun displayMessage(message: String) {
        mBinding.root.snack(message)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == ADD_ADDRESS_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            val addressPikerAdapter: AddressPikerAdapter = mBinding.rcvAddress.adapter as AddressPikerAdapter
            if (data != null) addressPikerAdapter.addData(data.getParcelableExtra(ADDRESS_DATA)!!)

        }
    }

}
