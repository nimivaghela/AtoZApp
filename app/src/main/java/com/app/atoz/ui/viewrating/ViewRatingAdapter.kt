package com.app.atoz.ui.viewrating

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.atoz.R
import com.app.atoz.databinding.ItemViewRatingBinding
import com.app.atoz.models.UserRatingModel

class ViewRatingAdapter(private val mDataList: ArrayList<UserRatingModel>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewRatingViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_view_rating, parent, false
            )
        )
    }

    override fun getItemCount(): Int = mDataList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewRatingViewHolder) {
            with(mDataList[position]) {
                holder.mBinding.ratingModel = this
            }
        }
    }

    inner class ViewRatingViewHolder(itemView: ItemViewRatingBinding) : RecyclerView.ViewHolder(itemView.root) {
        val mBinding = itemView
    }
}