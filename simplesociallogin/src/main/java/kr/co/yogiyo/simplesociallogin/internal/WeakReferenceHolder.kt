package kr.co.yogiyo.simplesociallogin.internal

import java.lang.ref.WeakReference
import kotlin.reflect.KProperty

class WeakReferenceHolder<T>(private var value: WeakReference<T?>) {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        return value.get()
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        this.value = WeakReference(value)
    }
}

fun <T> weak(value: T) = WeakReferenceHolder(WeakReference(value))