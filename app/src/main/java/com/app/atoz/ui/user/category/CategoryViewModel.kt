package com.app.atoz.ui.user.category

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.atoz.models.CategorySelectionResponseModel
import com.app.atoz.models.RequestState
import com.app.atoz.shareddata.base.BaseView
import com.app.atoz.shareddata.repo.CategorySubcategoryRepo
import com.google.gson.JsonObject
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class CategoryViewModel @Inject constructor(private val mCategorySubcategoryRepo: CategorySubcategoryRepo) :
    ViewModel() {
    var latitude: String = ""
    var longitude: String = ""

    private var mParentCategoryId: String? = null

    private val mLDCategoryRequest = MutableLiveData<RequestState<CategorySelectionResponseModel>>()

    fun getSubCategoryRequest(): LiveData<RequestState<CategorySelectionResponseModel>> = mLDCategoryRequest

    fun setLatLong(latitude: String, longitude: String) {
        this.latitude = latitude
        this.longitude = longitude
    }

    fun getSubCategoryList(
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable
    ) {
        mParentCategoryId?.let {
            val body = JsonObject()
            body.addProperty("parent_id", it)
            body.addProperty("latitude", latitude)
            body.addProperty("longitude", longitude)

            mCategorySubcategoryRepo.getSubcategoryChildCategoryList(
                body,
                isInternetConnected,
                baseView,
                disposable,
                mLDCategoryRequest
            )
        }
    }

    fun getParentCategoryId() = mParentCategoryId

    fun setParentCategoryId(categoryId: String) {
        mParentCategoryId = categoryId
    }
}