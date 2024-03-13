package com.app.atoz.ui.user.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.atoz.R
import com.app.atoz.common.helper.BaseViewHolder
import com.app.atoz.databinding.ItemMoreSavingChildBinding
import com.app.atoz.models.homescreen.CouponItem


class MoreSavingAdapter(
    private val mDataList: List<CouponItem>,
    private val mCouponCodeListener: OnCouponCodeClickListener
) :
    RecyclerView.Adapter<MoreSavingAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_more_saving_child, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val binding = holder.getBinding<ItemMoreSavingChildBinding>()
        binding.couponItem = mDataList[position]

        //binding.ivService.setImageDrawable(ContextCompat.getDrawable(binding.root.context, homeData.serviceImage))

    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    inner class ViewHolder internal constructor(itemView: View) : BaseViewHolder(itemView) {
        val mBinding = itemView

        init {
            mBinding.rootView.setOnClickListener {
                mCouponCodeListener.onClick(mDataList[adapterPosition])
            }
        }
    }

    interface OnCouponCodeClickListener {
        fun onClick(couponItem: CouponItem)
    }
}

