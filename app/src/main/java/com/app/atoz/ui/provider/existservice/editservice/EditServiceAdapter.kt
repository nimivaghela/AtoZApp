package com.app.atoz.ui.provider.existservice.editservice

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.atoz.R
import com.app.atoz.databinding.ItemEditPersonalServiceBinding
import com.app.atoz.models.CategoryModel
import com.app.atoz.utils.Config


class EditServiceAdapter(
    private var mDataList: ArrayList<CategoryModel>,
    private var mListener: OnServiceItemClickListener?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return SelectServiceViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_edit_personal_service, parent, false
            )
        )
    }

    override fun getItemCount(): Int = mDataList.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is SelectServiceViewHolder) {
            with(mDataList[position]) {
                holder.mBinding.service = this
                holder.mBinding.tvPrice.text = "${Config.RUPEES_SYMBOL} $price"
                holder.mBinding.tvRequestStatus.text =
                    "Requested price : ${Config.RUPEES_SYMBOL} $requestedPrice\n" +
                            holder.itemView.context.getString(R.string.text_waiting_for_approval)
            }
        }
    }

    inner class SelectServiceViewHolder(itemView: ItemEditPersonalServiceBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        val mBinding = itemView

        init {
            mBinding.tvPrice.setOnClickListener {
                mListener?.onClick(mDataList[adapterPosition])
            }
        }
    }

    interface OnServiceItemClickListener {
        fun onClick(categoryModel: CategoryModel)
    }
}