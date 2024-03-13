package com.app.atoz.ui.user.subcategory

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.app.atoz.AppApplication
import com.app.atoz.R
import com.app.atoz.common.extentions.snack
import com.app.atoz.common.helper.RxHelper
import com.app.atoz.databinding.ActivitySubcategoryBinding
import com.app.atoz.shareddata.base.BaseActivity
import com.app.atoz.ui.user.category.CategoryActivity
import com.app.atoz.ui.user.postrequest.PostRequestActivity
import com.app.atoz.utils.Config
import com.app.atoz.utils.Config.Companion.KEY_TOOLBAR_TITLE_TEXT
import com.app.atoz.utils.DividerItemDecoration
import timber.log.Timber
import javax.inject.Inject


class SubcategoryActivity : BaseActivity() {

    companion object {
        const val KEY_PARENT_CATEGORY_ID = "ParentCategoryId"
        const val KEY_SUBCATEGORY_ID = "SubCategoryId"
        private const val KEY_LATITUDE = "Latitude"
        private const val KEY_LONGITUDE = "Longitude"
        fun start(
            context: Context,
            parentCategoryId: String,
            subCategoryId: String,
            title: String?,
            latitude: String?,
            longitude: String?
        ) {
            val subCategoryIntent = Intent(context, SubcategoryActivity::class.java)
                .putExtra(KEY_PARENT_CATEGORY_ID, parentCategoryId)
                .putExtra(KEY_SUBCATEGORY_ID, subCategoryId)
            title?.let {
                subCategoryIntent.putExtra(KEY_TOOLBAR_TITLE_TEXT, title)
            }
            if (latitude != null && longitude != null) {
                subCategoryIntent.putExtra(KEY_LATITUDE, latitude)
                subCategoryIntent.putExtra(KEY_LONGITUDE, longitude)

            }
            context.startActivity(subCategoryIntent)
        }
    }

    override fun getResource(): Int = R.layout.activity_subcategory

    private lateinit var mBinding: ActivitySubcategoryBinding

    private val mViewModel: SubcategoryViewModel by lazy {
        ViewModelProviders
            .of(this, viewModelFactory)[SubcategoryViewModel::class.java]
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private var mAdapter: SubcategoryAdapter? = null

    override fun initView() {
        (application as AppApplication).mComponent.inject(this)
        mBinding = getBinding()
        setToolbar(
            mBinding.subcategoryToolbar.toolbar,
            intent.getStringExtra(KEY_TOOLBAR_TITLE_TEXT),
            true
        )
        mViewModel.setParentCategoryId(intent.getStringExtra(KEY_PARENT_CATEGORY_ID))
        mViewModel.setSubCategoryId(intent.getStringExtra(KEY_SUBCATEGORY_ID))
        mViewModel.setLatLong(
            intent.getStringExtra(CategoryActivity.KEY_LATITUDE),
            intent.getStringExtra(CategoryActivity.KEY_LONGITUDE)
        )
        initObserver()
        mViewModel.getChildSubCategoryList(isInternetConnected, this, mDisposable)
    }

    override fun handleListener() {
        RxHelper.onClick(mBinding.btnNext, mDisposable) {
            mAdapter?.let {
                if (mViewModel.checkSelected(it.mDataList)) {
                    if (mViewModel.getParentCategoryId() != null && mViewModel.getSubCategoryId() != null)
                        PostRequestActivity.start(
                            this@SubcategoryActivity,
                            mViewModel.getParentCategoryId()!!,
                            mViewModel.getSubCategoryId()!!,
                            mViewModel.getSelectedItemsId()
                        )
                } else {
                    displayMessage(getString(R.string.text_error_select_subcategory_item))
                }
            }
        }
    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.menu_subcategory, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
//        return when (item?.itemId) {
//            R.id.menuSearch -> {
//                true
//            }
//            else -> {
//                super.onOptionsItemSelected(item)
//            }
//        }
//    }

    private fun initObserver() {
        mViewModel.getChildSubCategoryRequest().observe(this, Observer { requestState ->
            showLoadingIndicator(requestState.progress)
            requestState.data?.let { mainResponse ->
                Timber.d("Data rendered ")
                mainResponse.data?.let {
                    if (it.service.size > 0) {
                        showEmptyView(false)
                        mAdapter = SubcategoryAdapter(it.service)
                        mBinding.rvSubCategory.adapter = mAdapter
                        mBinding.rvSubCategory
                            .addItemDecoration(DividerItemDecoration(this, R.drawable.item_divider))

                    } else {
                        showEmptyView(true)
                    }

                }
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

    private fun showEmptyView(isShow: Boolean) {
        mBinding.tvErrorEmpty.visibility = if (isShow) View.VISIBLE else View.GONE
        mBinding.tvNoteDesc.visibility = if (isShow) View.GONE else View.VISIBLE
        mBinding.rvSubCategory.visibility = if (isShow) View.GONE else View.VISIBLE
        mBinding.btnNext.visibility = if (isShow) View.GONE else View.VISIBLE
    }

    override fun displayMessage(message: String) {
        mBinding.root.snack(message)
    }

    private fun showLoadingIndicator(isShow: Boolean) {
        mBinding.progressBar.visibility = if (isShow) View.VISIBLE else View.GONE
    }
}