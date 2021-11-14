package com.uiza.sdk.utils

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target

class ImageUtils private constructor() {

//    enum class TransformationType {
//        CIRCLE,
//        ROUND,
//        NONE;
//
//        fun getTransformation(): Transformation<Bitmap> {
//            return when (this) {
//                CIRCLE -> CircleCrop()
//                ROUND -> RoundedCorners(20)
//                else -> RoundedCorners(0)
//            }
//        }
//    }

    companion object {
//        fun load(
//            imageView: ImageView,
//            url: String,
//            resPlaceHolder: Int,
//            progressBar: ProgressBar?
//        ) {
//            Glide.with(imageView.context).load(url)
//                .apply(RequestOptions().placeholder(resPlaceHolder))
//                .listener(object : RequestListener<Drawable?> {
//                    override fun onLoadFailed(
//                        e: GlideException?,
//                        model: Any,
//                        target: Target<Drawable?>,
//                        isFirstResource: Boolean
//                    ): Boolean {
//                        if (progressBar != null && progressBar.visibility != View.GONE) progressBar.visibility =
//                            View.GONE
//                        return false
//                    }
//
//                    override fun onResourceReady(
//                        resource: Drawable?,
//                        model: Any,
//                        target: Target<Drawable?>,
//                        dataSource: DataSource,
//                        isFirstResource: Boolean
//                    ): Boolean {
//                        if (progressBar != null && progressBar.visibility != View.GONE) progressBar.visibility =
//                            View.GONE
//                        return false
//                    }
//                })
//                .into(imageView)
//        }

//        @JvmOverloads
//        fun load(
//            imageView: ImageView,
//            imageUrl: String,
//            placeholder: Int = 0,
//            transformationType: TransformationType = TransformationType.NONE
//        ) {
//            var builder = Glide.with(imageView.context)
//                .load(imageUrl)
//                .centerCrop()
//                .transition(DrawableTransitionOptions.withCrossFade())
//                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
//            if (placeholder > 0) {
//                builder = builder.placeholder(placeholder)
//            }
//            if (transformationType != TransformationType.NONE) {
//                builder = builder.transform(transformationType.getTransformation(imageView.context))
//            }
//            builder.into(imageView)
//        }

        @JvmStatic
        fun loadThumbnail(imageView: ImageView, imageUrl: String? = "", currentPosition: Long) {
//            Glide.with(imageView)
//                .load(imageUrl)
//                .into(imageView)

            Glide.with(imageView)
                .load(imageUrl)
                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .transform(GlideThumbnailTransformation(currentPosition))
                .into(imageView)
        }
    }
}
