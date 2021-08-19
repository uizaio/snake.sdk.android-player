package com.uiza.sdk.analytics

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer

object RxBinder {
    fun <T> bind(
        observable: Observable<T>,
        onNext: Consumer<T>?,
        onError: Consumer<Throwable?>?
    ): Disposable {
        return observable.subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext, onError)
    }

    @JvmStatic
    fun <T> bind(
        observable: Observable<T>,
        onNext: Consumer<T>?,
        onError: Consumer<Throwable?>?,
        onComplete: Action?
    ): Disposable {
        return observable.subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext, onError, onComplete)
    }
}
