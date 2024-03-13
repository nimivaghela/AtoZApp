package com.app.atoz.ui.user.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.atoz.R
import com.app.atoz.databinding.ItemAdvertiseBinding
import com.app.atoz.models.AdvertisementItem

class AdvertiseAdapter(
    val mDataList: ArrayList<AdvertisementItem>,
    val mListener: OnAdvertiseClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return AdvertiseViewHolder(
            DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_advertise, parent, false)
        )
    }

    override fun getItemCount(): Int = mDataList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is AdvertiseViewHolder) {
            with(mDataList[position]) {
                holder.mBinding.advertise = this
                holder.mBinding.tvReadMore.visibility = if (redirectUrl == null) View.GONE else View.VISIBLE
            }
        }
    }

    inner class AdvertiseViewHolder(itemView: ItemAdvertiseBinding) : RecyclerView.ViewHolder(itemView.root) {
        val mBinding = itemView

        init {
            mBinding.root.setOnClickListener {
                mDataList[adapterPosition].apply {
                    mListener.onClick(this, mimeType == 2)
                }
            }

            mBinding.tvReadMore.setOnClickListener {
                mDataList[adapterPosition].let {
                    mListener.onClick(it, false)
                }
            }
        }
    }

    interface OnAdvertiseClickListener {
        fun onClick(advertisementItem: AdvertisementItem, isVideo: Boolean)
    }
}