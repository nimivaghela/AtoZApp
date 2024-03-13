package com.app.atoz.shareddata.endpoint

import com.app.atoz.models.*
import com.app.atoz.models.address.AddressListItem
import com.app.atoz.models.address.AddressModel
import com.app.atoz.models.address.AddressRequestModel
import com.app.atoz.models.homescreen.HomeResponse
import com.app.atoz.models.notification.NotificationModel
import com.app.atoz.models.providerhome.ProviderHomeModel
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.reactivex.Observable
import okhttp3.MultipartBody
import retrofit2.http.*

interface ApiEndPoint {

    @POST("/signup")
    fun signUp(@Body body: MultipartBody?): Observable<AtoZResponseModel<SignUpSignInResponse>>

    @POST("/check-email-phone-exist")
    fun checkPhoneEmailExist(@Body body: JsonObject?): Observable<AtoZResponseModel<PhoneEmailExistResponseModel>>

    @POST("/signin")
    fun signIn(@Body body: JsonObject?): Observable<AtoZResponseModel<SignUpSignInResponse>>

    @POST("/socialLogin")
    fun socialLogin(
        @Header("usertype") userType: String? = "user",
        @Body body: SocialLogin?
    ): Observable<AtoZResponseModel<SignUpSignInResponse>>

    @POST("/check-socialid-exist")
    fun checkIsSocialIDExist(
        @Header("usertype") userType: String = "user",
        @Body body: JsonObject?
    ): Observable<AtoZResponseModel<IsSocialMediaExistResponse>>

    @GET("/cities/list-with-state")
    fun getCities(): Observable<AtoZResponseModel<CityModel>>

    @POST("/verify-user")
    fun verifyPhone(
        @Header("authorization") authToken: String?,
        @Body body: JsonObject?
    ): Observable<AtoZResponseModel<VerificationResponse>>

    @POST("/forgotpassword")
    fun forgotPassword(@Body body: JsonObject?): Observable<AtoZResponseModel<Any>>

    @GET("/address/profile/")
    fun getAddress(
        @Header("authorization") authToken: String?
    ): Observable<AtoZResponseModel<AddressModel>>

    @POST("/address/profile")
    fun addAddress(
        @Header("authorization") authToken: String?,
        @Body body: AddressRequestModel?
    ): Observable<AtoZResponseModel<AddressListItem>>

    @DELETE("/address/profile/{addressID}")
    fun deleteAddress(
        @Header("authorization") authToken: String?,
        @Path("addressID") addressID: Int?
    ): Observable<AtoZResponseModel<AddressListItem>>

    @GET("/pages/{input}")
    fun getContentPage(
        @Path("input")
        pageName: String?
    ): Observable<AtoZResponseModel<String>>

    @POST("/services/provider/list-by-lat-long")
    fun getCategoryWithServiceList(
        @Body body: JsonObject?
    ): Observable<AtoZResponseModel<CategorySelectionResponseModel>>

    @POST("/services/get-providers-services")
    fun getProvidersExistServices(
        @Header("authorization") authToken: String?,
        @Body body: JsonObject?
    ): Observable<AtoZResponseModel<CategorySelectionResponseModel>>

    @GET("/view-profile/{user_id}")
    fun getProfile(
        @Header("authorization") authToken: String?,
        @Path("user_id") userId: String?
    ): Observable<AtoZResponseModel<ProfileResponseModel>>

    @POST("/edit-profile")
    fun editProfile(
        @Header("authorization") authToken: String?,
        @Body body: MultipartBody?
    ): Observable<AtoZResponseModel<ProfileResponseModel>>

    @POST("/edit-profile")
    fun addDeleteService(
        @Header("authorization") authToken: String?,
        @Body body: MultipartBody?
    ): Observable<AtoZResponseModel<Any>>

    @POST("/change-password")
    fun changePassword(
        @Header("authorization") authToken: String?,
        @Body body: JsonObject?
    ): Observable<AtoZResponseModel<Any>>

    @GET("/service-requests/list-by-status/{status_id}")
    fun getServiceList(
        @Header("authorization") authToken: String?,
        @Path("status_id") statusId: String?
    ): Observable<AtoZResponseModel<UserServiceStatusList>>

    @POST("/services/search/v3")
    fun provideHomeService(
        @Header("authorization") authToken: String?,
        @Body body: JsonObject?
    ): Observable<AtoZResponseModel<HomeResponse>>

    @POST("/services/list-by-location")
    fun getSubcategoryOrChildCategoryList(
        @Header("authorization") authToken: String?,
        @Body body: JsonObject?
    ): Observable<AtoZResponseModel<CategorySelectionResponseModel>>

    @GET("/service-requests/provider/list-by-status/{status_id}")
    fun getProviderServiceList(
        @Header("authorization") authToken: String?,
        @Path("status_id") statusId: String?
    ): Observable<AtoZResponseModel<UserServiceStatusList>>

    @POST("/service-requests/provider/change-request-status")
    fun changeRequestStatus(
        @Header("authorization") authToken: String?,
        @Body body: RequestChangeInput?
    ): Observable<AtoZResponseModel<Any>>

    @POST("/service-requests/v2/profile")
    fun postOrderRequest(
        @Header("authorization") authToken: String?,
        @Body body: MultipartBody?
    ): Observable<AtoZResponseModel<Int>>

    @GET("/service-requests/v2/order-summary/{request_id}")
    fun getOrderSummary(
        @Header("authorization") authToken: String?,
        @Path("request_id") requestId: String?
    ): Observable<AtoZResponseModel<SummaryResponseModel>>

    @POST("/transactions/profile")
    fun orderPayment(
        @Header("authorization") authToken: String?,
        @Body body: JsonObject?
    ): Observable<AtoZResponseModel<Any>>

    @GET("/service-requests/v2/provider/detail/{job_id}")
    fun getJobDetail(
        @Header("authorization") authToken: String?,
        @Path("job_id") statusId: String?
    ): Observable<AtoZResponseModel<OrderDetailModel>>

    @GET("/service-requests/v2/detail/{order_id}")
    fun getOrderDetail(
        @Header("authorization") authToken: String?,
        @Path("order_id") statusId: String?
    ): Observable<AtoZResponseModel<OrderDetailModel>>

    @POST("/call/make-call")
    fun getOtpForCall(
        @Header("authorization") authToken: String?,
        @Body body: JsonObject?
    ): Observable<AtoZResponseModel<CallModel>>

    @POST("/service-requests/provider/upload-bill")
    fun uploadBill(
        @Header("authorization") authToken: String?,
        @Body body: MultipartBody?
    ): Observable<AtoZResponseModel<List<OrderDetailModel.ServiceRequest.ServiceRequestImage>>>

    @GET("/service-requests/provider/request-list")
    fun providerRequest(
        @Header("authorization") authToken: String?
    ): Observable<AtoZResponseModel<ProviderHomeModel>>

    @POST("/service-requests/check-otp")
    fun otpCheck(
        @Header("authorization") authToken: String?,
        @Body body: JsonObject?
    ): Observable<AtoZResponseModel<JsonObject>>

    @POST("/service-requests/provider/complete-job/v2")
    fun completeJob(
        @Header("authorization") authToken: String?,
        @Body body: MultipartBody?
    ): Observable<AtoZResponseModel<Any>>

    @POST("/service-requests/edit-order-note")
    fun editOrderDetails(
        @Header("authorization") authToken: String?,
        @Body body: JsonObject?
    ): Observable<AtoZResponseModel<Any>>

    @POST("/services/service-list-with-price")
    fun getChildServices(
        @Header("authorization") authToken: String?,
        @Body body: JsonObject?
    ): Observable<AtoZResponseModel<CategorySelectionResponseModel>>

    @POST("/services/price-change-request")
    fun providerPriceChangeRequest(
        @Header("authorization") authToken: String?,
        @Body body: JsonObject?
    ): Observable<AtoZResponseModel<Any>>

    @GET("/ratings/list/{user_id}")
    fun getUserProviderRatingsComment(
        @Header("authorization") authToken: String?,
        @Path("user_id") userId: String?
    ): Observable<AtoZResponseModel<ProviderUserRatingResponse>>

    @GET("/ratings/get/{user_id}")
    fun getUserProviderPersonalRatingsComment(
        @Header("authorization") authToken: String?,
        @Path("user_id") userId: String?
    ): Observable<AtoZResponseModel<ArrayList<UserRatingModel>>>

    @POST("/ratings/add")
    fun sendRating(
        @Header("authorization") authToken: String?,
        @Body bodyData: JsonObject?
    ): Observable<AtoZResponseModel<Any>>

    @FormUrlEncoded
    @POST("/notifications/my-notifications")
    fun notificationList(
        @Header("authorization") authToken: String?,
        @Field("user_id") userId: String?
    ): Observable<AtoZResponseModel<NotificationModel>>

    @POST("/logout")
    fun logout(
        @Header("authorization") authToken: String?
    ): Observable<AtoZResponseModel<Any>>


    @GET("/settings/timer")
    fun getTimerValue(
        @Header("authorization") authToken: String?
    ): Observable<AtoZResponseModel<JsonElement>>

    @POST("/coupons/verify-coupon")
    fun verifyCouponCode(
        @Header("authorization") authToken: String?,
        @Body body: JsonObject?
    ): Observable<AtoZResponseModel<CouponCode>>

    @GET("/subscription/list")
    fun getSubscriptionList(
        @Header("authorization") authToken: String?
    ): Observable<AtoZResponseModel<SubscriptionResponse>>

    @GET("subscription/detail/{user_id}")
    fun getActiveSubscriptionPlan(
        @Header("authorization") authToken: String?,
        @Path("user_id") userId: String?
    ): Observable<AtoZResponseModel<ActiveSubscriptionPlan>>

    @POST("/subscription/purchase")
    fun buySubscriptionPlan(
        @Header("authorization") authToken: String?,
        @Body body: JsonObject?
    ): Observable<AtoZResponseModel<Any>>
}