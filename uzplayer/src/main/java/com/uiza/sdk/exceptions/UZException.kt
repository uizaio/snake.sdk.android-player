package com.uiza.sdk.exceptions

class UZException : Exception {
    var code = 0
        private set

    constructor() : super() {}

    constructor(code: Int, message: String?) : super(message) {
        this.code = code
    }

    constructor(code: Int, message: String?, cause: Throwable?) : super(message, cause) {
        this.code = code
    }

    constructor(code: Int, cause: Throwable?) : super(cause) {
        this.code = code
    }
}
