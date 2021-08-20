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
    fun exceptionTryAllLinkPlay(): UZException {
        return UZException(ErrorConstant.ERR_CODE_6, ErrorConstant.ERR_6)
    }

    @JvmStatic
    fun exceptionSetup(): UZException {
        return UZException(ErrorConstant.ERR_CODE_7, ErrorConstant.ERR_7)
    }

    @JvmStatic
    fun exceptionChangeSkin(): UZException {
        return UZException(ErrorConstant.ERR_CODE_9, ErrorConstant.ERR_9)
    }

    fun exceptionListHQ(): UZException {
        return UZException(ErrorConstant.ERR_CODE_10, ErrorConstant.ERR_10)
    }

    fun exceptionListAudio(): UZException {
        return UZException(ErrorConstant.ERR_CODE_11, ErrorConstant.ERR_11)
    }

    fun exceptionShowPip(): UZException {
        return UZException(ErrorConstant.ERR_CODE_19, ErrorConstant.ERR_19)
    }

    @JvmStatic
    fun exceptionPlayback(): UZException {
        return UZException(ErrorConstant.ERR_CODE_24, ErrorConstant.ERR_24)
    }

    fun exceptionNoPlaylist(): UZException {
        return UZException(ErrorConstant.ERR_CODE_25, ErrorConstant.ERR_25)
    }

    @JvmStatic
    fun exceptionPlaylistFolderItemFirst(): UZException {
        return UZException(ErrorConstant.ERR_CODE_25, ErrorConstant.ERR_25)
    }

    @JvmStatic
    fun exceptionPlaylistFolderItemLast(): UZException {
        return UZException(ErrorConstant.ERR_CODE_26, ErrorConstant.ERR_26)
    }
}
