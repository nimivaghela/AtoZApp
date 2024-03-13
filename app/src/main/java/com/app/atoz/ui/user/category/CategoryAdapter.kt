package com.app.atoz.ui.user.category

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.atoz.R
import com.app.atoz.databinding.ItemViewAllCategoryBinding
import com.app.atoz.models.CategoryModel

class CategoryAdapter(var mDataList: ArrayList<CategoryModel>, var mListener: CategoryClickListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return CategoryViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_view_all_category,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = mDataList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is CategoryViewHolder) {
            holder.mBinding.category = mDataList[position]
        }
    }

    inner class CategoryViewHolder(itemView: ItemViewAllCategoryBinding) : RecyclerView.ViewHolder(itemView.root) {
        val mBinding: ItemViewAllCategoryBinding = itemView

        init {
            itemView.root.setOnClickListener {
                mListener.onClick(mDataList[adapterPosition])
            }
        }
    }

    interface CategoryClickListener {
        fun onClick(data: CategoryModel)
    }
}