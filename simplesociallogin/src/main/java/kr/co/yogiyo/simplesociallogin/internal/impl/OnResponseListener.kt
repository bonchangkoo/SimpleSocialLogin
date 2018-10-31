package kr.co.yogiyo.simplesociallogin.internal.impl

import kr.co.yogiyo.simplesociallogin.model.LoginResultItem

interface OnResponseListener {
    fun onResultReceived(loginResultItem: LoginResultItem?, error: Throwable?)
}