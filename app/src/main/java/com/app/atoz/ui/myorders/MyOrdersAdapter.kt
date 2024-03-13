package com.app.atoz.ui.myorders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.atoz.R
import com.app.atoz.databinding.ItemMyOrdersBinding
import com.app.atoz.models.UserHolder
import com.app.atoz.models.UserServiceStatusList
import timber.log.Timber

class MyOrdersAdapter(
    private val mDataList: ArrayList<UserServiceStatusList.ServiceResponse>,
    private val mUserHolder: UserHolder,
    val mListener: OnOrderItemClickListener
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyOrdersViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_my_orders, parent, false
            )
        )
    }

    override fun getItemCount(): Int = mDataList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MyOrdersViewHolder) {
            with(mDataList[position]) {
                holder.mBinding.order = this
                Timber.d(
                    "Status of the data ${mUserHolder.isUserAsProvider && providerStatusList != null && providerStatusList.size > 0} && ${providerStatusList?.get(
                        0
                    )?.getOrderType()}"
                )
                holder.mBinding.tvOrderStatus.text = when {
                    mUserHolder.isUserAsProvider && providerStatusList != null && providerStatusList.size > 0 -> {
                        providerStatusList[0].getOrderType()
                    }
                    else -> getOrderType()
                }
                holder.mBinding.tvOrderStatus.background = ContextCompat.getDrawable(
                    holder.itemView.context,
                    when (when {
                        mUserHolder.isUserAsProvider && providerStatusList != null && providerStatusList.size > 0 -> {
                            providerStatusList[0].providerStatus.toString()
                        }
                        else -> status.toString()
                    }) {
                        MyOrdersViewModel.TYPE_ORDER_COMPLETED -> {
                            R.drawable.bg_rect_grey_with_radius
                        }
                        MyOrdersViewModel.TYPE_ORDER_CANCELLED -> {
                            R.drawable.bg_rect_red_with_radius
                        }
                        MyOrdersViewModel.TYPE_ORDER_BILL_UPLOADED -> {
                            R.drawable.bg_rect_orange_with_radius
                        }
                        MyOrdersViewModel.TYPE_ORDER_PAYMENT_DONE -> {
                            R.drawable.bg_rect_blue_with_radius
                        }
                        else -> {
                            R.drawable.bg_rect_green_with_radius
                        }
                    }
                )
            }
        }
    }

    inner class MyOrdersViewHolder(itemView: ItemMyOrdersBinding) : RecyclerView.ViewHolder(itemView.root) {
        val mBinding = itemView

        init {
            itemView.root.setOnClickListener {
                mListener.onClick(mDataList[adapterPosition])
            }
        }
    }

    interface OnOrderItemClickListener {
        fun onClick(data: UserServiceStatusList.ServiceResponse)
    }


}