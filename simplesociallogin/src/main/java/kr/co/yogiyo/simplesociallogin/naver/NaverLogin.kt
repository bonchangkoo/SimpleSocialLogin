package kr.co.yogiyo.simplesociallogin.naver

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.nhn.android.naverlogin.OAuthLogin
import com.nhn.android.naverlogin.OAuthLoginDefine
import com.nhn.android.naverlogin.OAuthLoginHandler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kr.co.yogiyo.simplesociallogin.base.SocialLogin
import kr.co.yogiyo.simplesociallogin.helper.HttpRequestHelper
import kr.co.yogiyo.simplesociallogin.model.LoginResult
import kr.co.yogiyo.simplesociallogin.model.SocialType

class NaverLogin(activity: Activity) : SocialLogin(activity) {
    companion object {
        private const val requestMeUrl = "https://openapi.naver.com/v1/nid/me"
    }

    private val compositeDisposable: CompositeDisposable by lazy {
        CompositeDisposable()
    }

    private val oAuthLoginInstance: OAuthLogin by lazy {
        OAuthLogin.getInstance()
    }

    override fun start() {
        OAuthLoginDefine.MARKET_LINK_WORKING = false

        val config = getConfig(SocialType.NAVER) as NaverConfig
        oAuthLoginInstance.init(activity, config.clientId, config.clientSecret, config.clientName)
        oAuthLoginInstance.startOauthLoginActivity(activity, NaverLoginHandler())
    }

    override fun release() {
        compositeDisposable.clear()
    }

    override fun logout() {
        OAuthLogin.getInstance().logout(activity)

    }

    fun logoutAndDeleteToken(): Boolean = OAuthLogin.getInstance().logoutAndDeleteToken(activity)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    }

    @SuppressLint("HandlerLeak")
    private inner class NaverLoginHandler : OAuthLoginHandler() {

        override fun run(success: Boolean) {
            if (success) {
                val accessToken = oAuthLoginInstance.getAccessToken(activity)
                val authHeader = "Bearer $accessToken"
                requestMe(authHeader)
            }
        }
    }

    private fun requestMe(authHeader: String) {
        val disposable = HttpRequestHelper.createRequest(requestMeUrl, authHeader)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    val jsonObject = Gson().fromJson(it, JsonObject::class.java)
                    val response = jsonObject.getAsJsonObject("response")

                    if (response == null) {
                        onLoginFail(SocialType.NAVER)
                        return@subscribe
                    }

                    val loginResult = LoginResult().apply {
                        try {
                            this.oAuthInfo = LoginResult.OAuthInfo(
                                    oAuthLoginInstance.getAccessToken(activity),
                                    oAuthLoginInstance.getRefreshToken(activity),
                                    oAuthLoginInstance.getExpiresAt(activity)
                            )
                        } catch (exception: Exception) {
                            onLoginFail(SocialType.NAVER)
                        }
                        this.id = response.get("id").asString
                        this.name = ""
                        if (response.has("name")) {
                            this.name = response.get("name").asString
                        }
                        this.email = ""
                        if (response.has("email")) {
                            this.email = response.get("email").asString
                        }
                        this.nickname = ""
                        if (response.has("nickname")) {
                            this.nickname = response.get("nickname").asString
                        }
                        this.type = SocialType.NAVER
                        this.status = LoginResult.STATUS_SUCCESS
                    }

                    when {
                        loginResult.oAuthInfo.accessToken.isEmpty() -> onLoginFail(SocialType.NAVER)
                        else -> onLoginSuccess(loginResult)
                    }

                }) {
                    onLoginFail(SocialType.NAVER)
                }

        compositeDisposable.add(disposable)
    }
}