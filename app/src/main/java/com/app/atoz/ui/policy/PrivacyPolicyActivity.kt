package com.app.atoz.ui.policy

import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.app.atoz.AppApplication
import com.app.atoz.R
import com.app.atoz.common.extentions.snack
import com.app.atoz.databinding.ActivityPricarcyPolicyBinding
import com.app.atoz.shareddata.base.BaseActivity
import com.app.atoz.utils.Config
import timber.log.Timber
import javax.inject.Inject


class PrivacyPolicyActivity : BaseActivity() {

    companion object {
        const val KEY_IS_PRIVACY_POLICY = "isPrivacyPolicy"
        fun start(context: Context, isPrivacyPolicy: Boolean) {
            val starter = Intent(context, PrivacyPolicyActivity::class.java)
            starter.putExtra(KEY_IS_PRIVACY_POLICY, isPrivacyPolicy)
            context.startActivity(starter)
        }
    }

    override fun getResource() = R.layout.activity_pricarcy_policy

    private lateinit var mBinding: ActivityPricarcyPolicyBinding
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val mViewModel: ContentPagesViewModel by lazy {
        ViewModelProviders
            .of(this, viewModelFactory)[ContentPagesViewModel::class.java]
    }
    private lateinit var mContentPageName: String

    override fun initView() {
        (application as AppApplication).mComponent.inject(this)
        mBinding = getBinding()
        val isPrivacyPolicy = intent.getBooleanExtra(KEY_IS_PRIVACY_POLICY, false)
        mContentPageName = if (isPrivacyPolicy) {
            "privacy-policy"
        } else {
            "terms-n-condition"
        }
        setToolbar(
            mBinding.includeToolbar.toolbar,
            getString(if (isPrivacyPolicy) R.string.privacy_policy else R.string.terms_of_use), true
        )
        populateData()
        mBinding.termsWeb.settings.allowFileAccess = true
        mBinding.termsWeb.settings.domStorageEnabled = true
        mBinding.termsWeb.requestFocus(View.FOCUS_DOWN)
        mBinding.termsWeb.settings.javaScriptEnabled = true

    }

    private fun loadDataToWebview(content: String) {
        mBinding.termsWeb.loadData(content, "text/html; charset=UTF-8", null)
    }

    private fun populateData() {

        mViewModel.getContentPageObserver().observe(this@PrivacyPolicyActivity, Observer { observer ->
            observer?.let { requestState ->
                showLoadingIndicator(requestState.progress)
                requestState.data?.run {
                    Timber.d("Successfully done")
                    loadDataToWebview(requestState.data.data!!)
//                    mBinding.policy = fromHtml(requestState.data.data).toString()
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
            }
        })
        mViewModel.getContentPage(mContentPageName, isInternetConnected, this, mDisposable)
    }

    private fun fromHtml(html: String?): Spanned {
        return when {
            html == null -> // return an empty spannable if the html is null
                SpannableString("")
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ->
                /**
                 * FROM_HTML_MODE_LEGACY is the behaviour that was used for versions below android N
                 * we are using this flag to give a consistent behaviour
                 */
                Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
            else -> Html.fromHtml(html)
        }
    }

    override fun handleListener() {

    }

    override fun displayMessage(message: String) {
        mBinding.llRootView.snack(message)
    }

    private fun showLoadingIndicator(isShow: Boolean) {
        mBinding.progressBar.visibility = if (isShow) View.VISIBLE else View.GONE
    }
}
