package com.app.atoz.ui.user.postrequest

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.view.MotionEvent
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.app.atoz.AppApplication
import com.app.atoz.common.extentions.*
import com.app.atoz.common.helper.ImagePickerHelper
import com.app.atoz.common.helper.RxHelper
import com.app.atoz.databinding.ActivityPostRequestBinding
import com.app.atoz.models.CategoryModel
import com.app.atoz.models.PostRequestUploadImageModel
import com.app.atoz.models.address.AddressListItem
import com.app.atoz.shareddata.base.BaseActivity
import com.app.atoz.ui.user.address.AddAddressActivity
import com.app.atoz.ui.user.address.AddressPikerActivity
import com.app.atoz.ui.user.address.AddressPikerActivity.Companion.KEY_ADDRESS_PICKER
import com.app.atoz.ui.user.summary.SummaryActivity
import com.app.atoz.utils.Config
import com.app.atoz.utils.ValidationUtils
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class PostRequestActivity : BaseActivity(), View.OnTouchListener, ImagePickerHelper.ImagePickerListener,
    PostRequestUploadImageAdapter.OnItemClickListener {
    companion object {
        const val KEY_PARENT_CATEGORY_KEY = "ParentCategoryKey"
        const val KEY_SUBCATEGORY_KEY = "SubCategoryKey"
        const val KEY_ORDER_ITEM_LIST = "OrderItemLists"

        fun start(
            context: Context,
            parentCategoryId: String,
            subCategoryId: String,
            selectedItemsId: ArrayList<CategoryModel>
        ) {
            context.startActivity(
                Intent(context, PostRequestActivity::class.java)
                    .putExtra(KEY_PARENT_CATEGORY_KEY, parentCategoryId)
                    .putExtra(KEY_SUBCATEGORY_KEY, subCategoryId)
                    .putParcelableArrayListExtra(KEY_ORDER_ITEM_LIST, selectedItemsId)
            )
        }
    }

    private lateinit var mDatePickerDialogListener: DatePickerDialog.OnDateSetListener
    private val mSelectedDateCalendar: Calendar by lazy {
        Calendar.getInstance()
    }
    private var mSelectedTimeCalendar: Calendar? = null

    private lateinit var mImagePickerHelpers: ImagePickerHelper
    private val mViewModel: PostRequestViewModel by lazy {
        ViewModelProviders
            .of(this, viewModelFactory)[PostRequestViewModel::class.java]
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun getResource(): Int = com.app.atoz.R.layout.activity_post_request

    private lateinit var mBinding: ActivityPostRequestBinding
    private lateinit var mUploadedPicAdapter: PostRequestUploadImageAdapter

    override fun initView() {
        (application as AppApplication).mComponent.inject(this)
        mBinding = getBinding()
        setToolbar(mBinding.postRequestToolbar.toolbar, getString(com.app.atoz.R.string.text_post_request), true)
        initDataAndView()
        initObserver()
        initDatePicker()
    }

    private fun initDatePicker() {
        mDatePickerDialogListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            mSelectedDateCalendar.set(Calendar.YEAR, year)
            mSelectedDateCalendar.set(Calendar.MONTH, month)
            mSelectedDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            mBinding.etDate.setText(mSelectedDateCalendar.time.showDateFormat())
            mSelectedTimeCalendar = null
            mBinding.etTime.setText("")
        }
    }

    private fun initObserver() {
        mViewModel.mUploadedList.observe(this, Observer {
            if (::mUploadedPicAdapter.isInitialized) {
                mUploadedPicAdapter.notifyDataSetChanged()
            } else {
                mUploadedPicAdapter = PostRequestUploadImageAdapter(it, this)
                mBinding.rvUploadPic.adapter = mUploadedPicAdapter
            }
        })
        mViewModel.getPostOrderRequest().observe(this, Observer { it ->
            it?.let { requestState ->
                showLoadingIndicator(requestState.progress)
                requestState.data?.let { response ->
                    Timber.d("Successfully done")
                    response.data?.let {
                        SummaryActivity.start(
                            this@PostRequestActivity,
                            it.toString()
                        )
                    } ?: let {
                        response.message?.let {
                            displayMessage(it)
                        }
                    }
                }
                requestState.error?.let { errorObj ->
                    Timber.d("Successfully Error showing")
                    when (errorObj.errorState) {
                        Config.NETWORK_ERROR ->
                            displayMessage(getString(com.app.atoz.R.string.text_error_network))
                        Config.CUSTOM_ERROR ->
                            errorObj.customMessage
                                ?.let { displayMessage(it) }
                        else -> {
                        }
                    }
                }
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun initDataAndView() {
        mImagePickerHelpers = ImagePickerHelper(this@PostRequestActivity, this)
        mViewModel.setParentCategoryId(intent.getStringExtra(KEY_PARENT_CATEGORY_KEY))
        mViewModel.setSubCategoryId(intent.getStringExtra(KEY_SUBCATEGORY_KEY))
        mViewModel.setSelectedIdList(intent.getParcelableArrayListExtra(KEY_ORDER_ITEM_LIST))

        var selectedItems = ""
        for ((index, value) in mViewModel.mSelectedIdList.withIndex()) {
            selectedItems += if (index == mViewModel.mSelectedIdList.size - 1) {
                value.categoryName
            } else {
                "${value.categoryName}, "
            }
        }

        mBinding.tvSelectedOrderItems.text = "You have selected $selectedItems"
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        mImagePickerHelpers.onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mImagePickerHelpers.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            KEY_ADDRESS_PICKER -> {
                if (resultCode == Activity.RESULT_OK) {
                    val address: AddressListItem? = data?.getParcelableExtra(AddAddressActivity.ADDRESS_DATA)
                    address?.let {
                        mViewModel.setAddress(address)
                        setAddressValue()
                    }
                }
            }
        }
    }

    private fun setAddressValue() {
        mBinding.etAddress.setText(mViewModel.getAddress())
    }

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    override fun handleListener() {
        mBinding.etAddress.setOnTouchListener(this)

        mBinding.etNotes.setOnTouchListener(this)

        RxHelper.onClick(mBinding.btnSendRequest, mDisposable) {
            if (checkValidation()) {
                val selectedDate =
                    "${mSelectedDateCalendar.time.apiDateFormat()} ${mSelectedTimeCalendar?.time?.formatTimeIn24hr()
                        ?: ""}"

                mViewModel.mAddress?.let {
                    mViewModel.callPostOrderRequest(
                        isInternetConnected, this, mDisposable,
                        mBinding.etName.text.toString(),
                        it.id.toString(),
                        "${selectedDate.formatTimeInGMT()}",
                        mBinding.etNotes.text.toString(),
                        mViewModel.mParentCategoryId,
                        mViewModel.mSubCategoryId,
                        mViewModel.mSelectedIdList,
                        mViewModel.mUploadedList.value!!
                    )
                }
            }
        }

        RxHelper.onClick(mBinding.etAddress, mDisposable) {
            AddressPikerActivity.start(this)
        }

        RxHelper.onClick(mBinding.etDate, mDisposable) {
            hideKeyboard()
            mSelectedDateCalendar.apply {
                val birthDateDialog = DatePickerDialog(
                    this@PostRequestActivity,
                    mDatePickerDialogListener,
                    get(Calendar.YEAR),
                    get(Calendar.MONTH),
                    get(Calendar.DAY_OF_MONTH)
                )
                birthDateDialog.datePicker.minDate = Date().time
                birthDateDialog.show()
            }
        }

        RxHelper.onClick(mBinding.etTime, mDisposable) {
            hideKeyboard()
            val currentTime = mSelectedDateCalendar
            val hour = currentTime.get(Calendar.HOUR_OF_DAY)
            val minute = increaseMinuteIfSameDate(currentTime.get(Calendar.MINUTE))
            val mTimePicker = TimePickerDialog(
                this@PostRequestActivity,
                TimePickerDialog.OnTimeSetListener { _, selectedHour, selectedMinute ->
                    val calendar = Calendar.getInstance()
                    if (mSelectedDateCalendar.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH)) {
                        if (selectedHour < calendar.get(Calendar.HOUR_OF_DAY)
                            || (selectedHour == calendar.get(Calendar.HOUR_OF_DAY)
                                    && selectedMinute < calendar.get(Calendar.MINUTE))
                        ) {
                            mSelectedTimeCalendar = null
                            mBinding.etTime.setText("")
                        } else {
                            mSelectedTimeCalendar = Calendar.getInstance()
                            mSelectedTimeCalendar?.apply {
                                set(Calendar.HOUR_OF_DAY, selectedHour)
                                set(Calendar.MINUTE, selectedMinute)
                                mBinding.etTime.setText(time.formatTime12hr())
                            }
                        }
                    } else {
                        mSelectedTimeCalendar = Calendar.getInstance()
                        mSelectedTimeCalendar?.apply {
                            set(Calendar.HOUR_OF_DAY, selectedHour)
                            set(Calendar.MINUTE, selectedMinute)
                            mBinding.etTime.setText(time.formatTime12hr())
                        }
                    }
                }, hour, minute, false
            )
            mTimePicker.setTitle("Select Time")
            mTimePicker.show()
        }
    }

    private fun increaseMinuteIfSameDate(minute: Int): Int {
        val date = Calendar.getInstance().time
        val df = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val formattedDate = df.format(date)
        if (mBinding.etDate.text.toString().isEmpty().not()) {
            if (mBinding.etDate.text.toString() == formattedDate) {
                return minute + 1
            }
        }
        return minute
    }

    private fun checkValidation(): Boolean {
        var isValid = true
        if (!ValidationUtils.isNotEmpty(mBinding.etName.text.toString())) {
            mBinding.tilName.isErrorEnabled = true
            mBinding.tilName.error = getString(com.app.atoz.R.string.text_error_empty_name)
            isValid = false
        } else {
            mBinding.tilName.isErrorEnabled = false
        }

        if (mViewModel.mAddress == null) {
            mBinding.tilAddress.isErrorEnabled = true
            mBinding.tilAddress.error = getString(com.app.atoz.R.string.text_error_empty_address)
            isValid = false
        } else {
            mBinding.tilAddress.isErrorEnabled = false
        }

        if (!ValidationUtils.isNotEmpty(mBinding.etDate.text.toString())) {
            mBinding.tilDate.isErrorEnabled = true
            mBinding.tilDate.error = getString(com.app.atoz.R.string.text_error_empty_date)
            isValid = false
        } else {
            mBinding.tilDate.isErrorEnabled = false
        }

        if (!ValidationUtils.isNotEmpty(mBinding.etTime.text.toString())) {
            mBinding.tilTime.isErrorEnabled = true
            mBinding.tilTime.error = getString(com.app.atoz.R.string.text_error_empty_time)
            isValid = false
        } else {
            mBinding.tilTime.isErrorEnabled = false
        }

        if (!ValidationUtils.isNotEmpty(mBinding.etNotes.text.toString())) {
            mBinding.tilNote.isErrorEnabled = true
            mBinding.tilNote.error = getString(com.app.atoz.R.string.text_error_empty_note)
            isValid = false
        } else {
            mBinding.tilNote.isErrorEnabled = false
        }

        return isValid
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (event != null) {
            if (v?.id == com.app.atoz.R.id.etAddress || v?.id == com.app.atoz.R.id.etNotes) {
                v.parent.requestDisallowInterceptTouchEvent(true)
                when (event.action and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_UP -> v.parent.requestDisallowInterceptTouchEvent(false)
                }
            }
        }
        return false
    }

    override fun displayMessage(message: String) {
        mBinding.root.snack(message)
    }

    override fun onImageLoad() {
        mImagePickerHelpers.mFile?.let {
            mViewModel.addUploadedFile(it)
        }
    }

    override fun showLoadingIndicator(isShow: Boolean) {
        mBinding.progressBar.visibility = if (isShow) View.VISIBLE else View.GONE
    }

    override fun getDisposable(): CompositeDisposable = mDisposable

    override fun onRemove(position: Int) {
        mViewModel.mUploadedList.value?.removeAt(position)
        mUploadedPicAdapter.notifyItemRemoved(position)
        val uploadedList = mViewModel.mUploadedList.value
        uploadedList?.let {
            if (uploadedList[uploadedList.size - 1].itemType != PostRequestUploadImageAdapter.TYPE_UPLOAD_PIC) {
                uploadedList.add(PostRequestUploadImageModel(null, PostRequestUploadImageAdapter.TYPE_UPLOAD_PIC))
                mUploadedPicAdapter.notifyItemRemoved(uploadedList.size - 1)
            }
        }
    }

    override fun onAddItem() {
        mImagePickerHelpers.selectOptionToLoadImage()
    }

}
