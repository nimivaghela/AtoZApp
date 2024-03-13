package com.app.atoz.ui.settings


import android.app.AlertDialog
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.app.atoz.AppApplication
import com.app.atoz.R
import com.app.atoz.common.extentions.snack
import com.app.atoz.common.helper.RxHelper
import com.app.atoz.databinding.FragmentSettingsBinding
import com.app.atoz.db.database.AppDatabase
import com.app.atoz.services.PaymentDueService
import com.app.atoz.shareddata.base.BaseFragment
import com.app.atoz.ui.auth.changepassword.ChangePasswordActivity
import com.app.atoz.ui.auth.signin.SignInActivity
import com.app.atoz.ui.contactus.ContactUsActivity
import com.app.atoz.ui.editprofile.UserEditProfileActivity
import com.app.atoz.ui.policy.PrivacyPolicyActivity
import com.app.atoz.ui.user.address.AddressPikerActivity
import com.app.atoz.ui.user.subscription.SubscriptionActivity
import com.app.atoz.ui.viewrating.ViewRatingActivity
import com.app.atoz.utils.Config
import com.razorpay.Checkout
import timber.log.Timber
import javax.inject.Inject

class SettingsFragment : BaseFragment() {
    companion object {
        fun newInstance(): SettingsFragment {
            return SettingsFragment()
        }
    }

    override fun getInflateResource(): Int = R.layout.fragment_settings

    private lateinit var mBinding: FragmentSettingsBinding

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory

    private val mViewModel by lazy {
        ViewModelProviders.of(this, mViewModelFactory)[SettingsViewModel::class.java]
    }

    override fun initView() {
        (requireActivity().application as AppApplication).mComponent.inject(this)
        mBinding = getBinding()
        setTitle(getString(R.string.settings))
        initObserver()
    }

    private fun initObserver() {
        mViewModel.getLogoutObserver().observe(this, Observer { requestState ->
            showLoadingIndicator(requestState.progress)
            requestState.data?.let {
                mViewModel.mUserHolder.clearData()
                SignInActivity.start(requireContext())
                requireActivity().finish()
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

    private fun showLoadingIndicator(isShow: Boolean) {
        mBinding.progressBar.visibility = if (isShow) View.VISIBLE else View.GONE
    }

    override fun postInit() {
        mBinding.tvAddressManager.visibility = if (mViewModel.mUserHolder.isUserAsProvider) View.GONE else View.VISIBLE
        mBinding.addressManagerViewDivider.visibility =
            if (mViewModel.mUserHolder.isUserAsProvider) View.GONE else View.VISIBLE
        mBinding.tvEditProfile.visibility = if (mViewModel.mUserHolder.isUserAsProvider) View.GONE else View.VISIBLE
        mBinding.tvSubscription.visibility = if (mViewModel.mUserHolder.isUserAsProvider) View.GONE else View.VISIBLE
        mBinding.subscriptionViewDivider.visibility =
            if (mViewModel.mUserHolder.isUserAsProvider) View.GONE else View.VISIBLE
        mBinding.profileViewDivider.visibility =
            if (mViewModel.mUserHolder.isUserAsProvider) View.GONE else View.VISIBLE

    }

    override fun handleListener() {
        RxHelper.onClick(mBinding.tvAddressManager, mDisposable) {
            AddressPikerActivity.start(requireContext(), true)
        }

        RxHelper.onClick(mBinding.tvEditProfile, mDisposable) {
            activity?.let {
                UserEditProfileActivity.start(it, true)
            }
        }

        RxHelper.onClick(mBinding.tvChangePassword, mDisposable) {
            ChangePasswordActivity.start(requireContext())
        }

        RxHelper.onClick(mBinding.tvRateReview, mDisposable) {
            activity?.let {
                ViewRatingActivity.start(it, mViewModel.mUserHolder.userId!!)
            }
        }

        RxHelper.onClick(mBinding.tvTermsOfUse, mDisposable) {
            PrivacyPolicyActivity.start(requireContext(), false)
        }

        RxHelper.onClick(mBinding.tvPrivacyPolicy, mDisposable) {
            PrivacyPolicyActivity.start(requireContext(), true)
        }

        RxHelper.onClick(mBinding.tvShare, mDisposable) {
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(
                Intent.EXTRA_TEXT,
                "Hey check out AtoZ9(Get Any type of Services at Your Doorstep) " +
                        "app at: https://play.google.com/store/apps/details?id=com.app.atoz"
            )
            sendIntent.type = "text/plain"
            startActivity(sendIntent)
        }

        RxHelper.onClick(mBinding.tvRateUs, mDisposable) {
            val uri = Uri.parse("market://details?id=" + activity?.packageName)
            val goToMarket = Intent(Intent.ACTION_VIEW, uri)
            goToMarket.addFlags(
                Intent.FLAG_ACTIVITY_NO_HISTORY
                        or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
            )
            try {
                startActivity(goToMarket)
            } catch (e: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + activity?.packageName)
                    )
                )
            }
        }

        RxHelper.onClick(mBinding.tvSubscription, mDisposable) {
            SubscriptionActivity.start(requireContext())
        }

        RxHelper.onClick(mBinding.tvContactUs, mDisposable) {
            ContactUsActivity.start(requireContext())
        }

        RxHelper.onClick(mBinding.tvLogout, mDisposable) {
            activity?.let {
                PaymentDueService.startService(it)
            }
            showLogOutDialog()
        }
    }

    private fun clearNotification() {
        val notificationManager = activity?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

    private fun deleteActiveSubscriptionData() {
        try {
            Timber.d("Delete deleteActiveSubscription data")
            activity?.let { AppDatabase.getInstance(it).activeSubscriptionPlanDao().deleteAllData() }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showLogOutDialog() {
        val builder = AlertDialog.Builder(requireContext())

        builder.setMessage(getString(R.string.log_out_message))

        builder.setPositiveButton(getString(R.string.text_yes)) { _, _ ->
            deleteActiveSubscriptionData()
            clearNotification()
            Checkout.clearUserData(activity)

            mViewModel.logout(isInternetConnected, this, mDisposable)
        }

        builder.setNegativeButton(getString(R.string.text_cancel)) { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog: AlertDialog = builder.create()

        alertDialog.setOnShowListener {
            val negativeButton = (it as AlertDialog).getButton(DialogInterface.BUTTON_NEGATIVE)
            val positiveButton = it.getButton(DialogInterface.BUTTON_POSITIVE)

            positiveButton.setTextColor(ContextCompat.getColor(it.context, android.R.color.white))

            negativeButton.setBackgroundColor(ContextCompat.getColor(it.context, android.R.color.white))
            negativeButton.setTextColor(ContextCompat.getColor(it.context, android.R.color.black))

        }
        alertDialog.show()
    }

    override fun displayMessage(message: String) {
        mBinding.svRootView.snack(message)
    }
}