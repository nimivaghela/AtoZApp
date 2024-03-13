package com.app.atoz.ui.editprofile

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.app.atoz.AppApplication
import com.app.atoz.R
import com.app.atoz.common.extentions.hideKeyboard
import com.app.atoz.common.extentions.loadImage
import com.app.atoz.common.extentions.resToast
import com.app.atoz.common.helper.ImagePickerHelper
import com.app.atoz.common.helper.RxHelper
import com.app.atoz.databinding.ActivityUserEditProfileBinding
import com.app.atoz.db.database.AppDatabase
import com.app.atoz.models.CitiesItem
import com.app.atoz.models.UserHolder
import com.app.atoz.shareddata.base.BaseActivity
import com.app.atoz.utils.Config
import com.app.atoz.utils.ValidationUtils
import com.app.rspicker.LocationModel
import com.app.rspicker.RSPlacePicker
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject


class UserEditProfileActivity : BaseActivity(), ImagePickerHelper.ImagePickerListener {
    companion object {
        const val KEY_EDIT_PROFILE = 3000
        const val REQUEST_PLACE_PICKER = 102
        const val KEY_CALL_GET_PROFILE = "KeyCallGetProfile"
        fun start(context: Activity, needToCallGetProfile: Boolean = false) {
            context.startActivityForResult(
                Intent(context, UserEditProfileActivity::class.java)
                    .putExtra(KEY_CALL_GET_PROFILE, needToCallGetProfile), KEY_EDIT_PROFILE
            )
        }
    }

    override fun getResource(): Int = com.app.atoz.R.layout.activity_user_edit_profile

    private lateinit var mImagePickerHelpers: ImagePickerHelper
    private lateinit var mBinding: ActivityUserEditProfileBinding
    private var isImageUploadCallForCertificate = false
    private val mViewModel: UserEditProfileViewModel by lazy {
        ViewModelProviders
            .of(this, viewModelFactory)[UserEditProfileViewModel::class.java]
    }
    private var mIsCitySelect = false
    private lateinit var mCityStateID: String
    @Inject
    lateinit var mUserHolder: UserHolder
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun initView() {
        (application as AppApplication).mComponent.inject(this)
        mBinding = getBinding()

        setToolbar(mBinding.SignUpToolbar.toolbar, getString(R.string.edit_profile), true)

        mImagePickerHelpers = ImagePickerHelper(this@UserEditProfileActivity, this)
        initObserver()
        loadCities()

        mBinding.acCityState.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val cityModel: CitiesItem = mBinding.acCityState.adapter.getItem(position) as CitiesItem
            mIsCitySelect = true
            mCityStateID = cityModel.cityStateId!!
            mBinding.etAddress.requestFocus()
        }
        mBinding.acCityState.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                mBinding.acCityState.text = null
                mIsCitySelect = false
            }
        }
        bindViewData()
        if (intent.getBooleanExtra(KEY_CALL_GET_PROFILE, false)) {
            mViewModel.getUserProfile(isInternetConnected, this, mDisposable)
        }
    }

    private fun bindViewData() {
        mUserHolder.apply {
            mBinding.ivUploadPic.loadImage(imageUrl, null, null, R.drawable.bg_circle_grey_camera, true)
            mBinding.etFirstName.setText(firstName)
            mBinding.etLastName.setText(lastName)
            mBinding.etEmail.setText(email)
            mBinding.etPhoneNumber.setText(phone)
            mBinding.etAddress.setText(address)
            val address = getUserAddressFromProfile()
            address?.let {
                mBinding.etLocationPiker.setText(it.location)
                if (it.latitude != null && it.longitude != null) {
                    try {
                        mViewModel.latLng = LatLng(it.latitude.toDouble(), it.longitude.toDouble())
                    } catch (nfe: NumberFormatException) {
                        nfe.printStackTrace()
                    }
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun handleListener() {
        RxHelper.onClick(mBinding.rlUploadPic, mDisposable) {
            isImageUploadCallForCertificate = false
            mImagePickerHelpers.mIsCropInCircleShape = true
            mImagePickerHelpers.selectOptionToLoadImage()
        }

        RxHelper.onClick(mBinding.etLocationPiker, mDisposable) {
            val placePicker = RSPlacePicker()
                .setAndroidApiKey(getString(R.string.GooglePlaceApiKey))
                .build(this)
            startActivityForResult(placePicker, REQUEST_PLACE_PICKER)
        }

        mBinding.etAddress.setOnTouchListener { v, event ->
            if (v.id == com.app.atoz.R.id.etAddress) {
                v.parent.requestDisallowInterceptTouchEvent(true)
                when (event.action and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_UP -> v.parent.requestDisallowInterceptTouchEvent(false)
                }
            }
            false
        }

        RxHelper.onClick(mBinding.btnSave, mDisposable) {
            hideKeyboard()
            if (checkValidation()) {
                mViewModel.generateRequestModel(
                    mBinding.etFirstName.text.toString(),
                    mBinding.etLastName.text.toString(),
                    mBinding.etAddress.text.toString(),
                    mViewModel.mProfilePicFile,
                    mViewModel.latLng?.latitude.toString(),
                    mViewModel.latLng?.longitude.toString(),
                    mBinding.etLocationPiker.text.toString().trim(),
                    mCityStateID
                )
                mViewModel.editProfile(isInternetConnected, this, mDisposable)
            }
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        mImagePickerHelpers.onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mImagePickerHelpers.onActivityResult(requestCode, resultCode, data)

        if ((requestCode == REQUEST_PLACE_PICKER) && (resultCode == Activity.RESULT_OK)) {
            val location: LocationModel? = data?.let { RSPlacePicker.getLocation(it) }
            location?.apply {
                mBinding.etLocationPiker.setText(address)
                mViewModel.latLng = LatLng(latitude, longitude)
            }
        }
    }

    override fun onImageLoad() {
        loadImage()
    }

    override fun showLoadingIndicator(isShow: Boolean) {
        mBinding.progressBar.visibility = if (isShow) View.VISIBLE else View.GONE
    }

    override fun displayMessage(message: String) {
        Snackbar.make(mBinding.rlRootView, message, Snackbar.LENGTH_LONG).show()
    }

    override fun getDisposable(): CompositeDisposable = mDisposable

    private fun loadImage() {

        mViewModel.mProfilePicFile = mImagePickerHelpers.mFile
        mViewModel.mProfilePicFileUri = mImagePickerHelpers.mFileUri

        Glide.with(this)
            .load(mViewModel.mProfilePicFileUri)
            .apply(RequestOptions.circleCropTransform())
            .placeholder(com.app.atoz.R.drawable.bg_circle_grey_camera)
            .into(mBinding.ivUploadPic)

        mBinding.ivUploadPicPlus.visibility = View.GONE
    }

    private fun loadCities() {
        try {
            val cityList = AppDatabase.getInstance(applicationContext).cityStateDao().getAllCities()
            if (cityList.isEmpty()) {
                mViewModel.provideCities(
                    isInternetConnected,
                    this,
                    mDisposable
                )
            } else {
                initCityAutoComplete(cityList)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            mViewModel.provideCities(
                isInternetConnected,
                this,
                mDisposable
            )
        }
    }

    private fun initObserver() {
        mViewModel.getCitiesLiveData().observe(this, Observer {
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

        mViewModel.viewProfileRequest().observe(this, Observer { requestState ->
            showLoadingIndicator(requestState.progress)
            requestState.data?.let {
                Timber.d("Successfully done")
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

        mViewModel.viewEditProfileRequest().observe(this, Observer { requestState ->
            showLoadingIndicator(requestState.progress)
            requestState.data?.let {
                Timber.d("Successfully done")
                resToast(R.string.text_profile_success)
                setResult(Activity.RESULT_OK)
                finish()
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

    private fun initCityAutoComplete(cityList: List<CitiesItem>) {
        cityList.let {
            mBinding.acCityState.setAdapter(
                ArrayAdapter<CitiesItem>(this, android.R.layout.simple_list_item_1, it)
            )

            val userAddress = mUserHolder.getUserAddressFromProfile()
            userAddress?.let { address ->
                val cityStateId = "${address.cityId}-${address.stateId}"
                loop@ for ((_, value) in cityList.withIndex()) {
                    if (value.cityStateId == cityStateId) {
                        mIsCitySelect = true
                        mCityStateID = value.cityStateId
                        mBinding.acCityState.setText(value.cityState)
                        break@loop
                    }
                }
            }
        }
    }

    private fun checkValidation(): Boolean {
        var isValid = true
        if (!ValidationUtils.isNotEmpty(mBinding.etFirstName.text.toString())) {
            mBinding.tilFirstName.isErrorEnabled = true
            mBinding.tilFirstName.error = getString(R.string.text_error_empty_first_name)
            isValid = false
        } else {
            mBinding.tilFirstName.isErrorEnabled = false
        }

        if (!ValidationUtils.isNotEmpty(mBinding.etLastName.text.toString())) {
            mBinding.tilLastName.isErrorEnabled = true
            mBinding.tilLastName.error = getString(R.string.text_error_empty_last_name)
            isValid = false
        } else {
            mBinding.tilLastName.isErrorEnabled = false
        }

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

        return isValid
    }
}