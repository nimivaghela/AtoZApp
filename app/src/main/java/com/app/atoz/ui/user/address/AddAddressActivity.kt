package com.app.atoz.ui.user.address

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.app.atoz.AppApplication
import com.app.atoz.R
import com.app.atoz.common.extentions.snack
import com.app.atoz.common.helper.RxHelper
import com.app.atoz.databinding.ActivityAddAddressBinding
import com.app.atoz.db.database.AppDatabase
import com.app.atoz.models.CitiesItem
import com.app.atoz.models.UserHolder
import com.app.atoz.shareddata.base.BaseActivity
import com.app.atoz.ui.auth.signup.SignUpViewModel
import com.app.atoz.utils.Config
import com.app.atoz.utils.ValidationUtils
import com.app.rspicker.LocationModel
import com.app.rspicker.RSPlacePicker
import com.google.android.material.chip.Chip
import timber.log.Timber
import javax.inject.Inject


class AddAddressActivity : BaseActivity() {

    companion object {
        const val ADD_ADDRESS_REQUEST_CODE = 101
        const val ADDRESS_DATA = "ADDRESS_DATA"
        const val REQUEST_PLACE_PICKER = 102
        fun start(context: FragmentActivity) {
            context.startActivityForResult(Intent(context, AddAddressActivity::class.java), ADD_ADDRESS_REQUEST_CODE)
        }
    }

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var mSignUPViewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var mUserHolder: UserHolder

    private var mIsCitySelect: Boolean = false

    private val mViewModel: AddressViewModel by lazy {
        ViewModelProviders.of(this, mViewModelFactory)[AddressViewModel::class.java]
    }

    private val mSignUpViewModel: SignUpViewModel by lazy {
        ViewModelProviders
            .of(this, mSignUPViewModelFactory)[SignUpViewModel::class.java]
    }

    private lateinit var mBinding: ActivityAddAddressBinding

    override fun displayMessage(message: String) {
        mBinding.root.snack(message)
    }

    override fun getResource() = R.layout.activity_add_address

    override fun initView() {
        mBinding = getBinding()
        (application as AppApplication).mComponent.inject(this)
        setToolbar(mBinding.includeToolbar.toolbar, "Add Address", true)
        initObserver()
        loadCities()
    }

    override fun handleListener() {
        mBinding.acCityState.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val cityModel: CitiesItem = mBinding.acCityState.adapter.getItem(position) as CitiesItem
            mIsCitySelect = true
            mViewModel.getAddressRequestModel().cityStateID = cityModel.cityStateId!!
            mBinding.etAddress.requestFocus()
        }

        mBinding.acCityState.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                mBinding.acCityState.text = null
                mIsCitySelect = false
            }
        }

        RxHelper.onClick(mBinding.etLocationPiker, mDisposable) {
            val placePicker = RSPlacePicker()
                .setAndroidApiKey(getString(R.string.GooglePlaceApiKey))
                .build(this)
            startActivityForResult(placePicker, REQUEST_PLACE_PICKER)
        }

        RxHelper.onClick(mBinding.btnSave, mDisposable) {

            if (checkInput()) {
                mViewModel.getAddressRequestModel().userID = mUserHolder.userId

                mViewModel.getAddressRequestModel().address = mBinding.etAddress.text.toString().trim()

                val chip: Chip = findViewById(mBinding.cgAddressType.checkedChipId)
                mViewModel.getAddressRequestModel().addressType = chip.text.toString().toLowerCase()

                mViewModel.addAddress(isInternetConnected, this, mDisposable)
            }
        }
    }

    private fun loadCities() {
        try {
            val cityList = AppDatabase.getInstance(applicationContext).cityStateDao().getAllCities()
            if (cityList.isEmpty()) {
                mSignUpViewModel.provideCities(
                    isInternetConnected,
                    this,
                    mDisposable
                )
            } else {
                initCityAutoComplete(cityList)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            mSignUpViewModel.provideCities(
                isInternetConnected,
                this,
                mDisposable
            )
        }
    }

    private fun initObserver() {

        mViewModel.getAddAddressLiveData().observe(this, Observer { requestState ->

            requestState?.let { request ->
                showLoadingIndicator(request.progress)
                request.data?.let {
                    Timber.d("Successfully done")
                    it.message?.let { it1 -> displayMessage(it1) }
                    setResult(Activity.RESULT_OK, Intent().putExtra(ADDRESS_DATA, request.data.data))
                    finish()
                }
                request.error?.let { errorObj ->
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


        mSignUpViewModel.getCitiesLiveData().observe(this, Observer {
            it?.let { requestState ->
                showLoadingIndicator(requestState.progress)
                requestState.data?.let { response ->
                    response.data?.cities?.let { cityItems -> initCityAutoComplete(cityItems) }
                }
                requestState.error?.let { errorObj ->
                    Timber.d("Successfully Error showing")
                    when (errorObj.errorState) {
                        Config.NETWORK_ERROR ->
                            displayMessage(getString(R.string.text_error_network))
                        else -> {
                            errorObj.customMessage
                                ?.let { it1 -> displayMessage(it1) }
                        }
                    }
                }
            }
        })
    }

    private fun initCityAutoComplete(cityList: List<CitiesItem>) {
        mBinding.acCityState.setAdapter(
            ArrayAdapter<CitiesItem>(this, android.R.layout.simple_list_item_1, cityList)
        )
    }

    private fun checkInput(): Boolean {
        var isValid = true

        if (!ValidationUtils.isNotEmpty(mBinding.etLocationPiker.text.toString())) {
            mBinding.tilLocationPiker.isErrorEnabled = true
            mBinding.tilLocationPiker.error = getString(R.string.text_error_empty_location)
            isValid = false
        } else {
            mBinding.tilLocationPiker.isErrorEnabled = false
        }

        if (!ValidationUtils.isNotEmpty(mBinding.etAddress.text.toString())) {
            mBinding.tilAddress.isErrorEnabled = true
            mBinding.tilAddress.error = getString(R.string.text_error_empty_address)
            isValid = false
        } else {
            mBinding.tilAddress.isErrorEnabled = false
        }

        if (!mIsCitySelect) {
            mBinding.acCityState.error = getString(R.string.text_error_empty_city)
            isValid = false
        } else {
            mBinding.tilCityState.isErrorEnabled = false
        }

        if (mBinding.cgAddressType.checkedChipId == -1) {
            displayMessage(getString(R.string.text_error_empty_address_type))
            isValid = false
        }

        return isValid

    }

    private fun showLoadingIndicator(progress: Boolean) {
        mBinding.progressBar.visibility = if (progress) View.VISIBLE else View.GONE
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == REQUEST_PLACE_PICKER) && (resultCode == Activity.RESULT_OK)) {
            val location: LocationModel? = data?.let { RSPlacePicker.getLocation(it) }
            mBinding.etLocationPiker.setText(location?.address)

            mViewModel.getAddressRequestModel().location = location?.address
            mViewModel.getAddressRequestModel().longitude = location?.longitude?.toString()
            mViewModel.getAddressRequestModel().latitude = location?.latitude?.toString()
        }
    }
}