package kr.co.yogiyo.simplesociallogin.model

class LoginResultItem {
    lateinit var platformType: PlatformType
    var id = ""
    var name = ""
    var email = ""
    var nickname = ""
    var status = STATUS_FAIL

    lateinit var oAuthInfo: OAuthInfo

    companion object {
        const val STATUS_FAIL = 0
        const val STATUS_SUCCESS = 1

        fun createFailResult(type: PlatformType) = LoginResultItem().apply {
            this.status = STATUS_FAIL
            this.platformType = type
        }
    }

    override fun toString(): String {
        return "LoginResultItem(platformType=$platformType, status=$status, id='$id', name='$name', email='$email', nickname='$nickname')"
    }

    class OAuthInfo(var accessToken: String = "", var refreshToken: String = "", private var expiresAt: Long = -1L) {

        override fun toString(): String {
            return "LoginResultItem.OAuthIOnfo(accessToken=$accessToken, refreshToken=$refreshToken, expiresAt=$expiresAt)"
        }
    }
}