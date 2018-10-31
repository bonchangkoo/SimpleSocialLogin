package kr.co.yogiyo.simplesociallogin

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.support.annotation.CheckResult
import android.support.v4.app.FragmentActivity
import com.kakao.auth.KakaoSDK
import io.reactivex.Observable
import kr.co.yogiyo.simplesociallogin.base.SocialLogin
import kr.co.yogiyo.simplesociallogin.internal.SocialLoginObservable
import kr.co.yogiyo.simplesociallogin.internal.weak
import kr.co.yogiyo.simplesociallogin.internal.BuilderFunction
import kr.co.yogiyo.simplesociallogin.internal.exception.LoginFailedException
import kr.co.yogiyo.simplesociallogin.internal.initSocialLoginJava
import kr.co.yogiyo.simplesociallogin.kakao.KakaoLogin
import kr.co.yogiyo.simplesociallogin.kakao.KakaoSDKAdapter
import kr.co.yogiyo.simplesociallogin.internal.impl.OnResponseListener
import kr.co.yogiyo.simplesociallogin.internal.impl.RefreshTokenCallback
import kr.co.yogiyo.simplesociallogin.model.LoginResultItem
import kr.co.yogiyo.simplesociallogin.model.PlatformType
import kr.co.yogiyo.simplesociallogin.model.SocialConfig
import kr.co.yogiyo.simplesociallogin.naver.NaverLogin
import java.util.*

object SimpleSocialLogin {
    private var configMap: MutableMap<PlatformType, SocialConfig> = HashMap()
    private var moduleMap: WeakHashMap<PlatformType, SocialLogin> = WeakHashMap()
    private var application: Application? by weak(null)

    const val EXCEPTION_FAILED_RESULT = "Failed to get results."
    const val EXCEPTION_MAIN_THREAD = "Expected to be called on the main thread but was "
    const val EXCEPTION_USER_CANCELLED = "User has cancelled the job."
    const val EXCEPTION_UNKNOWN_ERROR = "Unknown error"
    private const val EXCEPTION_CONFIG_MISSING = "Config object is missing."

    /**
     * Initialize 'SimpleSocialLogin' in Java.
     * In Kotlin, use [Application.initSocialLogin] instead.
     */
    @JvmStatic
    fun initSocialLogin(application: Application, callback: BuilderFunction) {
        application.initSocialLoginJava {
            callback.invoke(this)
        }
    }

    /**
     * Initialize 'Social module object' in once by Configs on Application class
     *
     * @param activity [Activity] to initialize individual Social module object.
     */
    @JvmStatic
    fun initialize(activity: Activity) {
        val map = configMap.map {
            it.key to when (it.key) {
                PlatformType.KAKAO -> KakaoLogin(activity)
                PlatformType.NAVER -> NaverLogin(activity)
                PlatformType.NONE -> TODO()
            }
        }.toMap().toMutableMap()

        moduleMap.clear()
        moduleMap.putAll(map)
    }

    /**
     * Try Login of [SocialLogin] using given [PlatformType]
     */
    @JvmStatic
    fun login(platformType: PlatformType) {
        val socialLogin = moduleMap[platformType]
                ?: throw LoginFailedException(EXCEPTION_CONFIG_MISSING)
        socialLogin.login()
    }

    @JvmStatic
    fun logout(platformType: PlatformType) {
        val socialLogin = moduleMap[platformType]
                ?: throw LoginFailedException(EXCEPTION_CONFIG_MISSING)
        socialLogin.logout()
    }

    @JvmStatic
    fun refreshAccessToken(context: Context, socialType: String, callback: RefreshTokenCallback) {
        if (socialType == PlatformType.NAVER.name.toLowerCase()) {
            (moduleMap[PlatformType.NAVER] as NaverLogin?)?.refreshAccessToken(context, callback)
        } else if (socialType == PlatformType.KAKAO.name.toLowerCase()) {
            (moduleMap[PlatformType.KAKAO] as KakaoLogin?)?.refreshAccessToken(context, callback)
        }
    }

    @JvmStatic
    fun unlinkApp(platformType: PlatformType): Boolean {
        return moduleMap[platformType]?.unlinkApp() ?: false
    }

    /**
     * Receive [Activity.onActivityResult] event to handle result of platform process
     */
    @JvmStatic
    fun activityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        moduleMap.values.forEach {
            it?.onActivityResult(requestCode, resultCode, data)
        }
    }

    /**
     * Observe SocialLogin result by RxJava2 way
     */
    @CheckResult
    @JvmStatic
    @JvmOverloads
    fun result(activity: Activity? = null): Observable<LoginResultItem> {
        if (moduleMap.isEmpty() && activity != null) initialize(activity)
        return Observable.merge(moduleMap.values.map { SocialLoginObservable(it) })
    }

    /**
     * Observe SocialLogin result by traditional (Listener) way
     */
    @JvmOverloads
    fun result(callback: (LoginResultItem) -> Unit, fragmentActivity: FragmentActivity? = null) {
        if (moduleMap.isEmpty() && fragmentActivity != null) initialize(fragmentActivity)

        val listener = object : OnResponseListener {
            override fun onResultReceived(loginResultItem: LoginResultItem?, error: Throwable?) {
                if (loginResultItem != null && error == null) {
                    callback(loginResultItem)
                } else if (error != null) {
                    throw LoginFailedException(error)
                } else {
                    throw LoginFailedException(EXCEPTION_UNKNOWN_ERROR)
                }
            }
        }

        val newMap = mutableMapOf<PlatformType, SocialLogin>()

        moduleMap.forEach {
            val moduleObject = it.value
            moduleObject?.responseListener = listener
            newMap[it.key] = moduleObject
        }

        moduleMap.clear()
        moduleMap.putAll(newMap)
    }

    internal fun initializeInternal(application: Application, map: Map<PlatformType, SocialConfig>) {
        this.application = application
        configMap.putAll(map)
        configMap.forEach {
            when (it.key) {
                PlatformType.KAKAO -> initKakao()
                else -> {
                }
            }
        }
    }

    internal fun getPlatformConfig(type: PlatformType): SocialConfig {
        if (!configMap.containsKey(type)) {
            throw LoginFailedException(EXCEPTION_CONFIG_MISSING)
        }

        return configMap[type]!!
    }

    private fun initKakao() {
        KakaoSDK.init(KakaoSDKAdapter(application!!.applicationContext))
    }
}