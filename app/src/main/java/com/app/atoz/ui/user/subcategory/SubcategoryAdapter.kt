package com.app.atoz.ui.user.subcategory

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.atoz.R
import com.app.atoz.common.extentions.removeUnnecessaryDecimalPoint
import com.app.atoz.databinding.ItemViewAllSubcategoryBinding
import com.app.atoz.db.database.AppDatabase
import com.app.atoz.models.ActiveSubscriptionPlan
import com.app.atoz.models.CategoryModel
import com.app.atoz.utils.Config

class SubcategoryAdapter(
    val mDataList: ArrayList<CategoryModel>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return SubcategoryViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_view_all_subcategory,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = mDataList.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is SubcategoryViewHolder) {
            holder.mBinding.subcategory = mDataList[position]

            val activeSubscriptionPlan: List<ActiveSubscriptionPlan>? =
                AppDatabase.getInstance(holder.itemView.context.applicationContext).activeSubscriptionPlanDao()
                    .getActiveSubscription()

            if (activeSubscriptionPlan != null && activeSubscriptionPlan.isNotEmpty()
                && activeSubscriptionPlan[0].serviceIds.contains(mDataList[position].parentCategoryId)
            ) {
                holder.mBinding.tvPriceValue.visibility = View.GONE
                holder.mBinding.tvTotalAmountOriginalValue.visibility = View.VISIBLE
                holder.mBinding.tvTotalAmountValue.visibility = View.VISIBLE

                holder.mBinding.tvTotalAmountOriginalValue.text =
                    "${Config.RUPEES_SYMBOL} ${if (mDataList[position].price != null) String.format(
                        "%.2f",
                        mDataList[position].price!!.toFloat()
                    ).removeUnnecessaryDecimalPoint() else "-"}"

                if (mDataList[position].price != null) {
                    val subscriptionPrice: Float =
                        (mDataList[position].price!!.toFloat() * activeSubscriptionPlan[0].discount / 100)
                    val totalPrice = mDataList[position].price!!.toFloat() - subscriptionPrice
                    holder.mBinding.tvTotalAmountValue.text =
                        "${Config.RUPEES_SYMBOL} ${String.format("%.2f", totalPrice).removeUnnecessaryDecimalPoint()}"
                }
            } else {
                holder.mBinding.tvPriceValue.visibility = View.VISIBLE
                holder.mBinding.tvTotalAmountOriginalValue.visibility = View.GONE
                holder.mBinding.tvTotalAmountValue.visibility = View.GONE
                holder.mBinding.tvPriceValue.text =
                    "${Config.RUPEES_SYMBOL} ${if (mDataList[position].price != null) String.format(
                        "%.2f",
                        mDataList[position].price!!.toFloat()
                    ).removeUnnecessaryDecimalPoint() else "-"}"

            }
            holder.mBinding.executePendingBindings()
        }
    }

    inner class SubcategoryViewHolder(itemView: ItemViewAllSubcategoryBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        var mBinding: ItemViewAllSubcategoryBinding = itemView

        init {
            itemView.root.setOnClickListener {
                mDataList[adapterPosition].isChecked = !itemView.chbSubcategory.isChecked
                notifyItemChanged(adapterPosition)
            }
        }
    }
}