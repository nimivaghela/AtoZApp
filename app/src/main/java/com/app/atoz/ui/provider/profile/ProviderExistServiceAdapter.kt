package com.app.atoz.ui.provider.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.atoz.R
import com.app.atoz.common.extentions.loadImage
import com.app.atoz.common.helper.BaseViewHolder
import com.app.atoz.databinding.ItemProviderExistServiceBinding
import com.app.atoz.models.ServiceModel

class ProviderExistServiceAdapter(
    private val mDataList: ArrayList<ServiceModel>,
    private val mListener: OnProviderExistServiceListener?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ServiceExistViewHolder) {
            with(mDataList[position]) {
                holder.mBinding.tvServiceName.text = serviceName
                holder.mBinding.ivService.loadImage(serviceImageUrl, null, null, R.drawable.ic_logo_splash)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ServiceExistViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_provider_exist_service, parent, false
            )
        )
    }

    override fun getItemCount() = mDataList.size

    inner class ServiceExistViewHolder internal constructor(itemView: ItemProviderExistServiceBinding) :
        BaseViewHolder(itemView.root) {
        val mBinding = itemView

        init {
            itemView.root.setOnClickListener {
                mListener?.onClick(mDataList[adapterPosition].serviceId.toString())
            }
        }
    }

    interface OnProviderExistServiceListener {
        fun onClick(serviceId: String)
    }
}

