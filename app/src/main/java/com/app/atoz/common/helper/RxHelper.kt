package com.app.atoz.common.helper

import android.view.View
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import java.util.concurrent.TimeUnit


class RxHelper {
    companion object {
        fun onClick(view: View, disposable: CompositeDisposable, onRxClick: () -> Unit) {
            RxView.clicks(view)
                .debounce(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onNext = {
                    onRxClick()
                }, onError = {
                    it.printStackTrace()
                }).addTo(disposable)
        }
    }
}