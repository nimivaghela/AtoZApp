package com.app.atoz.ui.main

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.View
import androidx.fragment.app.Fragment
import com.app.atoz.AppApplication
import com.app.atoz.R
import com.app.atoz.common.extentions.showAlert
import com.app.atoz.models.UserHolder
import com.app.atoz.shareddata.base.BaseActivity
import com.app.atoz.ui.myorders.MyOrdersFragment
import com.app.atoz.ui.provider.home.ProviderHomeFragment
import com.app.atoz.ui.provider.profile.ProviderProfileFragment
import com.app.atoz.ui.settings.SettingsFragment
import com.app.atoz.ui.user.home.HomeFragment
import com.app.atoz.ui.user.profile.UserMyProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import javax.inject.Inject


class MainActivity : BaseActivity() {

    companion object {

        fun start(context: Context) {
            val mainIntent = Intent(context, MainActivity::class.java)
            mainIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(mainIntent)
        }
    }

    override fun getResource(): Int = R.layout.activity_main

    private lateinit var mBinding: com.app.atoz.databinding.ActivityMainBinding
    @Inject
    lateinit var userHolder: UserHolder

    private var mCurrentItemID: Int = 0

    override fun initView() {
        (application as AppApplication).mComponent.inject(this)

        mBinding = getBinding()
        setToolbar(mBinding.includeToolbar.toolbar, getString(R.string.text_welcome))

        if (userHolder.isUserAsProvider) {
            mBinding.navView.inflateMenu(R.menu.bottom_provider_nav_menu)
        } else {
            mBinding.navView.inflateMenu(R.menu.bottom_user_nav_menu)
        }

        navigationHome()?.let {
            changeFragment(supportFragmentManager, it, R.id.frm_main)
        }
    }

    override fun handleListener() {
        mBinding.navView.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener {
            if (mCurrentItemID == it.itemId) {
                return@OnNavigationItemSelectedListener false
            } else {
                mCurrentItemID = it.itemId
            }

            var fragment: Fragment? = null
            when (it.itemId) {
                R.id.navigation_home -> {
                    fragment = navigationHome()
                }
                R.id.navigation_orders -> {
                    fragment = navigationOrder()
                }
                R.id.navigation_profile -> {
                    fragment = navigationProfile()
                }
                R.id.navigation_settings -> {
                    fragment = navigationSetting()
                }
            }

            fragment?.let {
                changeFragment(supportFragmentManager, fragment, R.id.frm_main)
                return@OnNavigationItemSelectedListener true
            } ?: let {
                return@OnNavigationItemSelectedListener false
            }
        })
    }

    override fun onBackPressed() {
        handleFragmentBackNavigation()
    }

    private fun handleFragmentBackNavigation() {
        if (checkFragmentVisible(supportFragmentManager, MyOrdersFragment::class.java.name) ||
            checkFragmentVisible(supportFragmentManager, ProviderProfileFragment::class.java.name) ||
            checkFragmentVisible(supportFragmentManager, UserMyProfileFragment::class.java.name) ||
            checkFragmentVisible(supportFragmentManager, SettingsFragment::class.java.name)
        ) {
            navigationHome()?.let { changeFragment(supportFragmentManager, it, R.id.frm_main) }
            mBinding.navView.menu.findItem(R.id.navigation_home).isChecked = true
            mCurrentItemID = R.id.navigation_home
        } else if (checkFragmentVisible(supportFragmentManager, ProviderHomeFragment::class.java.name)
            || checkFragmentVisible(supportFragmentManager, HomeFragment::class.java.name)
        ) {
            showAlert(getString(R.string.text_are_your_sure),
                null,
                getString(R.string.text_yes),
                getString(R.string.text_no),
                DialogInterface.OnClickListener { dialog, button ->
                    dialog?.dismiss()
                    if (button == DialogInterface.BUTTON_POSITIVE) {
                        dialog?.dismiss()
                        this@MainActivity.finish()
                    }
                })
        }
    }

    private fun navigationSetting(): Fragment? {
        val fragment: Fragment?
        fragment = SettingsFragment.newInstance()
        mBinding.includeToolbar.toolbar.visibility = View.VISIBLE
        return fragment
    }

    private fun navigationProfile(): Fragment? {
        val fragment: Fragment
        if (userHolder.isUserAsProvider) {
            fragment = ProviderProfileFragment.newInstance()
        } else {
            fragment = UserMyProfileFragment.newInstance()
        }
        mBinding.includeToolbar.toolbar.visibility = View.GONE
        return fragment
    }

    private fun navigationOrder(): Fragment? {
        val fragment: Fragment
        fragment = MyOrdersFragment.newInstance()
        mBinding.includeToolbar.toolbar.visibility = View.VISIBLE

        return fragment
    }

    private fun navigationHome(): Fragment? {
        val fragment: Fragment
        if (userHolder.isUserAsProvider) {
            fragment = ProviderHomeFragment.newInstance()
            mBinding.includeToolbar.toolbar.visibility = View.VISIBLE
        } else {
            fragment = HomeFragment.newInstance()
            mBinding.includeToolbar.toolbar.visibility = View.GONE
        }
        return fragment
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val providerHomeFragment: Fragment? =
            supportFragmentManager.findFragmentByTag(ProviderHomeFragment::class.java.name)
        providerHomeFragment?.onActivityResult(requestCode, resultCode, data)
        val userProfileFragment: Fragment? =
            supportFragmentManager.findFragmentByTag(UserMyProfileFragment::class.java.name)
        userProfileFragment?.onActivityResult(requestCode, resultCode, data)
        val providerProfileFragment: Fragment? =
            supportFragmentManager.findFragmentByTag(ProviderProfileFragment::class.java.name)
        providerProfileFragment?.onActivityResult(requestCode, resultCode, data)

        val myOrdersFragment: Fragment? =
            supportFragmentManager.findFragmentByTag(MyOrdersFragment::class.java.name)
        myOrdersFragment?.onActivityResult(requestCode, resultCode, data)

    }

    override fun displayMessage(message: String) {

    }
}
