package kr.co.yogiyo.simplesociallogin.base

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import com.kakao.auth.KakaoSDK
import kr.co.yogiyo.simplesociallogin.kakao.KakaoLogin
import kr.co.yogiyo.simplesociallogin.listener.LoginResultListener
import kr.co.yogiyo.simplesociallogin.kakao.KakaoSDKAdapter
import kr.co.yogiyo.simplesociallogin.listener.RefreshTokenCallback
import kr.co.yogiyo.simplesociallogin.model.LoginResult
import kr.co.yogiyo.simplesociallogin.model.SocialConfig
import kr.co.yogiyo.simplesociallogin.model.SocialType
import kr.co.yogiyo.simplesociallogin.naver.NaverLogin
import java.lang.ref.WeakReference

abstract class SocialLogin(activity: Activity) {

    lateinit var resultListener: LoginResultListener

    private val activityWeakReference: WeakReference<Activity> = WeakReference(activity)

    protected val activity: Activity?
        get() = activityWeakReference.get()

    protected val kakaoSDKAdapter: KakaoSDKAdapter by lazy {
        KakaoSDKAdapter(activity.applicationContext)
    }

    abstract fun start()

    abstract fun release()

    abstract fun logout()

    abstract fun unlinkApp(): Boolean

    abstract fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

    fun onLoginSuccess(loginResult: LoginResult) {
        resultListener.onLoginResultReceived(loginResult)
    }

    fun onLoginFail(socialType: SocialType) {
        resultListener.onLoginResultReceived(LoginResult.createFailResult(socialType))
    }

    companion object {
        private lateinit var application: Application

        private lateinit var socialConfigMap: HashMap<SocialType, SocialConfig>

        @JvmStatic
        fun init(application: Application?, socialConfigMap: HashMap<SocialType, SocialConfig>?) {
            Companion.application = application!!
            Companion.socialConfigMap = socialConfigMap!!

            initializeSDK()
        }

        private fun initializeSDK() {
            if (socialConfigMap.containsKey(SocialType.KAKAO)) {
                KakaoSDK.init(KakaoSDKAdapter(application.applicationContext))
            }
        }

        internal fun getConfig(type: SocialType): SocialConfig {
            if (!socialConfigMap.containsKey(type)) {
                throw RuntimeException(String.format("There is no ${type.name} SocialConfig."))
            }
            return socialConfigMap[type]!!
        }

        @JvmStatic
        fun refreshAccessToken(context: Context, socialType: String, callback: RefreshTokenCallback) {
            if (socialType == SocialType.NAVER.name.toLowerCase()) {
                NaverLogin.refreshAccessToken(context, callback)
            } else if (socialType == SocialType.KAKAO.name.toLowerCase()) {
                KakaoLogin.refreshAccessToken(callback)
            }
        }
    }
}

