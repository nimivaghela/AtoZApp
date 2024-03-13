package com.app.atoz.ui.provider.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.atoz.R
import com.app.atoz.common.helper.BaseViewHolder
import com.app.atoz.databinding.ItemProviderExistCategoryBinding
import com.app.atoz.models.CategoryModel

class ProviderExistCategoryAdapter(
    private val mDataList: ArrayList<CategoryModel>,
    private val mListener: ProviderExistServiceAdapter.OnProviderExistServiceListener?
) :
    RecyclerView.Adapter<BaseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return CategoryExistViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_provider_exist_category,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(baseViewHolder: BaseViewHolder, position: Int) {
        with(mDataList[position]) {
            val binding = baseViewHolder.getBinding<ItemProviderExistCategoryBinding>()
            binding.tvServiceName.text = categoryName
            serviceList?.let {
                binding.rvService.adapter = ProviderExistServiceAdapter(it, mListener)
            }
        }
    }

    override fun getItemCount() = mDataList.size

    inner class CategoryExistViewHolder internal constructor(itemView: View) : BaseViewHolder(itemView)
}
