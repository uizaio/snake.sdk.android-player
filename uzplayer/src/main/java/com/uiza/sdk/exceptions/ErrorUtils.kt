package com.uiza.sdk.exceptions

object ErrorUtils {
    @JvmStatic
    fun exceptionNoConnection(): UZException {
        return UZException(ErrorConstant.ERR_CODE_0, ErrorConstant.ERR_0)
    }

    @JvmStatic
    fun exceptionNoLinkPlay(): UZException {
        return UZException(ErrorConstant.ERR_CODE_5, ErrorConstant.ERR_5)
    }

    @JvmStatic
    fun exceptionSetup(): UZException {
        return UZException(ErrorConstant.ERR_CODE_7, ErrorConstant.ERR_7)
    }

    @JvmStatic
    fun exceptionChangeSkin(): UZException {
        return UZException(ErrorConstant.ERR_CODE_9, ErrorConstant.ERR_9)
    }

    @JvmStatic
    fun exceptionPlayback(): UZException {
        return UZException(ErrorConstant.ERR_CODE_24, ErrorConstant.ERR_24)
    }
}
