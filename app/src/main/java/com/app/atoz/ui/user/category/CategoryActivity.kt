package com.app.atoz.ui.user.category

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.app.atoz.AppApplication
import com.app.atoz.R
import com.app.atoz.common.extentions.loadImage
import com.app.atoz.common.extentions.snack
import com.app.atoz.databinding.ActivityCategoryBinding
import com.app.atoz.models.CategoryModel
import com.app.atoz.shareddata.base.BaseActivity
import com.app.atoz.ui.user.subcategory.SubcategoryActivity
import com.app.atoz.utils.Config
import com.app.atoz.utils.DividerItemDecoration
import timber.log.Timber
import javax.inject.Inject

class CategoryActivity : BaseActivity() {

    companion object {
        private const val KEY_CATEGORY_ID = "KeyCategoryId"
        private const val KEY_CATEGORY_NAME = "KeyCategoryName"
        const val KEY_LATITUDE = "Latitude"
        const val KEY_LONGITUDE = "Longitude"

        fun start(
            context: Context,
            categoryId: String,
            categoryName: String,
            latitude: String?,
            longitude: String?
        ) {
            val categoryIntent = Intent(context, CategoryActivity::class.java)
                .putExtra(KEY_CATEGORY_ID, categoryId)
                .putExtra(KEY_CATEGORY_NAME, categoryName)

            if (latitude != null && longitude != null) {
                categoryIntent.putExtra(KEY_LATITUDE, latitude)
                categoryIntent.putExtra(KEY_LONGITUDE, longitude)
            }
            context.startActivity(categoryIntent)
        }
    }

    override fun getResource(): Int = R.layout.activity_category

    private lateinit var mBinding: ActivityCategoryBinding
    private val mViewModel: CategoryViewModel by lazy {
        ViewModelProviders
            .of(this, viewModelFactory)[CategoryViewModel::class.java]
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory


    override fun initView() {
        (application as AppApplication).mComponent.inject(this)
        mBinding = getBinding()
        setToolbar(
            mBinding.categoryToolbar.toolbar,
            intent.getStringExtra(KEY_CATEGORY_NAME)!!,
            true,
            R.color.transparent
        )
        mViewModel.setParentCategoryId(intent.getStringExtra(KEY_CATEGORY_ID)!!)
        if (intent.hasExtra(KEY_LATITUDE) && intent.hasExtra(KEY_LONGITUDE)) {
            mViewModel.setLatLong(
                intent.getStringExtra(KEY_LATITUDE)!!,
                intent.getStringExtra(KEY_LONGITUDE)!!
            )
        }
        initObserver()
        mViewModel.getSubCategoryList(isInternetConnected, this, mDisposable)
    }

    private fun initObserver() {
        mViewModel.getSubCategoryRequest().observe(this, Observer { requestState ->
            showLoadingIndicator(requestState.progress)
            requestState.data?.let { data ->
                Timber.d("Data rendered ")
                data.data?.let { response ->
                    if (response.service.size > 1) {
                        mBinding.expandedImage.loadImage(response.service[0].categoryImage)
                    }
                    mBinding.rvCategory.adapter = CategoryAdapter(response.service,
                        object : CategoryAdapter.CategoryClickListener {
                            override fun onClick(data: CategoryModel) {
                                mViewModel.getParentCategoryId()?.let {
                                    SubcategoryActivity.start(
                                        this@CategoryActivity,
                                        it,
                                        data.categoryId.toString(),
                                        data.categoryName,
                                        mViewModel.latitude,
                                        mViewModel.longitude
                                    )
                                }
                            }
                        })
                    mBinding.rvCategory.addItemDecoration(
                        DividerItemDecoration(
                            this,
                            R.drawable.item_divider
                        )
                    )
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

    override fun handleListener() {

    }

    override fun displayMessage(message: String) {
        mBinding.root.snack(message)
    }

    private fun showLoadingIndicator(isShow: Boolean) {
        mBinding.progressBar.visibility = if (isShow) View.VISIBLE else View.GONE
    }
}