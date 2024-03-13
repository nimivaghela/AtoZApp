package com.app.atoz.shareddata.base

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.app.atoz.R
import com.app.atoz.common.extentions.hideKeyboard
import com.app.atoz.common.extentions.resToast
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.github.pwittchen.reactivenetwork.library.rx2.internet.observing.InternetObservingSettings
import com.github.pwittchen.reactivenetwork.library.rx2.internet.observing.strategy.SocketInternetObservingStrategy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import org.jetbrains.annotations.NotNull
import timber.log.Timber


abstract class BaseActivity : AppCompatActivity(), BaseView {


    @LayoutRes
    abstract fun getResource(): Int

    open lateinit var mDisposable: CompositeDisposable
    private lateinit var binding: ViewDataBinding

    abstract fun initView()
    abstract fun handleListener()
    abstract fun displayMessage(message: String)

    var isInternetConnected: Boolean = true

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initDisposable()
        initConnectivity()
        setView(getResource())
    }

    private fun setView(@LayoutRes layoutId: Int) {
        try {
            binding = DataBindingUtil.setContentView(this, layoutId)

            initView()
            handleListener()
        } catch (e: Exception) {
            Timber.e(this.localClassName, e.printStackTrace())
            resToast(e.localizedMessage)
        }
    }

    protected fun <T : ViewDataBinding> getBinding(): T {
        @Suppress("UNCHECKED_CAST")
        return binding as T
    }


    private lateinit var mToolbar: Toolbar

    @SuppressLint("RestrictedApi")
    protected fun setToolbar(
        @NotNull toolbar: Toolbar, @NotNull title: String?, isBackEnabled: Boolean = false,
        backgroundColor: Int = R.color.colorPrimary
    ) {
        this.mToolbar = toolbar
        super.setSupportActionBar(toolbar)
        toolbar.setBackgroundColor(ContextCompat.getColor(this, backgroundColor))
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.title = title
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        toolbar.title = title

        if (isBackEnabled) {
            supportActionBar?.setDefaultDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeButtonEnabled(true)
            if (backgroundColor == R.color.white)
                toolbar.setNavigationIcon(R.drawable.ic_back_arrow)
            toolbar.setNavigationOnClickListener { onBackPressed() }
        }
    }

    fun setTitle(@NotNull title: String) {
        this.mToolbar.title = title
    }


    fun setNavigationIcon(navigationIconResId: Int) {
        if (::mToolbar.isInitialized) {
            mToolbar.setNavigationIcon(navigationIconResId)
        }
    }

    private fun initConnectivity() {
        val settings = InternetObservingSettings.builder()
            .host("www.google.com")
            .strategy(SocketInternetObservingStrategy())
            .interval(3000)
            .build()

        ReactiveNetwork
            .observeInternetConnectivity(settings)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { isConnectedToHost ->
                isInternetConnected = isConnectedToHost
            }.addTo(mDisposable)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun initDisposable() {
        mDisposable = CompositeDisposable()
    }


    override fun onDestroy() {
        super.onDestroy()
        mDisposable.clear()
        mDisposable.dispose()
    }

    fun changeFragment(
        fragmentManager: FragmentManager,
        fragment: Fragment,
        layout: Int,
        addToBackStack: Boolean = false
    ) {
        fragmentManager.beginTransaction().replace(layout, fragment, fragment::class.java.name).commit()
    }

    fun checkFragmentVisible(fragmentManager: FragmentManager, fragmentName: String): Boolean {
        return fragmentManager.findFragmentByTag(fragmentName)?.isVisible ?: false
    }

    override fun onBackPressed() {
        super.onBackPressed()
        hideKeyboard()
    }

    override fun onUnknownError(error: String?) {
        error?.let {
            Timber.d("Base Activity $error")
            displayMessage(error)
        }
    }

    override fun internalServer() {
        Timber.d("Base Activity API Internal server")
        displayMessage(getString(R.string.text_error_internal_server))
    }

    override fun onTimeout() {
        Timber.d("Base Activity API Timeout")
        displayMessage(getString(R.string.text_error_timeout))
    }

    override fun onNetworkError() {
        Timber.d("Base Activity network error")
        displayMessage(getString(R.string.text_error_network))
    }

    override fun onConnectionError() {
        Timber.d("Base Activity internet issue")
        displayMessage(getString(R.string.text_error_connection))
    }

}