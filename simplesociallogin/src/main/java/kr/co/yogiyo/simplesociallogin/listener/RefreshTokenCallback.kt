package kr.co.yogiyo.simplesociallogin.listener

interface RefreshTokenCallback {
    fun onRefreshSuccess(accessToken: String)

    fun onRefreshFailure()
}