package com.app.atoz.ui.provider.addservice

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.atoz.models.CategoryModel
import com.app.atoz.models.CategorySelectionResponseModel
import com.app.atoz.models.RequestState
import com.app.atoz.models.UserHolder
import com.app.atoz.shareddata.base.BaseView
import com.app.atoz.shareddata.repo.ExistServiceRepo
import com.google.gson.Gson
import io.reactivex.disposables.CompositeDisposable
import okhttp3.MultipartBody
import javax.inject.Inject

class AddServiceViewModel @Inject constructor(
    private val mExistServiceRepo: ExistServiceRepo,
    private val mUserHolder: UserHolder
) : ViewModel() {

    private val mLDAddService = MutableLiveData<RequestState<Any>>()

    var mCategoryResponseModel: CategorySelectionResponseModel? = null
    var mNonSelectedServiceList = MutableLiveData<ArrayList<CategoryModel>>()
    var mSelectedServicesId = ArrayList<Int>()

    fun getAddServiceObserver() = mLDAddService

    fun setCategoryResponseModel(data: String?) {
        mCategoryResponseModel = Gson().fromJson(data, CategorySelectionResponseModel::class.java)
        mCategoryResponseModel?.let {
            val allServiceList = ArrayList<CategoryModel>()
            allServiceList.addAll(it.service)

            val existServiceList = it.providerExistService
            val nonSelectedServiceList = ArrayList<CategoryModel>()

            for ((index, allService) in allServiceList.withIndex()) {
                allService.apply {
                    val categoryModel = CategoryModel(
                        categoryId, categoryName, parentCategoryId, categoryDescription,
                        categoryImage, categoryOriginalPhoto, ArrayList(),
                        false, price, false
                    )
                    nonSelectedServiceList.add(index, categoryModel)
                }
            }

            for ((index, allService) in allServiceList.withIndex()) {
                allService.serviceList?.let { listService ->
                    for (allServiceChild in listService) {
                        var isExist = false
                        existServiceList?.get(index)?.serviceList?.let { listExistService ->
                            loop@ for (existServiceChild in listExistService) {
                                if (allServiceChild.serviceId == existServiceChild.serviceId) {
                                    isExist = true
                                    break@loop
                                }
                            }
                        }
                        if (!isExist) {
                            nonSelectedServiceList[index].serviceList?.add(allServiceChild)
                        }
                    }
                }
            }
            mNonSelectedServiceList.value = nonSelectedServiceList
        }
    }

    fun checkSelectedItems(): Boolean {
        mSelectedServicesId.clear()

        mNonSelectedServiceList.value?.let { list ->
            list.filter { !it.serviceList.isNullOrEmpty() }
                .flatMap { it.serviceList!! }
                .filter { it.selected }
                .forEach { mSelectedServicesId.add(it.serviceId) }
        }
        return mSelectedServicesId.size > 0
    }

    fun callAddService(isInternetConnected: Boolean, baseView: BaseView, disposable: CompositeDisposable) {
        val multipartBody = MultipartBody.Builder()
        multipartBody.setType(MultipartBody.FORM)
        multipartBody.addFormDataPart("id", mUserHolder.userId!!)


        for ((index, newId) in mSelectedServicesId.withIndex())
            multipartBody.addFormDataPart("new_service_ids[$index]", newId.toString())

        mExistServiceRepo.addDeleteProviderServices(
            multipartBody.build(),
            isInternetConnected,
            baseView,
            disposable,
            mLDAddService
        )
    }
}