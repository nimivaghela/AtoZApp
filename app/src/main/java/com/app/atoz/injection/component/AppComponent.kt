package com.app.atoz.injection.component

import com.app.atoz.injection.module.AppModule
import com.app.atoz.injection.module.UserHolderModule
import com.app.atoz.services.*
import com.app.atoz.ui.auth.changepassword.ChangePasswordActivity
import com.app.atoz.ui.auth.forgotpassword.ForgotPasswordActivity
import com.app.atoz.ui.auth.signin.SignInActivity
import com.app.atoz.ui.auth.signup.SignUpActivity
import com.app.atoz.ui.auth.verification.VerificationActivity
import com.app.atoz.ui.editprofile.UserEditProfileActivity
import com.app.atoz.ui.main.MainActivity
import com.app.atoz.ui.myorders.MyOrdersChildFragment
import com.app.atoz.ui.myorders.orderdetails.OrderDetailsActivity
import com.app.atoz.ui.myorders.orderdetails.editorderdetails.EditOrderDetailsDialog
import com.app.atoz.ui.policy.PrivacyPolicyActivity
import com.app.atoz.ui.provider.addservice.AddServicesActivity
import com.app.atoz.ui.provider.existservice.ExistServiceActivity
import com.app.atoz.ui.provider.existservice.editservice.EditPersonalServiceActivity
import com.app.atoz.ui.provider.home.ProviderHomeFragment
import com.app.atoz.ui.provider.jobdetails.JobDetailsActivity
import com.app.atoz.ui.provider.jobdetails.jobdoneotp.JobDoneOTPDialog
import com.app.atoz.ui.provider.profile.ProviderProfileFragment
import com.app.atoz.ui.provider.ratecustomer.ProviderOtherUserProfileActivity
import com.app.atoz.ui.provider.selectservice.SelectServiceActivity
import com.app.atoz.ui.settings.SettingsFragment
import com.app.atoz.ui.user.address.AddAddressActivity
import com.app.atoz.ui.user.address.AddressPikerActivity
import com.app.atoz.ui.user.category.CategoryActivity
import com.app.atoz.ui.user.home.HomeFragment
import com.app.atoz.ui.notification.NotificationsActivity
import com.app.atoz.ui.user.paymentmethod.PaymentMethodActivity
import com.app.atoz.ui.user.postrequest.PostRequestActivity
import com.app.atoz.ui.user.profile.UserMyProfileFragment
import com.app.atoz.ui.user.rateprovider.UserProviderProfileActivity
import com.app.atoz.ui.user.subcategory.SubcategoryActivity
import com.app.atoz.ui.user.subscription.SubscriptionActivity
import com.app.atoz.ui.user.subscriptionpayment.SubscriptionPaymentActivity
import com.app.atoz.ui.user.summary.SummaryActivity
import com.app.atoz.ui.viewrating.ViewRatingActivity
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, UserHolderModule::class])
interface AppComponent {
    fun inject(activity: SignUpActivity)
    fun inject(activity: VerificationActivity)
    fun inject(activity: OrderDetailsActivity)
    fun inject(fragment: SettingsFragment)
    fun inject(activity: MainActivity)
    fun inject(activity: UserEditProfileActivity)
    fun inject(activity: SelectServiceActivity)
    fun inject(activity: AddServicesActivity)
    fun inject(activity: ExistServiceActivity)
    fun inject(activity: JobDetailsActivity)
    fun inject(dialog: JobDoneOTPDialog)
    fun inject(activity: SignInActivity)
    fun inject(activity: ForgotPasswordActivity)
    fun inject(activity: PrivacyPolicyActivity)
    fun inject(fragment: UserMyProfileFragment)
    fun inject(fragment: ProviderProfileFragment)
    fun inject(activity: AddressPikerActivity)
    fun inject(activity: AddAddressActivity)
    fun inject(activity: ChangePasswordActivity)
    fun inject(activity: CategoryActivity)
    fun inject(activity: SubcategoryActivity)
    fun inject(activity: PostRequestActivity)
    fun inject(activity: SummaryActivity)
    fun inject(activity: PaymentMethodActivity)
    fun inject(fragment: HomeFragment)
    fun inject(fragment: ProviderHomeFragment)
    fun inject(fragment: MyOrdersChildFragment)
    fun inject(dialog: EditOrderDetailsDialog)
    fun inject(activity: EditPersonalServiceActivity)
    fun inject(service: UpdateFCMTokenService)
    fun inject(service: MyFirebaseMessagingService)
    fun inject(activity: UserProviderProfileActivity)
    fun inject(activity: ProviderOtherUserProfileActivity)
    fun inject(activity: NotificationsActivity)
    fun inject(service: CityStateStoreService)
    fun inject(activity: ViewRatingActivity)
    fun inject(service: PaymentDueService)
    fun inject(activity: SubscriptionActivity)
    fun inject(activity: SubscriptionPaymentActivity)
    fun inject(service: ActiveSubscriptionService)
}