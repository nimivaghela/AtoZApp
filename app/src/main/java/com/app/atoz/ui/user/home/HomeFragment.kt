package com.app.atoz.ui.user.home


import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.atoz.AppApplication
import com.app.atoz.R
import com.app.atoz.common.extentions.snack
import com.app.atoz.common.helper.AUTOCOMPLETE_REQUEST_CODE
import com.app.atoz.common.helper.RxHelper
import com.app.atoz.models.AdvertisementItem
import com.app.atoz.models.UserHolder
import com.app.atoz.models.homescreen.ChildrenItem
import com.app.atoz.models.homescreen.CouponItem
import com.app.atoz.models.homescreen.HomeModel
import com.app.atoz.models.homescreen.HomeModel.Companion.TYPE_ADVERTISEMENT
import com.app.atoz.models.homescreen.HomeModel.Companion.TYPE_PROMO_CODE
import com.app.atoz.models.homescreen.HomeModel.Companion.TYPE_SERVICE
import com.app.atoz.models.homescreen.ServicesItem
import com.app.atoz.shareddata.base.BaseFragment
import com.app.atoz.ui.notification.NotificationsActivity
import com.app.atoz.ui.user.category.CategoryActivity
import com.app.atoz.ui.user.home.advertise.AdvertiseDetailsActivity
import com.app.atoz.ui.user.home.coupon.CouponDetailsDialog
import com.app.atoz.ui.user.subcategory.SubcategoryActivity
import com.app.atoz.utils.Config
import com.app.atoz.utils.LocationRequestResult
import com.app.atoz.utils.isLocationServiceEnabled
import com.app.atoz.utils.showLocationSettingsAlert
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.item_no_location_found.view.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class HomeFragment : BaseFragment(), EasyPermissions.PermissionCallbacks {
    companion object {
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var mUserHolder: UserHolder

    private lateinit var mBinding: com.app.atoz.databinding.FragmentHomeBinding

    private val homeViewModel: HomeViewModel by lazy {
        ViewModelProviders
            .of(this, mViewModelFactory)[HomeViewModel::class.java]
    }

    private var isManualSearchRefresh = false

    private lateinit var mItemClickListener: ItemClickListener
    private var mLatLng: LatLng? = null
    private val mFields = mutableListOf(
        Place.Field.ID,
        Place.Field.ADDRESS,
        Place.Field.ADDRESS_COMPONENTS,
        Place.Field.NAME,
        Place.Field.LAT_LNG
    )

    override fun getInflateResource() = R.layout.fragment_home

    override fun initView() {
        mBinding = getBinding()
        (requireActivity().application as AppApplication).mComponent.inject(this)

        mBinding.rvHome.layoutManager = LinearLayoutManager(requireContext())

        mBinding.userName = mUserHolder.firstName?.capitalize()

        initObserver()

        isGpsCheck()

        mBinding.pullToRefresh.setOnRefreshListener {
            mLatLng?.let {
                populateHomeData(
                    it.latitude.toString(),
                    it.longitude.toString()
                )
            } ?: isGpsCheck()
        }
    }

    fun initObserver() {
        homeViewModel.getHomeDataObserver().observe(this, Observer { t ->
            t?.let { requestState ->
                showLoadingIndicator(requestState.progress)
                requestState.data?.let {
                    Timber.d("New Response of home screen is here ${Gson().toJson(it)}")
                    it.data?.apply {
                        isManualSearchRefresh = true
                        mBinding.tieSearch.setText("")
                        bindListData(this)
                    } ?: let {
                        mBinding.tvNoService.visibility = View.VISIBLE
                    }

                    mBinding.pullToRefresh.isRefreshing = false

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
                    showLoadingIndicator(requestState.progress)
                    mBinding.pullToRefresh.isRefreshing = false

                }
            }
        })
    }

    private fun bindListData(homeList: ArrayList<HomeModel>) {
        if (homeList.size == 0) {
            mBinding.rlHomeBg.visibility = View.GONE
            mBinding.tvNoService.visibility = View.VISIBLE
        } else {
            mBinding.rlHomeBg.visibility = View.VISIBLE
            mBinding.tvNoService.visibility = View.GONE
            mBinding.rvHome.adapter = HomeAdapter(homeList, mDisposable, mItemClickListener,
                object : AdvertiseAdapter.OnAdvertiseClickListener {
                    override fun onClick(advertisementItem: AdvertisementItem, isVideo: Boolean) {
                        onClickOfAdvertisement(advertisementItem, isVideo)
                    }
                },
                object : MoreSavingAdapter.OnCouponCodeClickListener {
                    override fun onClick(couponItem: CouponItem) {
                        onClickOfCouponCode(couponItem)
                    }
                })
        }
    }

    private fun onClickOfAdvertisement(advertisementItem: AdvertisementItem, isVideo: Boolean) {
        if (isVideo) {
            val fragmentTransaction =
                activity?.supportFragmentManager?.beginTransaction()
            val previousFragment =
                activity?.supportFragmentManager?.findFragmentByTag(AdvertiseVideoDialog.ADVERTISE_VIDEO_DIALOG_TAG)
            previousFragment?.let {
                fragmentTransaction?.remove(previousFragment)
            }
            fragmentTransaction?.addToBackStack(null)
            val advertiseDialog = advertisementItem.mediaUrl?.let { AdvertiseVideoDialog.newInstance(it) }
            fragmentTransaction?.let { it1 ->
                advertiseDialog?.show(
                    it1,
                    AdvertiseVideoDialog.ADVERTISE_VIDEO_DIALOG_TAG
                )
            }
        } else {
            activity?.let { AdvertiseDetailsActivity.newIntent(it, advertisementItem) }
        }
    }

    private fun onClickOfCouponCode(couponItem: CouponItem) {
        val fragmentTransaction =
            activity?.supportFragmentManager?.beginTransaction()
        val previousFragment =
            activity?.supportFragmentManager?.findFragmentByTag(CouponDetailsDialog.COUPON_DETAILS_DIALOG_TAG)
        previousFragment?.let {
            fragmentTransaction?.remove(previousFragment)
        }
        fragmentTransaction?.addToBackStack(null)
        val couponDetailsDialog = CouponDetailsDialog.newInstance(couponItem)
        fragmentTransaction?.let { it1 ->
            couponDetailsDialog.show(it1, CouponDetailsDialog.COUPON_DETAILS_DIALOG_TAG)
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        }
        locationDisableViewVisibility(true)
        showLoadingIndicator(false)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {

    }

    private fun isGpsCheck() {
        if (isInternetConnected) {
            if (isLocationServiceEnabled(requireContext())) {
                checkLocationPermission()
            } else {
                displayMessage("Please Enable GPS")
                showLocationSettingsAlert(requireContext(), object : LocationRequestResult {
                    override fun onComplete(result: Boolean) {
                        locationDisableViewVisibility(false)
                        checkLocationPermission()
                    }
                })
                locationDisableViewVisibility(true)
            }
        } else {
            displayMessage(getString(R.string.no_internet))
        }
    }

    private fun locationDisableViewVisibility(isShow: Boolean) {
        if (isShow) mBinding.pullToRefresh.isRefreshing = false
        mBinding.includeNoLocation.visibility = if (isShow) View.VISIBLE else View.GONE
        mBinding.tvNoService.visibility = if (isShow) View.GONE else View.VISIBLE
        mBinding.rlHomeBg.visibility = if (isShow) View.GONE else View.VISIBLE
        mBinding.tvLocation.visibility = if (isShow) View.GONE else View.VISIBLE
    }

    @AfterPermissionGranted(101)
    private fun checkLocationPermission() {
        showLoadingIndicator(true)
        if (EasyPermissions.hasPermissions(requireContext(), ACCESS_FINE_LOCATION)) {
            locationDisableViewVisibility(false)
            getUserLocationName()
        } else {
            EasyPermissions.requestPermissions(
                this,
                "Please allow location permission to this app to get location specific services",
                101,
                ACCESS_FINE_LOCATION
            )
        }
    }

    private fun showLoadingIndicator(progress: Boolean) {
        if (progress) {
            if (mBinding.pullToRefresh.isRefreshing.not()) {
                displayCustomProgressDialog(false)
            }
        } else {
            closeCustomProgressDialog()
        }
    }

    override fun postInit() {

    }

    private fun populateHomeData(lat: String, long: String) {
        homeViewModel.populateHomeData(isInternetConnected, this, mDisposable, lat, long)
    }

    override fun handleListener() {
        mItemClickListener = object : ItemClickListener {
            override fun onItemClick(view: View, position: Int, data: Any?) {
                when (view.id) {
                    R.id.tv_view_all -> {
                        val servicesItem = data as ServicesItem
                        activity?.let {
                            servicesItem.name?.let { categoryName ->
                                CategoryActivity.start(
                                    it,
                                    servicesItem.id.toString(),
                                    categoryName,
                                    mLatLng?.latitude.toString(),
                                    mLatLng?.longitude.toString()
                                )
                            }
                        }
                    }
                    else -> {
                        val childrenItem = data as JsonObject

                        SubcategoryActivity.start(
                            requireContext(), childrenItem.get("categoryID").asString,
                            childrenItem.get("subCategoryID").asString,
                            childrenItem.get("name").asString,
                            mLatLng?.latitude.toString(),
                            mLatLng?.longitude.toString()
                        )
                    }
                }
            }
        }

        RxHelper.onClick(mBinding.tvLocation, mDisposable) {
            if (Places.isInitialized()) {
                // Start the autocomplete intent.
                val intent = Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.OVERLAY, mFields
                ).setCountry("IN")
                    .build(requireContext())
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
            }
        }

        RxHelper.onClick(mBinding.frmNotification, mDisposable) {
            NotificationsActivity.start(requireContext())
        }

        RxHelper.onClick(mBinding.includeNoLocation.btn_retry, mDisposable) {
            isGpsCheck()
        }

        RxTextView.afterTextChangeEvents(mBinding.tieSearch)
            .skip(1)
            .filter {
                val goAhead = !isManualSearchRefresh
                isManualSearchRefresh = false
                goAhead
            }
            .debounce(300, TimeUnit.MILLISECONDS)
            .distinctUntilChanged()
            .subscribeOn(Schedulers.computation())
            .map { textAfterChangeEvent ->
                val searchText: String = textAfterChangeEvent.editable().toString()
                val homeData: ArrayList<HomeModel>? = homeViewModel.getHomeDataObserver().value?.data?.data
                val newHomeDataList: ArrayList<HomeModel> = ArrayList()
                if (homeData != null && searchText.isNotBlank()) {
                    for (data in homeData) {
                        when (data.viewType) {
                            TYPE_SERVICE -> {
                                data.serviceItem?.children?.apply {
                                    val serviceList = ArrayList<ChildrenItem>()
                                    for (service in this) {
                                        if (service.name != null
                                            && service.name.toLowerCase().startsWith(searchText.toLowerCase())
                                        ) {
                                            serviceList.add(service)
                                        }
                                    }
                                    if (serviceList.size > 0) {
                                        val serviceItem: ServicesItem
                                        data.serviceItem.let { item ->
                                            serviceItem = ServicesItem(
                                                item.image, item.originalPhoto, item.paymentType, item.level,
                                                serviceList, item.parentId, item.name, item.id
                                            )
                                        }
                                        newHomeDataList.add(HomeModel(TYPE_SERVICE, serviceItem))
                                    }
                                }
                            }
                            TYPE_ADVERTISEMENT -> {
                                data.advertisement?.let {
                                    newHomeDataList.add(HomeModel(TYPE_ADVERTISEMENT, advertisement = it))
                                }
                            }
                            TYPE_PROMO_CODE -> {
                                data.couponList?.let {
                                    newHomeDataList.add(HomeModel(TYPE_PROMO_CODE, couponList = it))
                                }
                            }
                        }
                    }
                }
                if (searchText.isNotBlank()) {
                    Timber.d("NewHome List size ${newHomeDataList.size} and search text $searchText")
                    newHomeDataList
                } else {
                    Timber.d("NewHome List size  homeData and search text $searchText")
                    homeData ?: ArrayList()
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                it?.let {
                    bindListData(it)
                } ?: let {
                    mBinding.tvNoService.visibility = View.VISIBLE
                }
            }, {
                it.printStackTrace()
            }).addTo(mDisposable)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {

                    val place = Autocomplete.getPlaceFromIntent(data!!)
                    mBinding.tvLocation.text = place.address
                    mBinding.tvYouAreIn.visibility = View.VISIBLE
                    mLatLng = place.latLng
                    populateHomeData(place.latLng?.latitude.toString(), place.latLng?.longitude.toString())
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    val status = Autocomplete.getStatusFromIntent(data!!)
                    Timber.i(status.statusMessage)
                }
                Activity.RESULT_CANCELED -> {
                    // The user canceled the operation.
                }
            }
        }
    }

    private fun getUserLocationName() {
        showLoadingIndicator(true)
        // Use fields to define the data types to return.
        val placeFields = mutableListOf(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)

        // Use the builder to create a FindCurrentPlaceRequest.
        val request = FindCurrentPlaceRequest.builder(placeFields)
            .build()


        // Call findCurrentPlace and handle the response (first check that the user has granted permission).
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Places.createClient(requireContext()).findCurrentPlace(request).addOnSuccessListener { response ->

                if (response.placeLikelihoods.isEmpty().not()) {

                    mBinding.tvLocation.text = ""

                    val placeLikelihood = response.placeLikelihoods[0]

                    mBinding.tvLocation.text = placeLikelihood.place.address
                    mBinding.tvYouAreIn.visibility = View.VISIBLE

                    mLatLng = placeLikelihood.place.latLng!!
                    populateHomeData(
                        placeLikelihood.place.latLng!!.latitude.toString(),
                        placeLikelihood.place.latLng!!.longitude.toString()
                    )
                }

            }.addOnFailureListener { exception ->
                if (exception is ApiException) {
                    Timber.e("Place not found: %s", exception.statusCode)
                    showLoadingIndicator(false)
                    locationDisableViewVisibility(true)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun displayMessage(message: String) {
        mBinding.llRootView.snack(message)
    }
}