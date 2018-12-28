package kr.co.yogiyo.simplesociallogin.kakao

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import com.kakao.auth.*
import com.kakao.auth.authorization.accesstoken.AccessToken
import com.kakao.network.ErrorResult
import com.kakao.usermgmt.UserManagement
import com.kakao.usermgmt.callback.UnLinkResponseCallback
import com.kakao.util.exception.KakaoException
import kr.co.yogiyo.simplesociallogin.SimpleSocialLogin
import kr.co.yogiyo.simplesociallogin.base.SocialLogin
import kr.co.yogiyo.simplesociallogin.internal.exception.LoginFailedException
import kr.co.yogiyo.simplesociallogin.internal.impl.RefreshTokenCallback
import kr.co.yogiyo.simplesociallogin.internal.impl.UnlinkAppCallback
import kr.co.yogiyo.simplesociallogin.model.LoginResultItem
import kr.co.yogiyo.simplesociallogin.model.PlatformType


class KakaoLogin(activity: Activity) : SocialLogin(activity) {

    companion object {
        const val EXTRA_ERROR_DESCRIPTION = "com.kakao.sdk.talk.error.description"
        const val EXTRA_ERROR_TYPE = "com.kakao.sdk.talk.error.type"
        const val PACKAGE_KAKAO_TALK = "com.kakao.talk"
        const val PACKAGE_KAKAO_STORY = "com.kakao.story"
    }

    private val sessionCallback: SessionCallback by lazy {
        SessionCallback()
    }

    override fun login() {
        kakaoSDKAdapter = null

        logout()

        checkSession()

        val session = Session.getCurrentSession()

        session.addCallback(sessionCallback)
        if (!session.checkAndImplicitOpen()) {
            session.open(AuthType.KAKAO_TALK, activity)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        checkSession()

        Session.getCurrentSession().removeCallback(sessionCallback)
        kakaoSDKAdapter = null
    }

    override fun logout() {
        checkSession()

        if (Session.getCurrentSession().checkAndImplicitOpen()) {
            Session.getCurrentSession().appCache.clearAll()
            Session.getCurrentSession().removeCallback(sessionCallback)
            Session.getCurrentSession().close()
        }

        kakaoSDKAdapter = null
    }

    override fun unlinkApp(callback: UnlinkAppCallback) {
        checkSession()

        UserManagement.getInstance().requestUnlink(object : UnLinkResponseCallback() {
            override fun onSessionClosed(errorResult: ErrorResult?) {
                logout()
                callback.onUnlinkFailure()
            }

            override fun onNotSignedUp() {
                logout()
                callback.onUnlinkFailure()
            }

            override fun onSuccess(result: Long?) {
                logout()
                callback.onUnlinkSuccess()
            }
        })
    }

    override fun refreshAccessToken(context: Context?, callback: RefreshTokenCallback) {
        checkSession()

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

        var isKakaoInstalled = true
        try {
            activity?.packageManager?.getPackageInfo(PACKAGE_KAKAO_TALK, 0)
            activity?.packageManager?.getPackageInfo(PACKAGE_KAKAO_STORY, 0)

        } catch (e: Exception) {
            isKakaoInstalled = false
        }

        try {
            if (isKakaoInstalled) {
                if (data != null && data.extras != null) {
                    val bundle = data.extras!!
                    val errorType = bundle.getString(EXTRA_ERROR_TYPE)
                    val errorDes = bundle.getString(EXTRA_ERROR_DESCRIPTION)
                    if (errorType != null && errorDes != null) {
                        callbackAsFail(LoginFailedException(SimpleSocialLogin.EXCEPTION_FAILED_RESULT + " $errorDes"))
                    } else {
                        Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)
                    }
                } else {
                    Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)
                }

            } else {
                Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)
            }

        } catch (e: Exception) {
            callbackAsFail(LoginFailedException(SimpleSocialLogin.EXCEPTION_FAILED_RESULT + " Unknown Error"))
        }
    }

    private fun checkSession() {
        try {
            Session.getCurrentSession().checkAndImplicitOpen()
        } catch (e: Exception) {
            if (kakaoSDKAdapter == null) {
                kakaoSDKAdapter = KakaoSDKAdapter(activity!!.applicationContext)
            }
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