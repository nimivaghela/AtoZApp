package com.app.atoz.ui.viewrating

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.app.atoz.AppApplication
import com.app.atoz.R
import com.app.atoz.common.extentions.snack
import com.app.atoz.databinding.ActivityViewRatingBinding
import com.app.atoz.models.UserRatingModel
import com.app.atoz.shareddata.base.BaseActivity
import com.app.atoz.utils.Config
import timber.log.Timber
import javax.inject.Inject

class ViewRatingActivity : BaseActivity() {
    companion object {
        private const val KEY_USER_ID = "KeyUserId"
        fun start(context: Context, userId: String) {
            context.startActivity(
                Intent(context, ViewRatingActivity::class.java)
                    .putExtra(KEY_USER_ID, userId)
            )
        }
    }

    override fun getResource(): Int = R.layout.activity_view_rating

    private lateinit var mBinding: ActivityViewRatingBinding

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory

    private val mViewModel: ViewRatingViewModel by lazy {
        ViewModelProviders.of(this, mViewModelFactory)[ViewRatingViewModel::class.java]
    }

    override fun initView() {
        (application as AppApplication).mComponent.inject(this)
        mBinding = getBinding()
        setToolbar(mBinding.toolbarViewRating.toolbar, getString(R.string.text_title_rating), true)
        initObserver()
        mViewModel.getUserProviderPersonalRatingsComments(
            intent.getStringExtra(KEY_USER_ID),
            isInternetConnected, this, mDisposable
        )
    }

    private fun initObserver() {
        mViewModel.getPersonalRatingObserver().observe(this,
            Observer { requestState ->
                showLoadingIndicator(requestState.progress)
                requestState.data?.let { mainResponse ->
                    Timber.d("Data rendered ")
                    showErrorView(false)
                    mainResponse.data?.let {
                        bindDataInView(it)
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

    private fun bindDataInView(dataList: ArrayList<UserRatingModel>) {
        if (dataList.size == 0) {
            showErrorView(true)
        } else {
            showErrorView(false)
            mBinding.rvViewRatings.adapter = ViewRatingAdapter(dataList)
        }
    }

    private fun showErrorView(isShow: Boolean) {
        mBinding.tvEmptyView.visibility = if (isShow) View.VISIBLE else View.GONE
        mBinding.rvViewRatings.visibility = if (isShow) View.GONE else View.VISIBLE
    }

    private fun showLoadingIndicator(isShow: Boolean) {
        mBinding.progressBar.visibility = if (isShow) View.VISIBLE else View.GONE
    }

    override fun handleListener() {

    }

    override fun displayMessage(message: String) {
        mBinding.root.snack(message)
    }

}