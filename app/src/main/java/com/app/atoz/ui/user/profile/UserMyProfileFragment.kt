package com.app.atoz.ui.user.profile

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
import com.app.atoz.databinding.FragmentUserMyProfileBinding
import com.app.atoz.models.ProfileResponseModel
import com.app.atoz.models.UserHolder
import com.app.atoz.shareddata.base.BaseFragment
import com.app.atoz.ui.editprofile.UserEditProfileActivity
import com.app.atoz.utils.Config
import timber.log.Timber
import javax.inject.Inject

class UserMyProfileFragment : BaseFragment() {
    override fun getInflateResource() = R.layout.fragment_user_my_profile

    companion object {
        fun newInstance(): UserMyProfileFragment {
            return UserMyProfileFragment()
        }
    }

    private lateinit var mBinding: FragmentUserMyProfileBinding
    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var mUserHolder: UserHolder

    private val mViewModel: UserMyProfileViewModel by lazy {
        ViewModelProviders.of(this, mViewModelFactory)[UserMyProfileViewModel::class.java]
    }

    override fun initView() {
        (activity!!.application as AppApplication).mComponent.inject(this)
        mBinding = getBinding()
        initObserver()
        bindViewData(null)
    }

    override fun postInit() {
        callViewProfile()
    }

    private fun callViewProfile() {
        mViewModel.getUserProfile(isInternetConnected, this, mDisposable)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            UserEditProfileActivity.KEY_EDIT_PROFILE -> {
                if (resultCode == Activity.RESULT_OK) {
                    activity?.let {
                        callViewProfile()
                    }
                }
            }
        }
    }

    override fun handleListener() {
        RxHelper.onClick(mBinding.ivEditProfile, mDisposable) {
            activity?.let {
                UserEditProfileActivity.start(it)
            }
        }
    }

    override fun displayMessage(message: String) {
        mBinding.flRootView.snack(message)
    }

    private fun initObserver() {
        mViewModel.viewProfileRequest().observe(this, Observer { requestState ->
            showLoadingIndicator(requestState.progress)
            requestState.data?.let {
                Timber.d("Successfully done")
                bindViewData(it.data)
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

    @SuppressLint("SetTextI18n")
    private fun bindViewData(response: ProfileResponseModel?) {
        mBinding.ivProfileImage
            .loadImage(mUserHolder.imageUrl, null, null, R.drawable.bg_circle_grey_camera, true)
        mBinding.ivProfileImageBackground.loadImage(
            mUserHolder.imageUrl, null, null,
            R.drawable.bg_circle_grey_camera, false
        )
        mBinding.tvUserName.text = "${mUserHolder.firstName} ${mUserHolder.lastName}"
        mBinding.tvEmail.text = mUserHolder.email
        mBinding.tvMobileNumber.text = mUserHolder.phone
        response?.userAddress?.apply {
            mBinding.tvAddress.text = "$address \n\nLocation : $location"
        } ?: let {
            mBinding.tvAddress.text = mUserHolder.address
        }
    }

    private fun showLoadingIndicator(progress: Boolean) {
        mBinding.progressBar.visibility = if (progress) View.VISIBLE else View.GONE
    }

}
