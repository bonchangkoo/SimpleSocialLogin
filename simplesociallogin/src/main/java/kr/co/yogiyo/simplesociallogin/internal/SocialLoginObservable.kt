package kr.co.yogiyo.simplesociallogin.internal

import android.os.Looper
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable
import io.reactivex.disposables.Disposables
import kr.co.yogiyo.simplesociallogin.SimpleSocialLogin
import kr.co.yogiyo.simplesociallogin.base.SocialLogin
import kr.co.yogiyo.simplesociallogin.internal.exception.LoginFailedException
import kr.co.yogiyo.simplesociallogin.internal.impl.OnResponseListener
import kr.co.yogiyo.simplesociallogin.model.LoginResultItem

open class SocialLoginObservable(private val login: SocialLogin) : Observable<LoginResultItem>() {

    override fun subscribeActual(observer: Observer<in LoginResultItem>?) {
        if (observer == null || !checkMainThread(observer)) {
            observer?.onError(LoginFailedException(SimpleSocialLogin.EXCEPTION_MAIN_THREAD))
            return
        }

        val listener = Listener(login, observer)
        login.responseListener = listener
        observer.onSubscribe(listener)
    }

    private class Listener(val login: SocialLogin, val observer: Observer<in LoginResultItem>?) :
            MainThreadDisposable(), OnResponseListener {
        override fun onResultReceived(loginResultItem: LoginResultItem?, error: Throwable?) {
            if (!isDisposed) {
                when {
                    loginResultItem != null && loginResultItem.status == LoginResultItem.STATUS_SUCCESS -> observer?.onNext(loginResultItem)
                    error != null -> observer?.onError(error)
                    else -> observer?.onError(LoginFailedException(SimpleSocialLogin.EXCEPTION_UNKNOWN_ERROR))
                }
            }
        }

        override fun onDispose() {
            login.onDestroy()
        }
    }

    companion object {

        fun <T> checkMainThread(observer: Observer<T>): Boolean {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                observer.onSubscribe(Disposables.empty())
                observer.onError(LoginFailedException(SimpleSocialLogin.EXCEPTION_MAIN_THREAD + Thread.currentThread().name))
                return false
            }
            return true
        }
    }
}