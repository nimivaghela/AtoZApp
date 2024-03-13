package com.app.atoz.ui.user.postrequest

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.atoz.R
import com.app.atoz.databinding.ItemOrderUploadPicBinding
import com.app.atoz.models.PostRequestUploadImageModel
import timber.log.Timber

class PostRequestUploadImageAdapter(
    private var mDataList: ArrayList<PostRequestUploadImageModel>,
    private var mListener: OnItemClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_UPLOADED_VIEW = 1
        const val TYPE_UPLOAD_PIC = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_UPLOAD_PIC -> {
                UploadImageViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_order_upload_pic,
                        parent,
                        false
                    )
                )
            }
            else -> {
                UploadedImageViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_order_uploaded_pic,
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
        if (holder is UploadedImageViewHolder) {
            holder.mBinding.ibRemoveImage.visibility = View.VISIBLE
            holder.mBinding.ivUploadedImage.setImageURI(Uri.fromFile(mDataList[position].file))
        }
    }

    inner class UploadedImageViewHolder(itemView: com.app.atoz.databinding.ItemOrderUploadedPicBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        val mBinding = itemView

        init {
            mBinding.ibRemoveImage.setOnClickListener {
                mListener.onRemove(adapterPosition)
            }
        }
    }

    inner class UploadImageViewHolder(itemView: ItemOrderUploadPicBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        val mBinding = itemView
        init {
            mBinding.ibUploadButton.setOnClickListener {
                Timber.d("Onclick of upload pic")
                mListener.onAddItem()
            }
        }
    }

    interface OnItemClickListener {
        fun onRemove(position: Int)

        fun onAddItem()
    }
}