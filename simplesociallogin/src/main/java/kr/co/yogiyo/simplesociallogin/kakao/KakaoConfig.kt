package kr.co.yogiyo.simplesociallogin.kakao

import kr.co.yogiyo.simplesociallogin.internal.impl.ConfigFunction
import kr.co.yogiyo.simplesociallogin.model.SocialConfig
import java.util.ArrayList

class KakaoConfig : SocialConfig() {
    var requireEmail = false
    var requireAgeRange = false
    var requireBirthday = false
    var requireGender = false

    companion object {
        internal fun apply(setup: ConfigFunction<KakaoConfig>? = null): KakaoConfig {
            val config = KakaoConfig()
            setup?.invoke(config)
            return config
        }
    }
}
