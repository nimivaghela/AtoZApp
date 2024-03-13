package com.app.atoz.ui.myorders.orderdetails

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.atoz.R
import com.app.atoz.databinding.ItemOrderImagesBinding
import com.app.atoz.models.OrderDetailModel


class OrderItemImageAdapter(
    val mDataList: List<OrderDetailModel.ServiceRequest.ServiceRequestImage>,
    val mListener: OnImageItemClickListener?
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return OrderItemImageViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_order_images, parent, false
            )
        )
    }

    override fun getItemCount(): Int = mDataList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is OrderItemImageViewHolder) {
            holder.mBinding.service = mDataList[position]
        }
    }


    inner class OrderItemImageViewHolder(itemView: ItemOrderImagesBinding) : RecyclerView.ViewHolder(itemView.root) {
        val mBinding = itemView

        init {
            mBinding.root.setOnClickListener {
                mDataList[adapterPosition].original?.let {
                    mListener?.onClick(it)
                }
            }
        }
    }

    interface OnImageItemClickListener {
        fun onClick(url: String)
    }
}