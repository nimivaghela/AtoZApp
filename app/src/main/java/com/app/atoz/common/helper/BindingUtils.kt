package com.app.atoz.common.helper

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.atoz.R
import com.app.atoz.common.extentions.loadImage
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
import android.widget.TextView




@BindingAdapter("round_image")
fun bindImage(view: ImageView, source: String) {
    Glide.with(view).load(source).apply(RequestOptions().circleCrop()).into(view)
}

@BindingAdapter("load_url_image")
fun bindGlideUrl(view: ImageView, url: String?) {
    view.loadImage(url, null, null, R.drawable.bg_bag)
}

@BindingAdapter("load_image")
fun bindImageUrl(view: ImageView, url: String?) {
    val requestOption: RequestOptions = RequestOptions()
        .centerCrop()
        .placeholder(R.drawable.ic_logo_splash)
        .error(R.drawable.ic_logo_splash)

    Glide.with(view)
        .load(url)
        .apply(requestOption)
        .into(view)
}

@BindingAdapter("load_fit_center_image")
fun bindFitCenterImageUrl(view: ImageView, url: String?) {
    val requestOption: RequestOptions = RequestOptions()
        .fitCenter()
        .placeholder(R.drawable.ic_logo_splash)
        .error(R.drawable.ic_logo_splash)

    Glide.with(view)
        .load(url)
        .apply(requestOption)
        .into(view)
}

@BindingAdapter("load_image_circular")
fun bindCircularImageUrl(view: ImageView, url: String?) {
    view.loadImage(url, null, null, R.drawable.bg_circle_grey_camera, true)
}

@BindingAdapter("adapter")
fun adapter(view: RecyclerView, adapter: RecyclerView.Adapter<BaseViewHolder>) {
    view.layoutManager = LinearLayoutManager(view.context)
    view.adapter = adapter
}

@BindingAdapter("strikeThrough")
fun strikeThrough(textView: TextView, strikeThrough: Boolean) {
    if (strikeThrough) {
        textView.paintFlags = textView.paintFlags or STRIKE_THRU_TEXT_FLAG
    } else {
        textView.paintFlags = textView.paintFlags and STRIKE_THRU_TEXT_FLAG.inv()
    }
}