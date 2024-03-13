package com.app.atoz.ui.provider.ratecustomer

import android.annotation.SuppressLint
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.atoz.R
import com.app.atoz.common.helper.BaseViewHolder
import com.app.atoz.databinding.ItemCommentsBinding
import com.app.atoz.models.UserRatingModel


class CommentAdapter(
    private val mDataList: ArrayList<UserRatingModel>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return CommentViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                com.app.atoz.R.layout.item_comments,
                parent,
                false
            )
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is CommentViewHolder) {
            with(mDataList[position]) {
                holder.mBinding.ratingModel = this
                holder.mBinding.tvComments.movementMethod = ScrollingMovementMethod()
                holder.mBinding.tvComments.setOnTouchListener { v, event ->
                    if (event != null) {
                        if (v?.id == R.id.tv_comments && holder.mBinding.tvComments.lineCount > 3) {
                            v.parent.requestDisallowInterceptTouchEvent(true)
                            when (event.action and MotionEvent.ACTION_MASK) {
                                MotionEvent.ACTION_UP -> v.parent.requestDisallowInterceptTouchEvent(false)
                            }
                        }
                    }
                    false
                }

            }
        }
    }

    override fun getItemCount() = mDataList.size

    inner class CommentViewHolder internal constructor(itemView: ItemCommentsBinding) : BaseViewHolder(itemView.root) {
        val mBinding = itemView
    }
}
