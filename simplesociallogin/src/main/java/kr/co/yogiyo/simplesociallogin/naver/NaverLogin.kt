package kr.co.yogiyo.simplesociallogin.naver

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.annotation.WorkerThread
import com.nhn.android.naverlogin.OAuthLogin
import com.nhn.android.naverlogin.OAuthLoginDefine
import com.nhn.android.naverlogin.OAuthLoginHandler
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import kr.co.yogiyo.simplesociallogin.SimpleSocialLogin
import kr.co.yogiyo.simplesociallogin.SimpleSocialLogin.EXCEPTION_FAILED_RESULT
import kr.co.yogiyo.simplesociallogin.SimpleSocialLogin.getPlatformConfig
import kr.co.yogiyo.simplesociallogin.base.SocialLogin
import kr.co.yogiyo.simplesociallogin.internal.exception.LoginFailedException
import kr.co.yogiyo.simplesociallogin.internal.impl.RefreshTokenCallback
import kr.co.yogiyo.simplesociallogin.internal.impl.UnlinkAppCallback
import kr.co.yogiyo.simplesociallogin.model.LoginResultItem
import kr.co.yogiyo.simplesociallogin.model.PlatformType

class NaverLogin constructor(activity: Activity) : SocialLogin(activity) {
    private val oAuthLoginInstance: OAuthLogin by lazy {
        OAuthLogin.getInstance()
    }

    override fun login() {
        OAuthLoginDefine.MARKET_LINK_WORKING = false

        val config = getPlatformConfig(PlatformType.NAVER) as NaverConfig
        oAuthLoginInstance.init(activity, config.authClientId, config.authClientSecret, config.clientName)
        oAuthLoginInstance.startOauthLoginActivity(activity, NaverLoginHandler())
    }

    override fun logout() {
        Flowable.just("")
                .map {
                    oAuthLoginInstance.logout(activity)
                }
                .subscribeOn(Schedulers.io())
                .subscribe({
                    // Do nothing
                },{
                    // Do nothing
                })
    }

    override fun unlinkApp(callback: UnlinkAppCallback) {
        if (oAuthLoginInstance.logoutAndDeleteToken(activity)) {
            callback.onUnlinkSuccess()
        } else {
            callback.onUnlinkFailure()
        }
    }

    @WorkerThread
    override fun refreshAccessToken(context: Context?, callback: RefreshTokenCallback) {
        try {
            val accessToken = oAuthLoginInstance.refreshAccessToken(context)
            if (!accessToken.isNullOrEmpty()) {
                callback.onRefreshSuccess(accessToken)
            } else {
                callback.onRefreshFailure()
            }
        } catch (e: java.lang.Exception) {
            callback.onRefreshFailure()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Do nothing
    }

    @SuppressLint("HandlerLeak")
    private inner class NaverLoginHandler : OAuthLoginHandler() {

        override fun run(success: Boolean) {
            if (success) {
                var accessToken = ""
                try {
                    accessToken = oAuthLoginInstance.getAccessToken(activity)
                } catch (e: Exception) {
                    // pass
                }
                if (accessToken.isEmpty()) {
                    callbackAsFail(LoginFailedException(SimpleSocialLogin.EXCEPTION_FAILED_RESULT))

                } else {
                    val loginResultItem = LoginResultItem().apply {
                        try {
                            this.oAuthInfo = LoginResultItem.OAuthInfo(
                                    oAuthLoginInstance.getAccessToken(activity),
                                    oAuthLoginInstance.getRefreshToken(activity),
                                    oAuthLoginInstance.getExpiresAt(activity)
                            )
                        } catch (exception: Exception) {
                            callbackAsFail(LoginFailedException(SimpleSocialLogin.EXCEPTION_FAILED_RESULT))
                        }

                        this.platformType = PlatformType.NAVER
                        this.status = LoginResultItem.STATUS_SUCCESS
                    }

                    callbackAsSuccess(loginResultItem)
                }

            } else {
                callbackAsFail(LoginFailedException(EXCEPTION_FAILED_RESULT))
            }
        }
    }
}