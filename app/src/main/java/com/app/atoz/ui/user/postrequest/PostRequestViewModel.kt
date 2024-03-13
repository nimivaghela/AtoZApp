package com.app.atoz.ui.user.postrequest

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.atoz.models.CategoryModel
import com.app.atoz.models.PostRequestUploadImageModel
import com.app.atoz.models.RequestState
import com.app.atoz.models.UserHolder
import com.app.atoz.models.address.AddressListItem
import com.app.atoz.shareddata.base.BaseView
import com.app.atoz.shareddata.repo.PostOrderRequestRepo
import com.google.gson.JsonObject
import io.reactivex.disposables.CompositeDisposable
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

class PostRequestViewModel @Inject constructor(
    private val mPostOrderRequestRepo: PostOrderRequestRepo,
    private val mUserHolder: UserHolder
) : ViewModel() {
    lateinit var mParentCategoryId: String
    lateinit var mSubCategoryId: String
    lateinit var mSelectedIdList: ArrayList<CategoryModel>
    var mAddress: AddressListItem? = null

    val mUploadedList = MutableLiveData<ArrayList<PostRequestUploadImageModel>>()

    init {
        val list = ArrayList<PostRequestUploadImageModel>()
        list.add(PostRequestUploadImageModel(null, PostRequestUploadImageAdapter.TYPE_UPLOAD_PIC))
        mUploadedList.value = list
    }

    fun addUploadedFile(file: File) {
        val list = mUploadedList.value
        list?.add(
            0,
            PostRequestUploadImageModel(file, PostRequestUploadImageAdapter.TYPE_UPLOADED_VIEW)
        )
        if (list?.size == 6) {
            list.removeAt(5)
        }
        mUploadedList.postValue(list)
    }

    private val mLDPostOrderRequest = MutableLiveData<RequestState<Int>>()

    fun getPostOrderRequest(): LiveData<RequestState<Int>> = mLDPostOrderRequest

    fun setParentCategoryId(parentCategoryId: String?) {
        mParentCategoryId = parentCategoryId!!
    }

    fun setSubCategoryId(subCategoryId: String?) {
        mSubCategoryId = subCategoryId!!
    }

    fun setSelectedIdList(selectedList: ArrayList<CategoryModel>?) {
        mSelectedIdList = selectedList!!
    }

    fun setAddress(addressItem: AddressListItem) {
        mAddress = addressItem
    }

    fun getAddress() = "${mAddress?.address} \n\nLocation : ${mAddress?.location}"

    fun callPostOrderRequest(
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable,
        name: String,
        addressId: String,
        dateTime: String,
        note: String,
        categoryId: String,
        subCategoryId: String,
        serviceIdList: ArrayList<CategoryModel>,
        uploadedFilesList: ArrayList<PostRequestUploadImageModel>
    ) {

        val multipartBody = MultipartBody.Builder()
        multipartBody.setType(MultipartBody.FORM)
        multipartBody.addFormDataPart("name", name)
        multipartBody.addFormDataPart("address_id", addressId)
        multipartBody.addFormDataPart("date_time", dateTime)
        multipartBody.addFormDataPart("note", note)
        multipartBody.addFormDataPart("category_id", categoryId)
        multipartBody.addFormDataPart("sub_category_id", subCategoryId)
        multipartBody.addFormDataPart("user_id", mUserHolder.userId!!)
        for ((index, service) in serviceIdList.withIndex()) {
            val serviceJson = JsonObject()
            serviceJson.addProperty("category_id", service.categoryId.toString())
            serviceJson.addProperty("location_id", service.locationId)
            serviceJson.addProperty("price", service.price)
            multipartBody.addFormDataPart("services[$index]", serviceJson.toString())
        }

        for ((index, fileObj) in uploadedFilesList.withIndex()) {
            fileObj.file?.let {
                multipartBody.addFormDataPart(
                    "medias[$index]", it.name,
                    it.asRequestBody("image/*".toMediaTypeOrNull())
                )
            }
        }

        mPostOrderRequestRepo.postOrderRequest(
            multipartBody.build(),
            isInternetConnected,
            baseView,
            disposable,
            mLDPostOrderRequest
        )
    }
}