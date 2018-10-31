package kr.co.yogiyo.simplesociallogin.kakao

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.kakao.auth.*
import com.kakao.auth.authorization.accesstoken.AccessToken
import com.kakao.network.ErrorResult
import com.kakao.usermgmt.UserManagement
import com.kakao.usermgmt.callback.MeV2ResponseCallback
import com.kakao.usermgmt.response.MeV2Response
import com.kakao.util.OptionalBoolean
import com.kakao.util.exception.KakaoException
import kr.co.yogiyo.simplesociallogin.SimpleSocialLogin
import kr.co.yogiyo.simplesociallogin.base.SocialLogin
import kr.co.yogiyo.simplesociallogin.internal.exception.LoginFailedException
import kr.co.yogiyo.simplesociallogin.internal.impl.RefreshTokenCallback
import kr.co.yogiyo.simplesociallogin.model.LoginResultItem
import kr.co.yogiyo.simplesociallogin.model.PlatformType

class KakaoLogin(activity: Activity) : SocialLogin(activity) {

    private val sessionCallback: SessionCallback by lazy {
        SessionCallback()
    }

    override fun login() {
        checkSession()

        val session = Session.getCurrentSession()

        session.addCallback(sessionCallback)
        if (!session.checkAndImplicitOpen()) {
            session.open(AuthType.KAKAO_LOGIN_ALL, activity)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        checkSession()

        Session.getCurrentSession().removeCallback(sessionCallback)

    }

    override fun logout() {
        checkSession()

        if (Session.getCurrentSession().checkAndImplicitOpen()) {
            Session.getCurrentSession().close()
        }
    }

    override fun unlinkApp(): Boolean {
        logout()

        return Session.getCurrentSession().isClosed
    }

    override fun refreshAccessToken(context: Context?, callback: RefreshTokenCallback) {
        val refreshToken = Session.getCurrentSession().tokenInfo.refreshToken
        Session.getCurrentSession().accessTokenManager.refreshAccessToken(refreshToken, object : AccessTokenCallback() {
            override fun onAccessTokenReceived(accessToken: AccessToken?) {
                if (!accessToken?.accessToken.isNullOrEmpty()) {
                    callback.onRefreshSuccess(accessToken!!.accessToken)
                } else {
                    callback.onRefreshFailure()
                }
            }

            override fun onAccessTokenFailure(errorResult: ErrorResult?) {
                callback.onRefreshFailure()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        checkSession()
        Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)
    }

    private fun checkSession() {
        try {
            Session.getCurrentSession().checkAndImplicitOpen()
        } catch (e: Exception) {
            KakaoSDK.init(kakaoSDKAdapter)
        }
    }

    private inner class SessionCallback : ISessionCallback {
        override fun onSessionOpenFailed(exception: KakaoException?) {
            if (exception != null) {
                callbackAsFail(LoginFailedException(SimpleSocialLogin.EXCEPTION_FAILED_RESULT, exception))
            } else {
                callbackAsFail(LoginFailedException(SimpleSocialLogin.EXCEPTION_FAILED_RESULT))
            }
        }

        override fun onSessionOpened() {
            val accessToken = Session.getCurrentSession().tokenInfo.accessToken
            if (accessToken.isEmpty()) {
                callbackAsFail(LoginFailedException(SimpleSocialLogin.EXCEPTION_FAILED_RESULT))

            } else {
                val loginResult = LoginResultItem().apply {
                    try {
                        this.oAuthInfo = LoginResultItem.OAuthInfo(
                                Session.getCurrentSession().tokenInfo.accessToken,
                                Session.getCurrentSession().tokenInfo.refreshToken,
                                Session.getCurrentSession().tokenInfo.remainingExpireTime.toLong()
                        )
                    } catch (exception: Exception) {
                        callbackAsFail(LoginFailedException(SimpleSocialLogin.EXCEPTION_FAILED_RESULT))
                    }

                    this.platformType = PlatformType.KAKAO
                    this.status = LoginResultItem.STATUS_SUCCESS
                }
                callbackAsSuccess(loginResult)
            }
        }
    }
}