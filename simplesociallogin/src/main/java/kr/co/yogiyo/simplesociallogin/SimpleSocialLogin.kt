package kr.co.yogiyo.simplesociallogin

import android.support.annotation.CheckResult
import io.reactivex.Observable
import kr.co.yogiyo.simplesociallogin.kakao.KakaoLogin
import kr.co.yogiyo.simplesociallogin.kakao.RxKakaoLogin
import kr.co.yogiyo.simplesociallogin.model.LoginResult
import kr.co.yogiyo.simplesociallogin.naver.NaverLogin
import kr.co.yogiyo.simplesociallogin.naver.RxNaverLogin

object SimpleSocialLogin {
    @CheckResult
    @JvmStatic
    fun naver(login: NaverLogin): Observable<LoginResult>? {
        return RxNaverLogin(login)
    }

    @CheckResult
    @JvmStatic
    fun kakao(login: KakaoLogin): Observable<LoginResult>? {
        return RxKakaoLogin(login)
    }
}