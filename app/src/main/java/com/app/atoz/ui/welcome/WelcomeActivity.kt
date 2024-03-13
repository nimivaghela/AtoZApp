package com.app.atoz.ui.welcome

import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.Html
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.app.atoz.R
import com.app.atoz.common.helper.RxHelper
import com.app.atoz.databinding.ActivityWelcomeBinding
import com.app.atoz.shareddata.base.BaseActivity
import com.app.atoz.ui.auth.signin.SignInActivity
import com.app.atoz.ui.selectaccount.SelectAccountActivity
import com.app.atoz.utils.MyViewPagerAdapter

class WelcomeActivity : BaseActivity() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, WelcomeActivity::class.java))
        }
    }

    override fun getResource(): Int = R.layout.activity_welcome

    private lateinit var mBinding: ActivityWelcomeBinding
    private var mViewPagerAdapter: MyViewPagerAdapter? = null
    private lateinit var dots: Array<TextView?>

    override fun initView() {
        mBinding = getBinding()
        initViewPager()
    }


    override fun handleListener() {


        RxHelper.onClick(mBinding.btnSignIn, mDisposable) {
            SignInActivity.start(this@WelcomeActivity)
        }

        RxHelper.onClick(mBinding.tvCreateYourAccount, mDisposable) {
            SelectAccountActivity.start(this@WelcomeActivity)
        }

        mBinding.vpWelcome.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
                // no need to implement
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                // no need to implement
            }

            override fun onPageSelected(position: Int) {
                showPagerDots(position)
            }
        })

    }

    private fun showPagerDots(currentPage: Int) {
        dots = arrayOfNulls(3)

        mBinding.layoutDots.removeAllViews()
        for (i in 0 until dots.size) {
            dots[i] = TextView(this)
            @Suppress("DEPRECATION")
            dots[i]?.text = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ->
                    Html.fromHtml("&#8226;", Html.FROM_HTML_MODE_LEGACY)
                else -> Html.fromHtml("&#8226;")
            }
            dots[i]?.textSize = if (currentPage == i) 45f else 30f
            dots[i]?.setTextColor(ContextCompat.getColor(this, R.color.dot_inactive_color))
            mBinding.layoutDots.addView(dots[i])
        }

        if (dots.isNotEmpty()) {
            dots[currentPage]?.setTextColor(ContextCompat.getColor(this, R.color.dot_active_color))
        }
    }

    private fun initViewPager() {
        showPagerDots(0)
        mViewPagerAdapter = MyViewPagerAdapter(supportFragmentManager)
        mViewPagerAdapter?.apply {
            addFragments(WelcomeFragment.newInstance(pageIndex = 1))
            addFragments(WelcomeFragment.newInstance(pageIndex = 2))
            addFragments(WelcomeFragment.newInstance(pageIndex = 3))
        }
        mBinding.vpWelcome.adapter = mViewPagerAdapter
    }

    override fun displayMessage(message: String) {

    }
}