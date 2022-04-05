package com.uiza.sdk.exceptions

class ErrorConstant {
    companion object {
        const val ERR_CODE_0 = 0
        const val ERR_0 = "No internet connection."
        const val ERR_CODE_5 = 5
        const val ERR_5 = "LinkPlay cannot be null or empty."
        const val ERR_CODE_6 = 6
        const val ERR_6 = "Tried to play all link play of this entity, but failed."
        const val ERR_CODE_7 = 7
        const val ERR_7 = "Setup failed"
        const val ERR_CODE_9 = 9
        const val ERR_9 = "You cannot change skin if player is playing ad."
        const val ERR_CODE_10 = 10
        const val ERR_10 = "Error getHQList null"
        const val ERR_CODE_11 = 11
        const val ERR_11 = "Error audio null"
        const val ERR_CODE_12 = 12
        const val ERR_12 = "Activity cannot be null"
        const val ERR_CODE_13 = 13
        const val ERR_13 = "UZVideo cannot be null"
        const val ERR_CODE_14 = 14
        const val ERR_14 = "You must init custom linkPlay first."
        const val ERR_CODE_15 = 15
        const val ERR_15 = "Context cannot be null."
        const val ERR_CODE_16 = 16
        const val ERR_16 = "Domain api cannot be null or empty"
        const val ERR_CODE_17 = 17
        const val ERR_17 = "Token cannot be null or empty"
        const val ERR_CODE_18 = 18
        const val ERR_18 = "AppID cannot be null or empty"
        const val ERR_CODE_19 = 19
        const val ERR_19 = "Cannot use this feature at this time"
        const val ERR_CODE_20 = 20
        const val ERR_20 = "Cannot chat messenger now"
        const val ERR_CODE_22 = 22
        const val ERR_22 = "Cannot find Messenger App"
        const val ERR_CODE_23 = 23
        const val ERR_23 = "Data of this entity is invalid"
        const val ERR_CODE_24 = 24
        const val ERR_24 = "Error: Playback exception"
        const val ERR_CODE_25 = 25
        const val ERR_25 = "This is the first item of playlist/folder"
        const val ERR_CODE_26 = 26
        const val ERR_26 = "This is the first item of playlist/folder"
        const val ERR_CODE_27 = 27
        const val ERR_27 = "Media includes video tracks, but none are playable by this device"
        const val ERR_CODE_28 = 28
        const val ERR_28 = "Media includes audio tracks, but none are playable by this device"
        const val ERR_CODE_29 = 29
        const val ERR_29 = "Cleartext HTTP traffic not permitted. See https://exoplayer.dev/issues/cleartext-not-permitted"
        const val ERR_CODE_30 = 30
        const val ERR_30 = "DRM content not supported on API levels below 18"
        const val ERR_CODE_31 = 31
        const val ERR_31 = "This device does not support the required DRM scheme"
        const val ERR_CODE_32 = 32
        const val ERR_32 = "Cannot inflater view"
        const val ERR_CODE_33 = 33
        const val ERR_33 = "Cannot enter PIP Mode if screen is landscape"
        const val ERR_CODE_34 = 34
        const val ERR_34 = "setDefaultSeekValue(...) can be applied if the player state is Player.STATE_READY"
        const val ERR_CODE_35 = 35
        const val ERR_35 = "You cannot change skin because you use UZVideoView with UZDragView."

        const val ERR_CODE_400 = 400
        const val ERR_400 =
            "Bad Request: The request was unacceptable, often due to missing a required parameter."
        const val ERR_CODE_401 = 401
        const val ERR_401 = "Unauthorized: No valid API key provided."
        const val ERR_CODE_404 = 404
        const val ERR_404 = "Not Found: The requested resource does not exist."
        const val ERR_CODE_422 = 422
        const val ERR_422 =
            "Un-processable: The syntax of the request entity is incorrect (often is wrong parameter)."
        const val ERR_CODE_500 = 500
        const val ERR_500 =
            "Internal Server Error: We had a problem with our server. Try again later."
        const val ERR_CODE_503 = 503
        const val ERR_503 = "Service Unavailable: The server is overloaded or down for maintenance."
        const val ERR_CODE_504 = 504
        const val ERR_504 = "Exo Player library is missing"
        const val ERR_CODE_505 = 505
        const val ERR_505 = "Chromecast library is missing"
        const val ERR_CODE_506 = 506
        const val ERR_506 = "IMA ads library is missing"
    }
}
