package com.app.atoz.ui.user.summary

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.atoz.R
import com.app.atoz.common.extentions.removeUnnecessaryDecimalPoint
import com.app.atoz.databinding.ItemSummaryBinding
import com.app.atoz.db.database.AppDatabase
import com.app.atoz.models.ActiveSubscriptionPlan
import com.app.atoz.models.RequestItemList
import com.app.atoz.utils.Config

class SummaryAdapter(
    private val mDataList: ArrayList<RequestItemList>,
    private val mDate: String,
    private val mTime: String,
    private val mSubCategoryId: String
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return SummaryViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_summary,
                parent, false
            )
        )
    }

    override fun getItemCount(): Int = mDataList.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is SummaryViewHolder) {
            with(mDataList[position]) {
                holder.mBinding.let {
                    it.productItem = this
                    it.tvDateValue.text = mDate
                    it.tvTimeValue.text = mTime
                    it.tvPriceValue.text = "${Config.RUPEES_SYMBOL} $servicePrice"

                    val activeSubscriptionPlan: List<ActiveSubscriptionPlan>? =
                        AppDatabase.getInstance(holder.itemView.context.applicationContext).activeSubscriptionPlanDao()
                            .getActiveSubscription()

                    if (activeSubscriptionPlan != null && activeSubscriptionPlan.isNotEmpty()
                        && activeSubscriptionPlan[0].serviceIds.contains(mSubCategoryId)
                    ) {
                        holder.mBinding.tvPriceValue.visibility = View.GONE
                        holder.mBinding.tvTotalAmountOriginalValue.visibility = View.VISIBLE
                        holder.mBinding.tvTotalAmountValue.visibility = View.VISIBLE

                        holder.mBinding.tvTotalAmountOriginalValue.text =
                            "${Config.RUPEES_SYMBOL} ${String.format(
                                "%.2f",
                                mDataList[position].servicePrice
                            ).removeUnnecessaryDecimalPoint()}"
                        val subscriptionPrice: Float =
                            (mDataList[position].servicePrice * activeSubscriptionPlan[0].discount / 100)
                        val totalPrice = mDataList[position].servicePrice - subscriptionPrice
                        holder.mBinding.tvTotalAmountValue.text =
                            "${Config.RUPEES_SYMBOL} ${String.format(
                                "%.2f",
                                totalPrice
                            ).removeUnnecessaryDecimalPoint()}"
                    } else {
                        holder.mBinding.tvPriceValue.visibility = View.VISIBLE
                        holder.mBinding.tvTotalAmountOriginalValue.visibility = View.GONE
                        holder.mBinding.tvTotalAmountValue.visibility = View.GONE
                        holder.mBinding.tvPriceValue.text =
                            "${Config.RUPEES_SYMBOL} ${String.format(
                                "%.2f",
                                mDataList[position].servicePrice
                            ).removeUnnecessaryDecimalPoint()}"
                    }
                }
            }
        }
    }

    inner class SummaryViewHolder(itemView: ItemSummaryBinding) : RecyclerView.ViewHolder(itemView.root) {
        val mBinding = itemView
    }
}