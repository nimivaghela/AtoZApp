package com.app.atoz.ui.user.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.atoz.R
import com.app.atoz.common.helper.BaseViewHolder
import com.app.atoz.common.helper.RxHelper
import com.app.atoz.databinding.ItemAdvertisementBinding
import com.app.atoz.databinding.ItemMoreSavingBinding
import com.app.atoz.databinding.ItemServiceBinding
import com.app.atoz.databinding.ItemTrendingServiceBinding
import com.app.atoz.models.homescreen.HomeModel
import com.app.atoz.models.homescreen.HomeModel.Companion.TYPE_ADVERTISEMENT
import com.app.atoz.models.homescreen.HomeModel.Companion.TYPE_SERVICE
import com.google.android.gms.ads.AdRequest
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber


class HomeAdapter(
    private val mDataList: ArrayList<HomeModel>,
    var mDisposable: CompositeDisposable,
    private val mListener: ItemClickListener?,
    private val mAdvertiseListener: AdvertiseAdapter.OnAdvertiseClickListener,
    private val mCouponCodeListener: MoreSavingAdapter.OnCouponCodeClickListener
) : RecyclerView.Adapter<BaseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            TYPE_SERVICE -> ServiceVH(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_service, parent, false)
            )
            TYPE_ADVERTISEMENT -> return AdvertisementViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_advertisement, parent, false)
            )
            else -> //coupon code
                MoreSavingVH(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_more_saving, parent, false)
                )
        }
//            3 -> TrendingServiceVH(
//                LayoutInflater.from(parent.context).inflate(
//                    R.layout.item_trending_service,
//                    parent,
//                    false
//                )
//            )
    }

    override fun onBindViewHolder(baseViewHolder: BaseViewHolder, position: Int) {

        mDataList[position].let {
            when (baseViewHolder) {
                is ServiceVH -> {
                    val binding = baseViewHolder.getBinding<ItemServiceBinding>()
                    //binding.serviceItem = homeData.serviceName
                    binding.serviceItem = it.serviceItem

                    binding.rvService.adapter =
                        it.serviceItem?.children?.let { it1 ->
                            ServiceAdapter(it1, mDisposable, mListener, it.serviceItem.id.toString())
                        }

                    binding.executePendingBindings()
                }
                /*  is TrendingServiceVH -> {
                      val binding = baseViewHolder.getBinding<ItemTrendingServiceBinding>()
                      binding.serviceName = homeData.serviceName

                      binding.rvService.adapter =
                          TrendingServiceAdapter(homeData.lstService)
                  }*/
                is MoreSavingVH -> {
                    val binding = baseViewHolder.getBinding<ItemMoreSavingBinding>()

                    binding.tvServiceName.visibility =
                        if (it.couponList != null && it.couponList.size == 0) View.GONE else View.VISIBLE
                    binding.rvMoreSaving.adapter =
                        it.couponList?.let { it1 -> MoreSavingAdapter(it1, mCouponCodeListener) }
                }

                is AdvertisementViewHolder -> {
                    val binding = baseViewHolder.getBinding<ItemAdvertisementBinding>()
                    if (it.advertisement != null
                        && !it.advertisement.isGoogleEnable
                        && it.advertisement.advertisementList != null
                        && it.advertisement.advertisementList.size != 0
                    ) {
                        binding.rvAdvertisement.visibility = View.VISIBLE
                        binding.adView.visibility = View.GONE
                        binding.rvAdvertisement.adapter =
                            AdvertiseAdapter(it.advertisement.advertisementList, mAdvertiseListener)
                    } else {
                        binding.rvAdvertisement.visibility = View.GONE
                        binding.adView.visibility = View.VISIBLE
                        val adRequest = AdRequest.Builder().build()
                        binding.adView.loadAd(adRequest)
                    }
                }
            }
        }
    }


    override fun getItemViewType(position: Int): Int {
        super.getItemViewType(position)
        return mDataList[position].viewType
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    inner class ServiceVH internal constructor(itemView: View) : BaseViewHolder(itemView) {
        init {
            val itemServiceBinding: ItemServiceBinding = getBinding()

            RxHelper.onClick(itemServiceBinding.tvViewAll, mDisposable)
            {
                mDataList.getOrNull(adapterPosition)?.let {
                    mListener?.onItemClick(itemServiceBinding.tvViewAll, adapterPosition, it.serviceItem)
                } ?: let {
                    Timber.d("ViewAll Listener data is null")
                }

            }
        }
    }

    inner class TrendingServiceVH internal constructor(itemView: View) : BaseViewHolder(itemView) {
        init {
            val itemServiceBinding: ItemTrendingServiceBinding = getBinding()
            RxHelper.onClick(itemServiceBinding.tvViewAll, mDisposable)
            {
                mDataList.getOrNull(adapterPosition)?.let {
                    mListener?.onItemClick(itemServiceBinding.tvViewAll, adapterPosition, it)
                }
            }
        }
    }

    inner class MoreSavingVH internal constructor(itemView: View) : BaseViewHolder(itemView) {
        init {
            val itemServiceBinding: ItemMoreSavingBinding = getBinding()

            RxHelper.onClick(itemServiceBinding.tvViewAll, mDisposable)
            {
                mDataList.getOrNull(adapterPosition)?.let {
                    mListener?.onItemClick(itemServiceBinding.tvViewAll, adapterPosition, it)
                }
            }
        }
    }

    inner class AdvertisementViewHolder(itemView: View) : BaseViewHolder(itemView)
}