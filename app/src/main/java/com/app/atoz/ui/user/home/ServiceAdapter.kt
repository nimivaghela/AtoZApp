package com.app.atoz.ui.user.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.atoz.R
import com.app.atoz.common.helper.BaseViewHolder
import com.app.atoz.common.helper.RxHelper
import com.app.atoz.databinding.ItemServiceChildBinding
import com.app.atoz.models.homescreen.ChildrenItem
import com.google.gson.JsonObject
import io.reactivex.disposables.CompositeDisposable


class ServiceAdapter(
    private val list: List<ChildrenItem>, var disposable: CompositeDisposable,
    private val listener: ItemClickListener?,
    private val categoryID: String?

) : RecyclerView.Adapter<ServiceAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_service_child, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val homeData = list[position]
        val binding = holder.getBinding<ItemServiceChildBinding>()
        binding.serviceItemModel = homeData
        binding.executePendingBindings()

    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder internal constructor(itemView: View) : BaseViewHolder(itemView) {

        init {
            val itemServiceBinding: ItemServiceChildBinding = getBinding()

            RxHelper.onClick(itemServiceBinding.ivService, disposable)
            {

                val body = JsonObject()
                body.addProperty("name", list[adapterPosition].name)
                body.addProperty("categoryID", categoryID)
                body.addProperty("subCategoryID", list[adapterPosition].id)

                listener?.onItemClick(itemServiceBinding.ivService, adapterPosition, body)
            }
        }

    }
}

