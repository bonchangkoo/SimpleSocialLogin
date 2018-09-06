package kr.co.yogiyo.simplesociallogin.naver

import kr.co.yogiyo.simplesociallogin.model.SocialConfig

class NaverConfig constructor(val clientId: String?, val clientSecret: String?,
                              val clientName: String?): SocialConfig() {
    class Builder {
        private var clientId: String? = null
        private var clientSecret: String? = null
        private var clientName: String? = null

        fun setClientId(clientId: String): Builder {
            this.clientId = clientId
            return this
        }

        fun setClientSecret(clientSecret: String?): Builder {
            this.clientSecret = clientSecret
            return this
        }

        fun setClientName(clientName: String): Builder {
            this.clientName = clientName
            return this
        }

        fun build(): NaverConfig {
            return NaverConfig(clientId, clientSecret, clientName)
        }
    }
}