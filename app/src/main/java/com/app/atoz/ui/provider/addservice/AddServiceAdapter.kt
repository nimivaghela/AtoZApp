package com.app.atoz.ui.provider.addservice

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.atoz.R
import com.app.atoz.models.ServiceModel


class AddServiceAdapter(
    private var mDataList:ArrayList<ServiceModel>) :RecyclerView.Adapter<RecyclerView.ViewHolder>()
{


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return AddServiceViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_select_service, parent, false
            )

        )
    }

    override fun getItemCount() :Int = mDataList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is AddServiceAdapter.AddServiceViewHolder) {
            holder.mBinding.service = mDataList[position]
            holder.mBinding.viewDivider.visibility = if (itemCount - 1 == position) View.GONE else View.VISIBLE
        }
    }


    inner class AddServiceViewHolder(itemView: com.app.atoz.databinding.ItemSelectServiceBinding):RecyclerView.ViewHolder(itemView.root)
    {
        val mBinding =itemView

        init {

            mBinding.rvServiceBg.setOnClickListener {
                mDataList[adapterPosition].selected = !mDataList[adapterPosition].selected
                notifyItemChanged(adapterPosition)

            }
        }
    }
}