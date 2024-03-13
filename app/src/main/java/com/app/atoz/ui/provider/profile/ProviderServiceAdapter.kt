package com.app.atoz.ui.provider.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.atoz.R
import com.app.atoz.common.helper.BaseViewHolder
import com.app.atoz.common.helper.RxHelper
import com.app.atoz.databinding.ItemProviderServiceBinding
import com.app.atoz.models.HomeData
import com.app.atoz.ui.user.home.ItemClickListener
import io.reactivex.disposables.CompositeDisposable


class ProviderServiceAdapter(
    private val list: List<HomeData>,
    var disposable: CompositeDisposable,
    private val listener: ItemClickListener?
) : RecyclerView.Adapter<BaseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return ServiceVH(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_provider_service,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(baseViewHolder: BaseViewHolder, position: Int) {
        val homeData = list[position]

        val binding = baseViewHolder.getBinding<ItemProviderServiceBinding>()
        binding.serviceName = homeData.serviceName
        //binding.rvService.adapter = ServiceAdapter(homeData.lstService)
    }

    override fun getItemCount(): Int {
        return 2
    }

    inner class ServiceVH internal constructor(itemView: View) : BaseViewHolder(itemView) {

        init {
            val binding: ItemProviderServiceBinding = getBinding()

            RxHelper.onClick(binding.tvEdit, disposable)
            {
                listener?.onItemClick(binding.tvEdit, adapterPosition, list[adapterPosition])
            }
        }
    }
}