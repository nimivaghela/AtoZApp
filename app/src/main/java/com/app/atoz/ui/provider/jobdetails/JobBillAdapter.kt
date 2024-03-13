package com.app.atoz.ui.provider.jobdetails

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.atoz.R
import com.app.atoz.databinding.ItemAddBillBinding
import com.app.atoz.databinding.ItemBillImageBinding
import com.app.atoz.models.JobBillModel
import java.io.File

class JobBillAdapter(
    private var mDataList: ArrayList<JobBillModel>,
    private var mListener: OnItemClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_VIEW_VIEW = 1
        const val TYPE_ADD_BILL = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_ADD_BILL -> {
                JobAddBillViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_add_bill,
                        parent,
                        false
                    )
                )
            }
            else -> {
                JobBillViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_bill_image,
                        parent,
                        false
                    )
                )
            }
        }
    }

    override fun getItemCount(): Int = mDataList.size

    override fun getItemViewType(position: Int): Int = mDataList[position].itemType

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is JobBillViewHolder) {
            holder.mBinding.ibRemoveImage.visibility = View.VISIBLE
            holder.mBinding.ivBillImage.setImageURI(Uri.fromFile(mDataList[position].file))
        }
    }

    inner class JobBillViewHolder(itemView: ItemBillImageBinding) : RecyclerView.ViewHolder(itemView.root) {
        val mBinding = itemView

        init {
            itemView.root.setOnClickListener {
                mDataList[adapterPosition].file?.let {
                    mListener.showFileImageInFullScreen(it)
                }
            }

            itemView.ibRemoveImage.setOnClickListener {
                mListener.onRemove(adapterPosition)
            }
        }
    }

    inner class JobAddBillViewHolder(itemView: ItemAddBillBinding) : RecyclerView.ViewHolder(itemView.root) {
        init {
            itemView.cvAddBillBg.setOnClickListener {
                mListener.onAddItem()
            }
        }
    }

    interface OnItemClickListener {
        fun onRemove(position: Int)

        fun onAddItem()

        fun showFileImageInFullScreen(file: File)
    }
}