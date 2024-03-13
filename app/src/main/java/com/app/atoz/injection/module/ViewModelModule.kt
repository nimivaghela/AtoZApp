package com.app.atoz.injection.module

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.app.atoz.shareddata.AtoZViewModelFactory
import com.app.atoz.ui.auth.changepassword.ChangePasswordViewModel
import com.app.atoz.ui.auth.forgotpassword.ForgotPasswordViewModel
import com.app.atoz.ui.auth.signin.SignInViewModel
import com.app.atoz.ui.auth.signup.SignUpViewModel
import com.app.atoz.ui.auth.verification.VerificationViewModel
import com.app.atoz.ui.editprofile.UserEditProfileViewModel
import com.app.atoz.ui.myorders.MyOrdersViewModel
import com.app.atoz.ui.myorders.orderdetails.editorderdetails.EditOrderDetailsViewModel
import com.app.atoz.ui.policy.ContentPagesViewModel
import com.app.atoz.ui.provider.addservice.AddServiceViewModel
import com.app.atoz.ui.provider.existservice.ExistServiceViewModel
import com.app.atoz.ui.provider.existservice.editservice.EditPersonalServiceViewModel
import com.app.atoz.ui.provider.home.ProviderHomeViewModel
import com.app.atoz.ui.provider.jobdetails.JobDetailsViewModel
import com.app.atoz.ui.provider.jobdetails.jobdoneotp.JobDoneOtpViewModel
import com.app.atoz.ui.provider.profile.ProviderProfileViewModel
import com.app.atoz.ui.provider.ratecustomer.ProviderOtherUserProfileViewModel
import com.app.atoz.ui.provider.selectservice.SelectServiceViewModel
import com.app.atoz.ui.settings.SettingsViewModel
import com.app.atoz.ui.user.address.AddressViewModel
import com.app.atoz.ui.user.category.CategoryViewModel
import com.app.atoz.ui.user.home.HomeViewModel
import com.app.atoz.ui.notification.NotificationViewModel
import com.app.atoz.ui.user.paymentmethod.PaymentMethodViewModel
import com.app.atoz.ui.user.postrequest.PostRequestViewModel
import com.app.atoz.ui.user.profile.UserMyProfileViewModel
import com.app.atoz.ui.user.rateprovider.UserProviderProfileViewModel
import com.app.atoz.ui.user.subcategory.SubcategoryViewModel
import com.app.atoz.ui.user.subscription.SubscriptionViewModel
import com.app.atoz.ui.user.subscriptionpayment.SubscriptionPaymentViewModel
import com.app.atoz.ui.user.summary.SummaryViewModel
import com.app.atoz.ui.viewrating.ViewRatingViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {
    @Binds
    abstract fun bindViewModelFactory(factory: AtoZViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(SignUpViewModel::class)
    internal abstract fun bindSignUpViewModel(myViewModel: SignUpViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SignInViewModel::class)
    internal abstract fun bindSignInViewModel(myViewModel: SignInViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(VerificationViewModel::class)
    internal abstract fun bindVerificationViewModel(myViewModel: VerificationViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ForgotPasswordViewModel::class)
    internal abstract fun bindForgotPasswordViewModel(myViewModel: ForgotPasswordViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SelectServiceViewModel::class)
    internal abstract fun bindSelectServiceViewModel(myViewModel: SelectServiceViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AddServiceViewModel::class)
    internal abstract fun bindAddServiceViewModel(myViewModel: AddServiceViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ExistServiceViewModel::class)
    internal abstract fun bindExistServiceViewModel(myViewModel: ExistServiceViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(EditPersonalServiceViewModel::class)
    internal abstract fun bindEditServiceViewModel(myViewModel: EditPersonalServiceViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(JobDetailsViewModel::class)
    internal abstract fun bindJobDetailsViewModel(myViewModel: JobDetailsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(JobDoneOtpViewModel::class)
    internal abstract fun bindJobDoneOtpViewModel(myViewModel: JobDoneOtpViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AddressViewModel::class)
    internal abstract fun bindAddressViewModel(myViewModel: AddressViewModel): ViewModel


    @Binds
    @IntoMap
    @ViewModelKey(ContentPagesViewModel::class)
    internal abstract fun bindContentPagesViewModel(myViewModel: ContentPagesViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UserMyProfileViewModel::class)
    internal abstract fun bindUserMyProfileViewModel(myViewModel: UserMyProfileViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ProviderProfileViewModel::class)
    internal abstract fun bindProviderMyProfileViewModel(myViewModel: ProviderProfileViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UserEditProfileViewModel::class)
    internal abstract fun bindEditProfileViewModel(myViewModel: UserEditProfileViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ChangePasswordViewModel::class)
    internal abstract fun bindChangePasswordViewModel(myViewModel: ChangePasswordViewModel): ViewModel


    @Binds
    @IntoMap
    @ViewModelKey(MyOrdersViewModel::class)
    internal abstract fun bindMyOrderViewModel(myViewModel: MyOrdersViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(EditOrderDetailsViewModel::class)
    internal abstract fun bindEditOrderDetailsViewModel(myViewModel: EditOrderDetailsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(HomeViewModel::class)
    internal abstract fun bindHomeViewModel(myViewModel: HomeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CategoryViewModel::class)
    internal abstract fun bindCategoryViewModel(myViewModel: CategoryViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SubcategoryViewModel::class)
    internal abstract fun bindSubCategoryViewModel(myViewModel: SubcategoryViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PostRequestViewModel::class)
    internal abstract fun bindPostRequestViewModel(myViewModel: PostRequestViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ProviderHomeViewModel::class)
    internal abstract fun bindPRoviderHomeViewModel(myViewModel: ProviderHomeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SummaryViewModel::class)
    internal abstract fun bindSummaryViewModel(myViewModel: SummaryViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PaymentMethodViewModel::class)
    internal abstract fun bindPaymentMethodViewModel(myViewModel: PaymentMethodViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UserProviderProfileViewModel::class)
    internal abstract fun bindUserProviderProfileViewModel(myViewModel: UserProviderProfileViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ProviderOtherUserProfileViewModel::class)
    internal abstract fun bindProviderOtherUserProfileViewModel(myViewModel: ProviderOtherUserProfileViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SettingsViewModel::class)
    internal abstract fun bindSettingsViewModel(myViewModel: SettingsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(NotificationViewModel::class)
    internal abstract fun bindNotificationViewModel(myViewModel: NotificationViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ViewRatingViewModel::class)
    internal abstract fun bindViewRatingViewModel(myViewModel: ViewRatingViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SubscriptionViewModel::class)
    internal abstract fun bindSubscriptionViewModel(myViewModel: SubscriptionViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SubscriptionPaymentViewModel::class)
    internal abstract fun bindSubscriptionPaymentViewModel(myViewModel: SubscriptionPaymentViewModel): ViewModel

}