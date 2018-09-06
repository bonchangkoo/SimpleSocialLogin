package kr.co.yogiyo.simplesociallogin.base

import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable
import kr.co.yogiyo.simplesociallogin.listener.LoginResultListener
import kr.co.yogiyo.simplesociallogin.model.LoginResult

open class SocialLoginObservable<T : SocialLogin>(private val socialLogin: T) : Observable<LoginResult>() {
    override fun subscribeActual(observer: Observer<in LoginResult>?) {
        val listener = Listener(socialLogin, observer)
        socialLogin.resultListener = listener
        observer?.onSubscribe(listener)
    }

    private class Listener<out T : SocialLogin>(val socialLogin: T, val observer: Observer<in LoginResult>?) :
            MainThreadDisposable(), LoginResultListener {

        override fun onLoginResultReceived(loginResult: LoginResult) {
            if (!isDisposed) {
                val status = loginResult.status
                if (status == LoginResult.STATUS_SUCCESS) {
                    observer?.onNext(loginResult)

                } else {
                    observer?.onError(Exception("socialLogin failed: ${loginResult.type}"))
                }
            }
        }

        override fun onDispose() {
            socialLogin.release()
        }
    }
}