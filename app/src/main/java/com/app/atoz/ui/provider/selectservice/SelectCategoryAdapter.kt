package com.app.atoz.ui.provider.selectservice

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.atoz.R
import com.app.atoz.common.extentions.isVisible
import com.app.atoz.databinding.ItemSelectCategoryBinding
import com.app.atoz.models.CategoryModel
import com.app.atoz.ui.provider.existservice.ExistServiceAdapter

class SelectCategoryAdapter(
    private var mDataList: ArrayList<CategoryModel>,
    private var mListener: SelectServiceAdapter.OnServiceItemClickListener?,
    private var isForSelection: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return SelectCategoryViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_select_category,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = mDataList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is SelectCategoryViewHolder) {
            with(mDataList[position]) {
                holder.mBinding.tvCategoryName.text = categoryName
                holder.mBinding.tvCategoryName
                    .setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        if (isExpanded) R.drawable.ic_up_arrow else R.drawable.ic_down_arrow,
                        0
                    )
                holder.mBinding.rvChildView.visibility = isExpanded.isVisible()
                when {
                    isExpanded -> when {
                        serviceList != null && serviceList!!.size > 0 -> {
                            holder.mBinding.llServiceListBg.visibility = View.VISIBLE
                            holder.mBinding.llEmptyView.visibility = View.GONE
                            holder.mBinding.rvServices.adapter =
                                if (isForSelection) {
                                    serviceList?.let {
                                        SelectServiceAdapter(it, mListener)
                                    }
                                } else {
                                    serviceList?.let {
                                        ExistServiceAdapter(it, mListener)
                                    }
                                }
                        }
                        else -> {
                            holder.mBinding.llServiceListBg.visibility = View.GONE
                            holder.mBinding.llEmptyView.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    inner class SelectCategoryViewHolder(itemView: ItemSelectCategoryBinding) : RecyclerView.ViewHolder(itemView.root) {
        val mBinding = itemView

        init {
            itemView.tvCategoryName.setOnClickListener {
                mDataList[adapterPosition].isExpanded = !mDataList[adapterPosition].isExpanded
                notifyItemChanged(adapterPosition)
                mListener?.scrollAtPosition(adapterPosition)
            }
        }
    }
}