package com.app.atoz.ui.user.subscription

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.Html
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.app.atoz.AppApplication
import com.app.atoz.R
import com.app.atoz.common.extentions.snack
import com.app.atoz.databinding.ActivitySubscriptionBinding
import com.app.atoz.models.CurrentPlanModel
import com.app.atoz.models.SubscriptionModel
import com.app.atoz.services.ActiveSubscriptionService
import com.app.atoz.shareddata.base.BaseActivity
import com.app.atoz.ui.user.subscriptionpayment.SubscriptionPaymentActivity.Companion.SUBSCRIPTION_PAYMENT_ACTIVITY
import com.app.atoz.utils.Config
import timber.log.Timber
import javax.inject.Inject


class SubscriptionActivity : BaseActivity() {

    companion object {

        fun start(context: Context) {
            val starter = Intent(context, SubscriptionActivity::class.java)
            context.startActivity(starter)
        }
    }

    override fun getResource(): Int = R.layout.activity_subscription

    private val mViewModel: SubscriptionViewModel by lazy {
        ViewModelProviders.of(this, mViewModelFactory)[SubscriptionViewModel::class.java]
    }

    private lateinit var mDots: Array<TextView?>
    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory

    private lateinit var mBinding: ActivitySubscriptionBinding

    override fun initView() {
        (application as AppApplication).mComponent.inject(this)
        mBinding = getBinding()
        setToolbar(mBinding.includeToolbar.toolbar, getString(R.string.subscription), true)

        mBinding.viewPagerPlan.clipToPadding = false
        mBinding.viewPagerPlan.setPadding(124, 0, 124, 0)
        mBinding.viewPagerPlan.pageMargin = 20

        initObserver()

        callSubscriptionList()
    }

    private fun callSubscriptionList() {
        mViewModel.getSubscriptionList(isInternetConnected, this, mDisposable)
        ActiveSubscriptionService.startService(this@SubscriptionActivity)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            SUBSCRIPTION_PAYMENT_ACTIVITY -> {
                if (resultCode == Activity.RESULT_OK) {
                    callSubscriptionList()
                }
            }
        }
    }

    private fun initObserver() {
        mViewModel.getSubscriptionListRequest()
            .observe(this, Observer { requestState ->
                showLoadingIndicator(requestState.progress)
                requestState.data?.let {
                    it.data?.subscriptionList?.apply {
                        mBinding.tvChoosePlan.visibility =
                            if (it.data!!.currentPlanModel != null) View.INVISIBLE else View.VISIBLE
                        initViewPager(this, it.data!!.currentPlanModel)
                        showPagerDots(mBinding.layoutDots, 0)
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

    private fun showLoadingIndicator(isShow: Boolean) {
        mBinding.progressBar.visibility = if (isShow) View.VISIBLE else View.GONE
    }

    override fun handleListener() {

        mBinding.viewPagerPlan.setPageTransformer(false, CustomPagerTransformer(this))

        mBinding.viewPagerPlan.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
                // no need to implement
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                // no need to implement
            }

            override fun onPageSelected(position: Int) {
                showPagerDots(mBinding.layoutDots, position)
            }
        })
    }

    private fun initViewPager(
        subscriptionList: ArrayList<SubscriptionModel>,
        currentPlanModel: CurrentPlanModel?
    ) {
        val pagerAdapter = SubscriptionPagerAdapter(supportFragmentManager, subscriptionList, currentPlanModel)
        mBinding.viewPagerPlan.adapter = pagerAdapter
    }

    private inner class SubscriptionPagerAdapter(
        fm: FragmentManager,
        val mSubscriptionList: ArrayList<SubscriptionModel>,
        val mCurrentPlanModel: CurrentPlanModel?
    ) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getCount(): Int = mSubscriptionList.size + (if (mCurrentPlanModel != null) 1 else 0)

        override fun getItem(position: Int): Fragment =
            if (mCurrentPlanModel != null && position == 0) {
                ActiveSubscriptionPlanFragment.newInstance(mCurrentPlanModel)
            } else {
                Timber.d("Position of the fragment $position")
                SubscriptionPlanFragment.newInstance(
                    mSubscriptionList[position - (if (mCurrentPlanModel != null) 1 else 0)],
                    mCurrentPlanModel != null
                )
            }
    }


    private inner class CustomPagerTransformer(context: Context) : ViewPager.PageTransformer {

        private val maxTranslateOffsetX: Int
        private var viewPager: ViewPager? = null

        init {
            this.maxTranslateOffsetX = dp2px(context, 180f)
        }

        override fun transformPage(view: View, position: Float) {
            if (viewPager == null) {
                viewPager = view.parent as ViewPager?
            }

            val leftInScreen = view.left - viewPager!!.scrollX
            val centerXInViewPager = leftInScreen + view.measuredWidth / 2
            val offsetX = centerXInViewPager - viewPager!!.measuredWidth / 2
            val offsetRate = offsetX.toFloat() * 0.38f / viewPager!!.measuredWidth
            val scaleFactor = 1 - Math.abs(offsetRate)
            if (scaleFactor > 0) {
                view.scaleX = scaleFactor
                view.scaleY = scaleFactor
                view.translationX = -maxTranslateOffsetX * offsetRate
            }
        }

        private fun dp2px(context: Context, dipValue: Float): Int {
            val m = context.resources.displayMetrics.density
            return (dipValue * m + 0.5f).toInt()
        }
    }


    private fun showPagerDots(layoutDots: LinearLayout, currentPage: Int) {
        val responseData = mViewModel.getSubscriptionListRequest().value?.data?.data
        val subscriptionSize = responseData?.subscriptionList?.size ?: 0
        mDots =
            arrayOfNulls(
                (subscriptionSize + if (responseData?.currentPlanModel != null) 1 else 0)
            )

        layoutDots.removeAllViews()
        for (i in 0 until mDots.size) {
            mDots[i] = TextView(this)
            @Suppress("DEPRECATION")
            mDots[i]?.text = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ->
                    Html.fromHtml("&#8226;", Html.FROM_HTML_MODE_LEGACY)
                else -> Html.fromHtml("&#8226;")
            }
            mDots[i]?.textSize = if (currentPage == i) 45f else 30f
            mDots[i]?.setTextColor(ContextCompat.getColor(this, R.color.dot_active_color))
            layoutDots.addView(mDots[i])
        }

        if (mDots.isNotEmpty()) {
            mDots[currentPage]?.setTextColor(ContextCompat.getColor(this, R.color.dot_inactive_color))
        }
    }

    override fun displayMessage(message: String) {
        mBinding.root.snack(message)
    }
}
