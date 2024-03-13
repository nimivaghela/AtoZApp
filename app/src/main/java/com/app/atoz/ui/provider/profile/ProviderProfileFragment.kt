package com.app.atoz.ui.provider.profile


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.app.atoz.AppApplication
import com.app.atoz.R
import com.app.atoz.common.extentions.loadImage
import com.app.atoz.common.extentions.snack
import com.app.atoz.common.helper.RxHelper
import com.app.atoz.databinding.FragmentProviderProfileBinding
import com.app.atoz.models.AtoZResponseModel
import com.app.atoz.models.ProfileResponseModel
import com.app.atoz.models.UserHolder
import com.app.atoz.shareddata.base.BaseFragment
import com.app.atoz.ui.editprofile.UserEditProfileActivity
import com.app.atoz.ui.editprofile.UserEditProfileActivity.Companion.KEY_EDIT_PROFILE
import com.app.atoz.ui.provider.existservice.ExistServiceActivity
import com.app.atoz.ui.provider.existservice.ExistServiceActivity.Companion.KEY_EXIST_SERVICE
import com.app.atoz.ui.provider.existservice.editservice.EditPersonalServiceActivity
import com.app.atoz.utils.Config
import timber.log.Timber
import javax.inject.Inject

class ProviderProfileFragment : BaseFragment() {
    override fun getInflateResource() = R.layout.fragment_provider_profile

    companion object {
        fun newInstance(): ProviderProfileFragment {
            return ProviderProfileFragment()
        }
    }

    private lateinit var mBinding: FragmentProviderProfileBinding
    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var mUserHolder: UserHolder

    private val mViewModel: ProviderProfileViewModel by lazy {
        ViewModelProviders.of(this, mViewModelFactory)[ProviderProfileViewModel::class.java]
    }

    override fun initView() {
        (activity!!.application as AppApplication).mComponent.inject(this)
        mBinding = getBinding()
        initObserver()
    }

    override fun postInit() {
        bindViewData("", "0.0")
        callViewProfile()
    }

    private fun callViewProfile() {
        mViewModel.getProviderProfile(isInternetConnected, this, mDisposable)
    }

    override fun handleListener() {
        RxHelper.onClick(mBinding.icEditProfile, mDisposable) {
            activity?.let {
                UserEditProfileActivity.start(it)
            }
        }

        RxHelper.onClick(mBinding.tvEdit, mDisposable) {
            activity?.let {
                ExistServiceActivity.start(it)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            KEY_EDIT_PROFILE, KEY_EXIST_SERVICE -> {
                if (resultCode == Activity.RESULT_OK) {
                    activity?.let {
                        callViewProfile()
                    }
                }
            }
        }
    }

    override fun displayMessage(message: String) {
        mBinding.llRootView.snack(message)
    }

    private fun initObserver() {
        mViewModel.viewProfileRequest().observe(this, Observer { requestState ->
            showLoadingIndicator(requestState.progress)
            requestState.data?.let {
                Timber.d("Successfully done")
                val cityName = it.data?.providerCityName ?: ""
                val rating = it.data?.providerRating ?: "0.0"
                bindViewData(cityName, rating)
                bindServiceView(it)
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

    private fun bindServiceView(response: AtoZResponseModel<ProfileResponseModel>) {
        response.data?.let { data ->
            data.providerCategoryList?.let {
                mBinding.rvPersonalService.adapter =
                    ProviderExistCategoryAdapter(it,
                        object : ProviderExistServiceAdapter.OnProviderExistServiceListener {
                            override fun onClick(serviceId: String) {
                                activity?.let { activity ->
                                    EditPersonalServiceActivity.start(activity, serviceId)
                                }
                            }
                        })
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun bindViewData(cityName: String, rating: String) {
        mBinding.ivProfileImage
            .loadImage(mUserHolder.imageUrl, null, null, R.drawable.bg_circle_grey_camera, true)
        mBinding.ivProfileImageBackground.loadImage(
            mUserHolder.imageUrl, null, null,
            R.drawable.bg_circle_grey_camera, false
        )
        mBinding.tvUserName.text = "${mUserHolder.firstName} ${mUserHolder.lastName}"
        mBinding.tvEmail.text = mUserHolder.email
        mBinding.tvMobileNumber.text = mUserHolder.phone
        mBinding.tvCity.text = cityName
        mBinding.tvRating.text = rating
    }

    private fun showLoadingIndicator(progress: Boolean) {
        mBinding.progressBar.visibility = if (progress) View.VISIBLE else View.GONE
    }

}
