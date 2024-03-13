package com.app.atoz.ui.provider.selectservice

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.atoz.R
import com.app.atoz.databinding.ItemSelectServiceBinding
import com.app.atoz.models.ServiceModel

class SelectServiceAdapter(
    private var mDataList: ArrayList<ServiceModel>,
    private var mListener: OnServiceItemClickListener?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return SelectServiceViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_select_service, parent, false
            )
        )
    }

    override fun getItemCount(): Int = mDataList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is SelectServiceViewHolder) {
            holder.mBinding.service = mDataList[position]
            holder.mBinding.viewDivider.visibility = if (itemCount - 1 == position) View.GONE else View.VISIBLE
        }
    }

    inner class SelectServiceViewHolder(itemView: ItemSelectServiceBinding) : RecyclerView.ViewHolder(itemView.root) {
        val mBinding = itemView

        init {
            mBinding.rvServiceBg.setOnClickListener {
                mDataList[adapterPosition].selected = !mDataList[adapterPosition].selected
                notifyItemChanged(adapterPosition)
                mListener?.updateCounter()
            }
        }
    }

    interface OnServiceItemClickListener {
        fun updateCounter()

        fun scrollAtPosition(position: Int)

        fun onItemClick(view: View, position: Int, serviceModel: ServiceModel)
    }
}