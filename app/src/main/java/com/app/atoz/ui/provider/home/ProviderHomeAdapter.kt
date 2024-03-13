package com.app.atoz.ui.provider.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.atoz.R
import com.app.atoz.common.helper.BaseViewHolder
import com.app.atoz.common.helper.bindImage
import com.app.atoz.models.providerhome.ServiceRequestItem
import com.app.atoz.ui.user.home.ItemClickListener


class ProviderHomeAdapter(
    private var mDataList: List<ServiceRequestItem>?,
    private val mListener: ItemClickListener,
    private val timer: Int
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ProviderHomeViewHolder) {
            mDataList?.let { dataList ->
                with(dataList[position]) {
                    holder.mBinding.let { view ->
                        view.tvUserName.text = name
                        view.tvCity.text = userAddress?.city?.name
                        view.tvCity.text = userAddress?.city?.name
                        view.tvService.text = childCategory
                        view.tvDate.text = getFormattedDate()
                        view.tvTime.text = getFormattedTime()
                        view.tvRequestEndTime.text = getRequestEndTime(timer)
                        user?.image?.let { bindImage(view.ivUser, it) }
                        view.executePendingBindings()
                    }
                }
            }
        }
    }

    fun setServiceRequestList(serviceRequestItemLst: List<ServiceRequestItem>?) {
        mDataList = serviceRequestItemLst
        notifyDataSetChanged()

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ProviderHomeViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_provider_home,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return mDataList?.size ?: 0
    }

    inner class ProviderHomeViewHolder internal constructor(itemView: com.app.atoz.databinding.ItemProviderHomeBinding) :
        BaseViewHolder(itemView.root) {
        val mBinding = itemView

        init {
            mBinding.cvJobItem.setOnClickListener {
                mListener.onItemClick(it, adapterPosition, mDataList?.get(adapterPosition))
            }
        }
    }
}

