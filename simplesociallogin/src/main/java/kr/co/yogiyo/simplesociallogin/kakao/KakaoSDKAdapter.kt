package kr.co.yogiyo.simplesociallogin.kakao

import android.content.Context
import com.kakao.auth.IApplicationConfig
import com.kakao.auth.KakaoAdapter

class KakaoSDKAdapter(val context: Context) : KakaoAdapter() {

    override fun getApplicationConfig(): IApplicationConfig {
        return IApplicationConfig { context.applicationContext }
    }
}