package com.app.atoz.common.extentions

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.app.atoz.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import java.io.File

fun View.snack(@StringRes msg: Int) {
    Snackbar.make(this, context.getString(msg), Snackbar.LENGTH_SHORT).show()
}

fun View.snack(msg: String, duration: Int = Snackbar.LENGTH_SHORT) {
    Snackbar.make(this, msg, duration).show()
}

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.inVisible() {
    this.visibility = View.INVISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

@SuppressLint("CheckResult")
fun ImageView.loadImage(
    url: String?, height: Int? = null, width: Int? = null,
    default: Int = R.mipmap.ic_launcher, isCircle: Boolean = false
) {
    val requestOption: RequestOptions = RequestOptions()
        .placeholder(default)
        .error(default)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .transform(RoundedCorners(8.toPx))

    if (isCircle)
        requestOption.transform(CircleCrop())

    if (height != null && width != null) {
        requestOption.override(height, width)
        Glide.with(this.context)
            .load(url)
            .apply(requestOption)
            .into(this)
    } else {
        Glide.with(this.context)
            .load(url)
            .apply(requestOption)
            .into(this)
    }
}

fun ImageView.loadImageWithProgressBar(url: String, progressBar: ProgressBar) {
    progressBar.visible()
    this.inVisible()
    Glide.with(this.context)
        .load(url)
        .listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: com.bumptech.glide.request.target.Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                progressBar.gone()
                this@loadImageWithProgressBar.visible()
                return false
            }

            override fun onResourceReady(
                resource: Drawable,
                model: Any,
                target: com.bumptech.glide.request.target.Target<Drawable>?,
                dataSource: DataSource,
                isFirstResource: Boolean
            ): Boolean {
                progressBar.gone()
                this@loadImageWithProgressBar.visible()
                return false
            }
        }).into(this)

}

fun ImageView.loadImage(@DrawableRes res: Int, height: Int? = null, width: Int? = null) {
    if (height != null && width != null) {
        Glide.with(this.context)
            .load(res)
            .apply(RequestOptions().override(height, width))
            .into(this)

    } else {
        Glide.with(this.context)
            .load(res)
            .into(this)
    }
}

fun ImageView.loadImage(uri: Uri, height: Int? = null, width: Int? = null) {
    if (height != null && width != null) {
        Glide.with(this.context)
            .load(uri)
            .apply(RequestOptions().override(height, width))
            .into(this)

    } else {
        Glide.with(this.context)
            .load(uri)
            .into(this)
    }
}


fun ImageView.loadImage(file: File, height: Int? = null, width: Int? = null) {
    if (height != null && width != null) {
        Glide.with(this.context).asBitmap()
            .load(file)
            .apply(RequestOptions().override(height, width))
            .into(this)

    } else {
        Glide.with(this.context).asBitmap()
            .load(file)
            .into(this)
    }
}
