package kr.co.yogiyo.simplesociallogin.listener

import kr.co.yogiyo.simplesociallogin.model.LoginResult

interface LoginResultListener {
    fun onLoginResultReceived(loginResult: LoginResult)
}