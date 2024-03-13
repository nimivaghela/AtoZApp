package com.app.atoz.ui.welcome

import android.os.Bundle
import com.app.atoz.R
import com.app.atoz.common.extentions.snack
import com.app.atoz.databinding.FragmentWelcomeBinding
import com.app.atoz.shareddata.base.BaseFragment

class WelcomeFragment : BaseFragment() {
    companion object {
        private const val KEY_PAGE_INDEX = "PageIndex"
        fun newInstance(pageIndex: Int): WelcomeFragment {
            val bundle = Bundle()
            bundle.putInt(KEY_PAGE_INDEX, pageIndex)
            val fragment = WelcomeFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun getInflateResource(): Int = R.layout.fragment_welcome

    lateinit var mBinding: FragmentWelcomeBinding

    override fun initView() {
        mBinding = getBinding()
    }

    override fun postInit() {
        // no need to implement
        mBinding.tvWelcomeDesc.text = getString(
            when (arguments?.getInt(KEY_PAGE_INDEX)) {
                2 -> R.string.text_welcome_desc_two
                3 -> R.string.text_welcome_desc_three
                else -> R.string.text_welcome_desc_one
            }
        )
    }

    override fun handleListener() {
        // no need to implement
    }

    override fun displayMessage(message: String) {
        mBinding.llRootView.snack(message)
    }
}