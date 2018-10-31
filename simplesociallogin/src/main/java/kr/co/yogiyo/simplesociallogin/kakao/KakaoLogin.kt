package kr.co.yogiyo.simplesociallogin.kakao

import android.app.Activity
import android.content.Intent
import com.kakao.auth.*
import com.kakao.auth.authorization.accesstoken.AccessToken
import com.kakao.network.ErrorResult
import com.kakao.usermgmt.UserManagement
import com.kakao.usermgmt.callback.MeV2ResponseCallback
import com.kakao.usermgmt.response.MeV2Response
import com.kakao.util.OptionalBoolean
import com.kakao.util.exception.KakaoException
import kr.co.yogiyo.simplesociallogin.base.SocialLogin
import kr.co.yogiyo.simplesociallogin.listener.RefreshTokenCallback
import kr.co.yogiyo.simplesociallogin.model.LoginResult
import kr.co.yogiyo.simplesociallogin.model.SocialType

class KakaoLogin(activity: Activity) : SocialLogin(activity) {

    private var requestMe = false

    private val sessionCallback: SessionCallback by lazy {
        SessionCallback()
    }

    companion object {
        fun refreshAccessToken(callback: RefreshTokenCallback) {
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
    }

    override fun start() {
        checkSession()

        val session = Session.getCurrentSession()
        session.addCallback(sessionCallback)

        if (!session.checkAndImplicitOpen()) {
            session.open(AuthType.KAKAO_LOGIN_ALL, activity)
        }
    }

    fun startWithRequestMe() {
        requestMe = true
        start()
    }

    override fun release() {
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

    private fun requestMe() {
        val config = getConfig(SocialType.KAKAO) as KakaoConfig

        UserManagement.getInstance().me(config.requestOptions, object : MeV2ResponseCallback() {
            override fun onSuccess(result: MeV2Response) {
                val id = result.id.toString()
                val nickname = result.nickname.toString()

                // optional value
                var email = ""

                val userAccount = result.kakaoAccount

                if (userAccount != null && userAccount.hasEmail() == OptionalBoolean.TRUE) {
                    email = userAccount.email
                }


                val loginResult = LoginResult().apply {
                    try {
                        this.oAuthInfo = LoginResult.OAuthInfo(
                                Session.getCurrentSession().tokenInfo.accessToken,
                                Session.getCurrentSession().tokenInfo.refreshToken,
                                Session.getCurrentSession().tokenInfo.remainingExpireTime.toLong()
                        )
                    } catch (exception: Exception) {
                        onLoginFail(SocialType.KAKAO)
                    }

                    this.id = id
                    this.nickname = nickname
                    this.email = email
                    this.type = SocialType.KAKAO
                    this.status = LoginResult.STATUS_SUCCESS
                }

                when {
                    loginResult.oAuthInfo.accessToken.isEmpty() -> onLoginFail(SocialType.KAKAO)
                    else -> onLoginSuccess(loginResult)
                }
            }

            override fun onSessionClosed(errorResult: ErrorResult?) {
                if (errorResult?.errorCode == -401) {
                    // Could not refresh access token.
                } else {
                    onLoginFail(SocialType.KAKAO)
                }
            }
        })
    }

    private inner class SessionCallback : ISessionCallback {
        override fun onSessionOpenFailed(exception: KakaoException?) {
            onLoginFail(SocialType.KAKAO)
        }

        override fun onSessionOpened() {
            val accessToken = Session.getCurrentSession().tokenInfo.accessToken
            if (accessToken.isEmpty()) {
                onLoginFail(SocialType.KAKAO)
            } else {
                if (requestMe) {
                    requestMe()

                } else {
                    val loginResult = LoginResult().apply {
                        try {
                            this.oAuthInfo = LoginResult.OAuthInfo(
                                    Session.getCurrentSession().tokenInfo.accessToken,
                                    Session.getCurrentSession().tokenInfo.refreshToken,
                                    Session.getCurrentSession().tokenInfo.remainingExpireTime.toLong()
                            )
                        } catch (exception: Exception) {
                            onLoginFail(SocialType.KAKAO)
                        }

                        this.type = SocialType.KAKAO
                        this.status = LoginResult.STATUS_SUCCESS
                    }
                    onLoginSuccess(loginResult)
                }
            }
        }
    }
}