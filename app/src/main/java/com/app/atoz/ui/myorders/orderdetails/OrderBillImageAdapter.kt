package com.app.atoz.ui.myorders.orderdetails

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.atoz.R
import com.app.atoz.databinding.ItemBillImageBinding
import com.app.atoz.models.OrderDetailModel

class OrderBillImageAdapter(
    val mDataList: List<OrderDetailModel.ServiceRequest.ServiceRequestImage>,
    val mListener: OnOrderBillClickListener?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return OrderBillImageViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_bill_image, parent, false
            )
        )
    }

    override fun getItemCount(): Int = mDataList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is OrderBillImageViewHolder) {
            holder.mBinding.serviceImageModel = mDataList[position]
        }
    }

    inner class OrderBillImageViewHolder(itemView: ItemBillImageBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        val mBinding = itemView

        init {
            mBinding.root.setOnClickListener {
                mDataList[adapterPosition].original?.let {
                    mListener?.onClick(it)
                }
            }
        }
    }

    interface OnOrderBillClickListener {
        fun onClick(imageUrl: String)
    }
}