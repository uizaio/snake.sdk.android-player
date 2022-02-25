package com.uiza.sdk.utils

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target

class ImageUtils private constructor() {

    companion object {
        @JvmStatic
        fun loadThumbnail(imageView: ImageView, imageUrl: String? = "", currentPosition: Long) {
            Glide.with(imageView)
                .load(imageUrl)
                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .transform(GlideThumbnailTransformation(currentPosition))
                .into(imageView)
        }
    }
}
