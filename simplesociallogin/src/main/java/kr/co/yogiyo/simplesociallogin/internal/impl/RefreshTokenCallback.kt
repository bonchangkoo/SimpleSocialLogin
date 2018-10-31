package kr.co.yogiyo.simplesociallogin.internal.impl

interface RefreshTokenCallback {
    fun onRefreshSuccess(accessToken: String)

    fun onRefreshFailure()
}