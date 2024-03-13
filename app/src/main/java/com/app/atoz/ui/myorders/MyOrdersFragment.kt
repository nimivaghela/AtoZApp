package com.app.atoz.ui.myorders

import android.content.Intent
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.viewpager.widget.ViewPager
import com.app.atoz.R
import com.app.atoz.common.extentions.snack
import com.app.atoz.databinding.FragmentMyOrdersBinding
import com.app.atoz.services.PaymentDueService
import com.app.atoz.shareddata.base.BaseFragment
import com.app.atoz.utils.MyViewPagerAdapter
import com.google.android.material.tabs.TabLayout

class MyOrdersFragment : BaseFragment() {
    companion object {
        fun newInstance(): MyOrdersFragment {
            return MyOrdersFragment()
        }
    }

    override fun getInflateResource(): Int = R.layout.fragment_my_orders

    private lateinit var mBinding: FragmentMyOrdersBinding
    private var mViewPagerAdapter: MyViewPagerAdapter? = null

    override fun initView() {
        mBinding = getBinding()
        setTitle(getString(R.string.my_orders))
    }

    override fun hasOptionMenu(): Boolean = true

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        inflater.inflate(R.menu.menu_my_orders, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menuSearch -> {
                true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    override fun postInit() {
        activity?.let {
            PaymentDueService.startService(it)
        }
        initTab()
        initViewPager()
    }

    override fun handleListener() {
        mBinding.tlMyOrders.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
                //no need to implement
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                //no need to implement
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                mBinding.vpMyOrders.currentItem = tab?.position ?: 0
            }
        })
        mBinding.vpMyOrders.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
                //no need to implement
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                //no need to implement
            }

            override fun onPageSelected(position: Int) {
                mBinding.tlMyOrders.getTabAt(position)?.select()
            }
        })
    }

    private fun initTab() {
        mBinding.tlMyOrders.addTab(mBinding.tlMyOrders.newTab().setText(R.string.text_tab_all))
        mBinding.tlMyOrders.addTab(mBinding.tlMyOrders.newTab().setText(R.string.text_tab_confirmed))
        mBinding.tlMyOrders.addTab(mBinding.tlMyOrders.newTab().setText(R.string.text_tab_cancelled))
        mBinding.tlMyOrders.addTab(mBinding.tlMyOrders.newTab().setText(R.string.text_tab_bill_uploaded))
        mBinding.tlMyOrders.addTab(mBinding.tlMyOrders.newTab().setText(R.string.text_tab_payment_done))
        mBinding.tlMyOrders.addTab(mBinding.tlMyOrders.newTab().setText(R.string.text_tab_completed))

        mBinding.tlMyOrders.getTabAt(0)?.select()
    }

    private fun initViewPager() {
        mViewPagerAdapter = MyViewPagerAdapter(childFragmentManager)
        mViewPagerAdapter?.apply {
            addFragments(MyOrdersChildFragment.newInstance(MyOrdersViewModel.TYPE_ORDER_ALL))
            addFragments(MyOrdersChildFragment.newInstance(MyOrdersViewModel.TYPE_ORDER_CONFIRMED))
            addFragments(MyOrdersChildFragment.newInstance(MyOrdersViewModel.TYPE_ORDER_CANCELLED))
            addFragments(MyOrdersChildFragment.newInstance(MyOrdersViewModel.TYPE_ORDER_BILL_UPLOADED))
            addFragments(MyOrdersChildFragment.newInstance(MyOrdersViewModel.TYPE_ORDER_PAYMENT_DONE))
            addFragments(MyOrdersChildFragment.newInstance(MyOrdersViewModel.TYPE_ORDER_COMPLETED))
        }
        mBinding.vpMyOrders.adapter = mViewPagerAdapter
    }

    override fun displayMessage(message: String) {
        mBinding.llRootView.snack(message)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (mViewPagerAdapter?.getRegisteredFragment(mBinding.vpMyOrders.currentItem) != null) {
            (mViewPagerAdapter
                ?.getRegisteredFragment(mBinding.vpMyOrders.currentItem) as MyOrdersChildFragment).onActivityResult(
                requestCode,
                resultCode,
                data
            )
        }
    }
}