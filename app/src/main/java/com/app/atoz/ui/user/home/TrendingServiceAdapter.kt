package com.app.atoz.ui.user.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.atoz.R
import com.app.atoz.common.helper.BaseViewHolder
import com.app.atoz.databinding.ItemTrendingServiceChildBinding
import com.app.atoz.models.ServiceItemModel


class TrendingServiceAdapter(private val list: List<ServiceItemModel>) : RecyclerView.Adapter<TrendingServiceAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_trending_service_child, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val homeData = list.get(position)
        val binding = holder.getBinding<ItemTrendingServiceChildBinding>()
        binding.serviceItemModel = homeData
        //binding.ivService.setImageDrawable(ContextCompat.getDrawable(binding.root.context, homeData.serviceImage))

    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder internal constructor(itemView: View) : BaseViewHolder(itemView)
}

