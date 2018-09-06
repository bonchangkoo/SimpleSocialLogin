package kr.co.yogiyo.simplesociallogin.kakao

import kr.co.yogiyo.simplesociallogin.model.SocialConfig
import java.util.ArrayList

class KakaoConfig constructor(val requestOptions: ArrayList<String>) : SocialConfig() {
    class Builder {
        private var isRequireEmail = false

        fun setRequireEmail(): Builder {
            isRequireEmail = true
            return this
        }

        fun build(): KakaoConfig {
            val requestOptions = ArrayList<String>()
            requestOptions.add("properties.nickname")
            requestOptions.add("properties.profile_image")

            return KakaoConfig(requestOptions)
        }
    }
}