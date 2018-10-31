package kr.co.yogiyo.simplesociallogin.internal.exception

class LoginFailedException : IllegalStateException {

    constructor(message: String) : super(message)

    constructor(message: String, cause: Throwable) : super(message, cause)

    constructor(cause: Throwable) : super(cause)
}
