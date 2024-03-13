package com.app.atoz.ui.myorders.orderdetails

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.atoz.R
import com.app.atoz.common.extentions.removeUnnecessaryDecimalPoint
import com.app.atoz.databinding.ItemOrderSubCategoryBinding
import com.app.atoz.models.RequestItemList
import com.app.atoz.utils.Config

class OrderSubCategoryAdapter(private val mDataList: ArrayList<RequestItemList>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return OrderSubCategoryViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_order_sub_category,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = mDataList.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is OrderSubCategoryViewHolder) {
            with(mDataList[position]) {
                holder.mBinding.let {
                    it.productItem = this
                    it.tvPriceValue.text =
                        "${Config.RUPEES_SYMBOL} ${String.format("%.2f", servicePrice).removeUnnecessaryDecimalPoint()}"
                }
            }
        }
    }

    inner class OrderSubCategoryViewHolder(itemView: ItemOrderSubCategoryBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        val mBinding = itemView
    }

}