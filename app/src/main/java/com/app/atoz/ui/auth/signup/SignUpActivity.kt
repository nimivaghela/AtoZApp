package com.app.atoz.ui.auth.signup

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.method.LinkMovementMethod
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.app.atoz.AppApplication
import com.app.atoz.R
import com.app.atoz.common.extentions.*
import com.app.atoz.common.helper.FacebookManager
import com.app.atoz.common.helper.GooglePlusManager
import com.app.atoz.common.helper.ImagePickerHelper
import com.app.atoz.common.helper.RxHelper
import com.app.atoz.common.listeners.SocailCallBackListener
import com.app.atoz.databinding.ActivitySignupBinding
import com.app.atoz.db.database.AppDatabase
import com.app.atoz.models.CitiesItem
import com.app.atoz.models.SocialLogin
import com.app.atoz.models.UserHolder
import com.app.atoz.shareddata.base.BaseActivity
import com.app.atoz.ui.auth.signin.SignInViewModel
import com.app.atoz.ui.auth.verification.VerificationActivity
import com.app.atoz.ui.main.MainActivity
import com.app.atoz.ui.policy.PrivacyPolicyActivity
import com.app.atoz.ui.provider.selectservice.SelectServiceActivity
import com.app.atoz.ui.selectaccount.SelectAccountActivity
import com.app.atoz.utils.Config
import com.app.atoz.utils.ValidationUtils
import com.app.rspicker.LocationModel
import com.app.rspicker.RSPlacePicker
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import java.io.File
import javax.inject.Inject


class SignUpActivity : BaseActivity(), ImagePickerHelper.ImagePickerListener,
    SocailCallBackListener {

    companion object {
        private const val KEY_USER_TYPE = "UserType"
        const val EXTRA_SOCIAL_USER = "EXTRA_SOCIAL_USER"
        const val TYPE_USER = "User"
        const val TYPE_PROVIDER = "Provider"
        const val REQUEST_PLACE_PICKER = 102
        fun start(context: Context, userType: String) {
            context.startActivity(
                Intent(context, SignUpActivity::class.java)
                    .putExtra(KEY_USER_TYPE, userType)
            )
        }

        fun start(context: Context, userType: String, soicalUserString: String) {
            context.startActivity(
                Intent(context, SignUpActivity::class.java)
                    .putExtra(KEY_USER_TYPE, userType)
                    .putExtra(SelectAccountActivity.EXTRA_SOCIAL_USER, soicalUserString)
            )
        }

    }

    override fun getResource(): Int = R.layout.activity_signup

    private lateinit var mImagePickerHelpers: ImagePickerHelper
    private lateinit var mBinding: ActivitySignupBinding
    private var isProvider: Boolean = false
    private var isImageUploadCallForCertificate = false
    private val mViewModel: SignUpViewModel by lazy {
        ViewModelProviders
            .of(this, viewModelFactory)[SignUpViewModel::class.java]
    }


    private val mSignInViewModel: SignInViewModel by lazy {
        ViewModelProviders
            .of(this, viewModelFactory)[SignInViewModel::class.java]
    }

    @Inject
    lateinit var userHolder: UserHolder


    private var socialLogin: SocialLogin? = null

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private var mIsCitySelect = false
    private lateinit var mCityStateID: String

    override fun initView() {
        (application as AppApplication).mComponent.inject(this)
        lifecycle.addObserver(FacebookManager)

        mBinding = getBinding()

        setToolbar(mBinding.SignUpToolbar.toolbar, "", true, R.color.white)
        mBinding.tvTermsCondition.text =
            getString(R.string.text_terms_condition)
                .highlightText(38, 58, ContextCompat.getColor(this, R.color.colorPrimary))
                .highlightText(63, 77, ContextCompat.getColor(this, R.color.colorPrimary))
                .changeTextStyle(38, 58, ResourcesCompat.getFont(this, R.font.proximanova_bold)!!)
                .changeTextStyle(63, 77, ResourcesCompat.getFont(this, R.font.proximanova_bold)!!)
                .clickEvent(38, 58, false) {
                    PrivacyPolicyActivity.start(this@SignUpActivity, false)
                }
                .clickEvent(63, 77, false) {
                    PrivacyPolicyActivity.start(this@SignUpActivity, true)
                }
        mBinding.tvTermsCondition.movementMethod = LinkMovementMethod.getInstance()

        isProvider = intent.getStringExtra(KEY_USER_TYPE) == TYPE_PROVIDER

        if (intent.hasExtra(EXTRA_SOCIAL_USER)) {
            socialLogin =
                Gson().fromJson(intent.getStringExtra(EXTRA_SOCIAL_USER), SocialLogin::class.java)
            fillSocialData()
        }

        mBinding.tvSignUpTitle.text = String.format(
            getString(R.string.text_sign_up_as),
            getString(if (isProvider) R.string.text_provider else R.string.text_user)
        )
        mBinding.llUploadCertificateBg.visibility = if (isProvider) View.VISIBLE else View.GONE

        mBinding.btnSignUp.text =
            getString(if (isProvider) R.string.text_next else R.string.text_sign_up)

        mImagePickerHelpers = ImagePickerHelper(this@SignUpActivity, this)

        initSignUpObserver()
        loadCities()
        mBinding.acCityState.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val cityModel: CitiesItem =
                    mBinding.acCityState.adapter.getItem(position) as CitiesItem
                mIsCitySelect = true
                mCityStateID = cityModel.cityStateId!!
                mBinding.etAddress.requestFocus()
                this.hideKeyboard()
            }
        mBinding.acCityState.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                mBinding.acCityState.text = null
                mIsCitySelect = false
            }
        }

        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    mViewModel.mDeviceToken = task.result?.token
                }
                // Get new Instance ID token
            }
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

    private fun initSignUpObserver() {
        mViewModel.getSignUpRequest().observe(this@SignUpActivity, Observer {
            it?.let { requestState ->
                showLoadingIndicator(requestState.progress)
                requestState.data?.let {
                    Timber.d("Successfully done")
                    VerificationActivity.start(this@SignUpActivity)
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
                }
            }
        })

        mViewModel.getPhoneEmailExistRequest().observe(this@SignUpActivity, Observer {
            it?.let { requestState ->
                showLoadingIndicator(requestState.progress)
                requestState.data?.let { phoneExistData ->

                    when {
                        phoneExistData.data!!.doesEmailExist -> displayMessage(getString(R.string.text_error_email_already_exist))
                        phoneExistData.data!!.doesPhoneExist -> displayMessage(getString(R.string.text_error_phone_already_exist))
                        else -> mViewModel.mSignUpRequestModel?.let { model ->
                            SelectServiceActivity.start(this, model)
                        } ?: let {
                            Timber.d("Successfully Error model is empty")
                            displayMessage(getString(R.string.text_error_internal_server))
                        }
                    }

                }
                requestState.error?.let { errorObj ->
                    Timber.d("Successfully Error showing")
                    when (errorObj.errorState) {
                        Config.NETWORK_ERROR ->
                            displayMessage(getString(R.string.text_error_network))
                        else ->
                            errorObj.customMessage
                                ?.let { message -> displayMessage(message) }
                    }
                }
            }
        })

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

        mSignInViewModel.getSocialIdExist().observe(this@SignUpActivity, Observer
        { it ->
            it?.let { requestState ->
                showLoadingIndicator(requestState.progress)
                requestState.data?.let {
                    Timber.d("Successfully done")



                    mSignInViewModel.isSocialIdExist = it.data?.isExist!!
                    if (mSignInViewModel.isSocialIdExist) {
                        callSignInSocialApi()

                    } else {

                        socialLogin = mSignInViewModel.socialLoginModel
                        fillSocialData()

                    }

                }
                requestState.error?.let { errorObj ->
                    Timber.d("Successfully Error showing")
                    when (errorObj.errorState) {
                        Config.NETWORK_ERROR ->
                            displayMessage(getString(R.string.text_error_network))
                        Config.CUSTOM_ERROR ->
                            errorObj.customMessage
                                ?.let { displayMessage(it) }
                        else -> {
                        }
                    }
                }
            }
        })

        mSignInViewModel.getSignInRequest().observe(this@SignUpActivity, Observer { it ->
            it?.let { requestState ->
                showLoadingIndicator(requestState.progress)
                requestState.data?.let {
                    Timber.d("Successfully done")
                    if (userHolder.isVerified) {
                        if (userHolder.isUserAsProvider && it.data != null && it.data!!.status == "pending") {
                            displayMessage(getString(R.string.text_error_provider_sign_up))
                        } else {
                            MainActivity.start(this@SignUpActivity)
                            finish()
                        }
                    } else {
                        VerificationActivity.start(this@SignUpActivity)
                        finish()
                    }

                }
                requestState.error?.let { errorObj ->
                    Timber.d("Successfully Error showing")
                    when (errorObj.errorState) {
                        Config.NETWORK_ERROR ->
                            displayMessage(getString(R.string.text_error_network))
                        Config.CUSTOM_ERROR ->
                            errorObj.customMessage
                                ?.let { displayMessage(it) }
                        else -> {
                        }
                    }
                }
            }
        })
    }

    private fun fillSocialData() {
        socialLogin?.run {
            mBinding.etFirstName.setText(firstName)
            mBinding.etLastName.setText(lastName)
            mBinding.etEmail.setText(email)
            downloadProfileUrlAndSaveOnLocalStorage(url)
        }
    }

    private fun downloadProfileUrlAndSaveOnLocalStorage(url: String) {

        Glide.with(this)
            .asFile().load(url)
            .listener(object : RequestListener<File> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<File>?,
                    isFirstResource: Boolean
                ): Boolean {
                    mViewModel.mProfilePicFile = null
                    return false
                }

                override fun onResourceReady(
                    resource: File?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<File>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {

                    resource.let {
                        mViewModel.mProfilePicFile = resource
                        runOnUiThread {
                            Glide.with(this@SignUpActivity)
                                .load(Uri.fromFile(mViewModel.mProfilePicFile))
                                .apply(RequestOptions.circleCropTransform())
                                .placeholder(R.drawable.bg_circle_grey_camera)
                                .into(mBinding.ivUploadPic)
                        }
                    }
                    return true
                }

            }).submit()

        /*Glide.with(this)
            .asFile().load(url)
            .listener(object : RequestListener<File> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<File>?,
                    isFirstResource: Boolean
                ): Boolean {
                  //To change body of created functions use File | Settings | File Templates.
                }

                override fun onResourceReady(
                    resource: File?,
                    model: Any?,
                    target: Target<File>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                   //To change body of created functions use File | Settings | File Templates.

                }


            }
            ).submit()*/
    }

    private fun initCityAutoComplete(cityList: List<CitiesItem>) {
        mBinding.acCityState.setAdapter(
            ArrayAdapter<CitiesItem>(this, android.R.layout.simple_list_item_1, cityList)
        )
    }

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    override fun handleListener() {

        RxHelper.onClick(mBinding.rlUploadPic, mDisposable) {
            isImageUploadCallForCertificate = false
            mImagePickerHelpers.mIsCropInCircleShape = true
            mImagePickerHelpers.selectOptionToLoadImage()
        }

        RxHelper.onClick(mBinding.ivUploadCertificate, mDisposable) {
            isImageUploadCallForCertificate = true
            mImagePickerHelpers.mIsCropInCircleShape = false
            mImagePickerHelpers.selectOptionToLoadImage()
        }

        RxHelper.onClick(mBinding.btnSignUp, mDisposable) {
            Timber.d("OnClick of btnSignUp")

            if (checkValidation()) {
                var socialId = ""
                var socialType = ""
                val phoneNumber = "+91" + mBinding.etPhoneNumber.text.toString()
                socialLogin?.let {
                    socialId = it.socialId
                    socialType = it.socialType
                }
                mViewModel.generateRequestModel(
                    mBinding.etFirstName.text.toString(),
                    mBinding.etLastName.text.toString(),
                    mBinding.etEmail.text.toString(),
                    phoneNumber,
                    mBinding.etPassword.text.toString(),
                    mBinding.etAddress.text.toString(),
                    mViewModel.mProfilePicFile,
                    mViewModel.latLng?.latitude.toString(),
                    mViewModel.latLng?.longitude.toString(),
                    mViewModel.mCertificateFile,
                    socialId,
                    socialType,
                    mBinding.etLocationPiker.text.toString().trim(),
                    mCityStateID
                )
                if (isProvider) {
                    mViewModel.doesPhoneEmailExist(
                        isInternetConnected,
                        this,
                        mDisposable,
                        mBinding.etEmail.text.toString(),
                        mBinding.etPhoneNumber.text.toString()
                    )
                } else {
                    mViewModel.doSignUpForUser(
                        isInternetConnected,
                        this,
                        mDisposable, mBinding.etLocationPiker.text.toString().trim(), mCityStateID,
                        socialId,
                        socialType
                    )
                }
            }
        }

        RxHelper.onClick(mBinding.etLocationPiker, mDisposable) {
            val placePicker = RSPlacePicker()
                .setAndroidApiKey(getString(R.string.GooglePlaceApiKey))
                .build(this)
            startActivityForResult(placePicker, REQUEST_PLACE_PICKER)
        }

        mBinding.etAddress.setOnTouchListener { v, event ->
            if (v.id == R.id.etAddress && mBinding.etAddress.lineCount > 5) {
                v.parent.requestDisallowInterceptTouchEvent(true)
                when (event.action and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_UP -> v.parent.requestDisallowInterceptTouchEvent(false)
                }
            }
            false
        }

        RxHelper.onClick(mBinding.btnFacebook, mDisposable)
        {
            if (isInternetConnected) {
                FacebookManager.mListener = this
                FacebookManager.doLogin(this)
            } else {
                displayMessage(getString(R.string.text_error_network))
            }

        }

        RxHelper.onClick(mBinding.btnGooglePlus, mDisposable)
        {
            if (isInternetConnected) {
                GooglePlusManager.mListener = this
                GooglePlusManager.doLogin(this)
            } else {
                displayMessage(getString(R.string.text_error_network))
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

        if (!ValidationUtils.isNotEmpty(mBinding.etEmail.text.toString())) {
            mBinding.tilEmail.isErrorEnabled = true
            mBinding.tilEmail.error = getString(R.string.text_error_empty_email)
            isValid = false
        } else if (!ValidationUtils.doesEmailValid(mBinding.etEmail.text.toString())) {
            mBinding.tilEmail.isErrorEnabled = true
            mBinding.tilEmail.error = getString(R.string.text_error_invalid_email)
            isValid = false
        } else {
            mBinding.tilEmail.isErrorEnabled = false
        }

        if (!ValidationUtils.isNotEmpty(mBinding.etPhoneNumber.text.toString())) {
            mBinding.tilPhoneNumber.isErrorEnabled = true
            mBinding.tilPhoneNumber.error = getString(R.string.text_error_empty_phone_number)
            isValid = false
        } else if (!ValidationUtils.doesMobileNumberValid(mBinding.etPhoneNumber.text.toString())) {
            mBinding.tilPhoneNumber.isErrorEnabled = true
            mBinding.tilPhoneNumber.error = getString(R.string.text_error_invalid_phone_number)
            isValid = false
        } else {
            mBinding.tilPhoneNumber.isErrorEnabled = false
        }

        if (!ValidationUtils.isNotEmpty(mBinding.etPassword.text.toString())) {
            mBinding.tilPassword.isErrorEnabled = true
            mBinding.tilPassword.error = getString(R.string.text_error_empty_password)
            isValid = false
        } else if (!ValidationUtils.doesPasswordLengthValid(mBinding.etPassword.text.toString())) {
            mBinding.tilPassword.isErrorEnabled = true
            mBinding.tilPassword.error = getString(R.string.text_error_invalid_password)
            isValid = false
        } else {
            mBinding.tilPassword.isErrorEnabled = false
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

        if (isProvider && mViewModel.mCertificateFile == null) {
            mBinding.tvCertificateError.visibility = View.VISIBLE
            isValid = false
        } else {
            mBinding.tvCertificateError.visibility = View.INVISIBLE
        }

        return isValid
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
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
        mBinding.rlRootView.snack(message)
    }

    override fun getDisposable(): CompositeDisposable = mDisposable

    private fun loadImage() {
        if (isImageUploadCallForCertificate) {
            mViewModel.mCertificateFile = mImagePickerHelpers.mFile
            mViewModel.mCertificateFileUri = mImagePickerHelpers.mFileUri

            Glide.with(this)
                .load(mImagePickerHelpers.mFileUri)
                .placeholder(R.drawable.bg_rect_certificate)
                .into(mBinding.ivUploadCertificate)

        } else {
            mViewModel.mProfilePicFile = mImagePickerHelpers.mFile
            mViewModel.mProfilePicFileUri = mImagePickerHelpers.mFileUri

            Glide.with(this)
                .load(mViewModel.mProfilePicFileUri)
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.bg_circle_grey_camera)
                .into(mBinding.ivUploadPic)

            mBinding.ivUploadPicPlus.visibility = View.GONE
        }
    }

    override fun onSocialSuccess(socialType: Int, statusCode: Int, data: Any) {
        when (socialType) {
            FacebookManager.SOCIAL_FACEBOOK -> {
                mSignInViewModel.socialLoginModel = data as SocialLogin
                mSignInViewModel.socialType = FacebookManager.SOCIAL_FACEBOOK
            }

            GooglePlusManager.SOCIAL_GOOGLE_PLUS -> {
                mSignInViewModel.socialLoginModel = data as SocialLogin
                mSignInViewModel.socialType = GooglePlusManager.SOCIAL_GOOGLE_PLUS
                GooglePlusManager.doLogout()

            }
        }

        mViewModel.mDeviceToken?.let {
            mSignInViewModel.socialLoginModel?.deviceToken = it
        }

        doCheckingSocialIdExist()
    }

    private fun doCheckingSocialIdExist() {
        mSignInViewModel.doCheckingSocialIdExist(isInternetConnected, this, mDisposable)
    }

    override fun onSocialCancel(socialType: Int, statusCode: Int, msg: String) {
        Timber.d("cancel social Login $msg")
    }

    override fun onSocialFailed(socialType: Int, statusCode: Int, errorMsg: String) {
        Timber.d("Failed Login $errorMsg")
        displayMessage(errorMsg)
    }

    override fun signout(success: Boolean) {

    }

    private fun callSignInSocialApi() {
        mSignInViewModel.doSocialLogin(
            isInternetConnected,
            this,
            mDisposable,
            mSignInViewModel.socialLoginModel!!
        )
    }
}