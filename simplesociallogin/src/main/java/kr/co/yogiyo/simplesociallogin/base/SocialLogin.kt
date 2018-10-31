package kr.co.yogiyo.simplesociallogin.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import io.reactivex.disposables.CompositeDisposable
import kr.co.yogiyo.simplesociallogin.internal.weak
import kr.co.yogiyo.simplesociallogin.internal.impl.OnResponseListener
import kr.co.yogiyo.simplesociallogin.internal.impl.RefreshTokenCallback
import kr.co.yogiyo.simplesociallogin.kakao.KakaoSDKAdapter
import kr.co.yogiyo.simplesociallogin.model.LoginResultItem

abstract class SocialLogin constructor(activity: Activity) {

    internal var responseListener: OnResponseListener? = null

    protected val kakaoSDKAdapter: KakaoSDKAdapter by lazy {
        KakaoSDKAdapter(activity.applicationContext)
    }

    protected val compositeDisposable = CompositeDisposable()

    protected var activity: Activity? by weak(null)

    init {
        this.activity = activity
    }

    abstract fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

    abstract fun login()

    abstract fun unlinkApp(): Boolean

    abstract fun refreshAccessToken(context: Context?, callback: RefreshTokenCallback)

    open fun onDestroy() {
        compositeDisposable.clear()
    }

    open fun logout() { }

    protected fun callbackAsFail(exception: Exception) {
        responseListener?.onResultReceived(null, exception)
    }

    protected fun callbackAsSuccess(loginResultItem: LoginResultItem) {
        responseListener?.onResultReceived(loginResultItem, null)
    }
}

