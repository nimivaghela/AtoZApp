package com.app.atoz.ui.user.subcategory

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.atoz.models.CategoryModel
import com.app.atoz.models.CategorySelectionResponseModel
import com.app.atoz.models.RequestState
import com.app.atoz.shareddata.base.BaseView
import com.app.atoz.shareddata.repo.CategorySubcategoryRepo
import com.google.gson.JsonObject
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class SubcategoryViewModel @Inject constructor(private val mCategorySubcategoryRepo: CategorySubcategoryRepo) :
    ViewModel() {

    var latitude: String = ""
    var longitude: String = ""
    private lateinit var mParentCategoryId: String
    private var mSubCategoryId: String? = null
    private val mSelectedIdList: ArrayList<CategoryModel> = ArrayList()

    fun setLatLong(latitude: String?, longitude: String?) {
        this.latitude = latitude!!
        this.longitude = longitude!!
    }

    private val mLDChildSubCategoryRequest = MutableLiveData<RequestState<CategorySelectionResponseModel>>()

    fun getChildSubCategoryRequest(): LiveData<RequestState<CategorySelectionResponseModel>> =
        mLDChildSubCategoryRequest

    fun setParentCategoryId(parentCategoryId: String?) {
        mParentCategoryId = parentCategoryId!!
    }

    fun setSubCategoryId(subCategoryId: String?) {
        mSubCategoryId = subCategoryId!!
    }

    fun getParentCategoryId(): String? = mParentCategoryId

    fun getSubCategoryId(): String? = mSubCategoryId

    fun getSelectedItemsId() = mSelectedIdList

    fun getChildSubCategoryList(
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable
    ) {
        if (mSubCategoryId != null) {
            val body = JsonObject()
            body.addProperty("parent_id", mSubCategoryId)
            body.addProperty("latitude", latitude)
            body.addProperty("longitude", longitude)
            mCategorySubcategoryRepo.getSubcategoryChildCategoryList(
                body,
                isInternetConnected,
                baseView,
                disposable,
                mLDChildSubCategoryRequest
            )
        }
    }

    fun checkSelected(mDataList: ArrayList<CategoryModel>): Boolean {
        mSelectedIdList.clear()
        for (categoryModel in mDataList) {
            if (categoryModel.isChecked) {
                mSelectedIdList.add(categoryModel)
            }
        }
        return mSelectedIdList.size > 0
    }
}