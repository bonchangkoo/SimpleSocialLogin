package kr.co.yogiyo.simplesociallogin.internal

import android.app.Application
import kr.co.yogiyo.simplesociallogin.SimpleSocialLogin
import kr.co.yogiyo.simplesociallogin.internal.impl.ConfigFunction
import kr.co.yogiyo.simplesociallogin.internal.impl.invoke
import kr.co.yogiyo.simplesociallogin.kakao.KakaoConfig
import kr.co.yogiyo.simplesociallogin.model.PlatformType
import kr.co.yogiyo.simplesociallogin.model.SocialConfig
import kr.co.yogiyo.simplesociallogin.naver.NaverConfig


fun Application.initSocialLogin(setup: ConfigDSLBuilder.() -> Unit) {
    val builder = ConfigDSLBuilder(this)
    builder.setup()
    builder.build()
}

fun Application.initSocialLoginJava(setup: ConfigBuilder.() -> Unit) {
    val builder = ConfigBuilder(this)
    builder.setup()
    builder.build()
}

interface BuilderFunction {
    fun invoke(builder: ConfigBuilder)
}

open class BaseConfigDSLBuilder(val application: Application) {
    internal val typeMap: MutableMap<PlatformType, SocialConfig> = mutableMapOf()

    fun naver(authClientId: String, authClientSecret: String, clientName: String) {
        typeMap[PlatformType.NAVER] = NaverConfig.apply(authClientId, authClientSecret, clientName)
    }

    internal fun build() {
        SimpleSocialLogin.initializeInternal(application, typeMap)
    }
}

class ConfigDSLBuilder(application: Application) : BaseConfigDSLBuilder(application) {
    fun kakao(setup: KakaoConfig.() -> Unit = {}) {
        typeMap[PlatformType.KAKAO] = KakaoConfig.apply(invoke(setup))
    }
}

class ConfigBuilder(application: Application) : BaseConfigDSLBuilder(application) {
    @JvmOverloads
    fun kakao(setup: ConfigFunction<KakaoConfig> = EmptyFunction()) {
        typeMap[PlatformType.KAKAO] = KakaoConfig.apply(setup)
    }

    internal class EmptyFunction<T> : ConfigFunction<T> {
        override fun invoke(config: T) {}
    }
}