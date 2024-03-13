package com.app.atoz.ui.notification

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.atoz.R
import com.app.atoz.common.extentions.stringToDate
import com.app.atoz.databinding.ItemNotificationBinding
import com.app.atoz.models.notification.NotificationItem
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import org.json.JSONObject

class NotificationAdapter(var mDataList: List<NotificationItem?>, var mListener: NotificationClickListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_notification,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = mDataList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {


            JSONObject(mDataList[position]?.payload).optJSONObject("data").let {
                holder.mBinding.txtTitle.text = it.optString("title", "")
                holder.mBinding.txtBody.text = it.optString("body", "")
                try {
                    val requestOption: RequestOptions = RequestOptions()
                        .centerCrop()
                        .placeholder(R.drawable.ic_logo_splash)
                        .error(R.drawable.ic_logo_splash)

                    Glide.with(holder.mBinding.imgNotification)
                        .load(
                            if (it.has("data")) {
                                it.getJSONObject("data")?.optString("image")
                            } else R.drawable.ic_logo_splash
                        )
                        .apply(requestOption)
                        .into(holder.mBinding.imgNotification)
                } catch (e: Exception) {
                    e.printStackTrace()
                }


            }
            holder.mBinding.txtDate.text = mDataList[position]?.createdAt?.stringToDate("MMM dd , yyyy hh:mm a") ?: ""


        }
    }

    inner class ViewHolder(itemView: ItemNotificationBinding) : RecyclerView.ViewHolder(itemView.root) {
        val mBinding: ItemNotificationBinding = itemView

        init {
            itemView.root.setOnClickListener {
                try {
                    if (JSONObject(mDataList[adapterPosition]?.payload).has("data")) {
                        val dataObj = JSONObject(mDataList[adapterPosition]?.payload).optJSONObject("data")
                        if (dataObj.has("data") && dataObj.optJSONObject("data").has("order_id")) {
                            mListener.onClick(
                                JSONObject(mDataList[adapterPosition]?.payload).optJSONObject("data").optJSONObject("data").optString(
                                    "order_id"
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    interface NotificationClickListener {
        fun onClick(orderID: String)
    }
}