package kr.co.yogiyo.simplesociallogin.internal.impl

interface ConfigFunction<T> {
    fun invoke(config: T)
}

fun <T> invoke(callback: (T.() -> Unit)? = null) = object : ConfigFunction<T> {
    override fun invoke(config: T) {
        callback?.invoke(config)
    }
}