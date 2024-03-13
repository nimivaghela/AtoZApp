package com.app.atoz.ui.user.address

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.atoz.R
import com.app.atoz.databinding.ItemAddressBinding
import com.app.atoz.models.address.AddressListItem
import com.app.atoz.ui.user.home.ItemClickListener

class AddressPikerAdapter(val mListener: ItemClickListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_address,
                parent,
                false
            )
        )
    }

    private var mDataAddressList: MutableList<AddressListItem?>? = null
    fun setAddressList(dataAddressList: MutableList<AddressListItem?>) {
        mDataAddressList = dataAddressList
    }

    fun addData(dataAddressList: AddressListItem) {
        try {
            mDataAddressList?.add(dataAddressList)?.let {
                if (it) notifyItemInserted((mDataAddressList?.size ?: -1) - 1)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun removeData(dataAddressList: AddressListItem) {
        val index = mDataAddressList?.indexOf(dataAddressList)
        index?.let { mDataAddressList?.removeAt(it) }
        index?.let { notifyItemRemoved(it) }
    }

    override fun getItemCount(): Int = if (mDataAddressList == null) 0 else mDataAddressList!!.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            holder.mBinding.addressModel = mDataAddressList?.get(position)
            holder.mBinding.executePendingBindings()
            if (mDataAddressList?.size == 1) holder.mBinding.tvDelete.visibility = View.GONE else View.VISIBLE
        }
    }

    inner class ViewHolder(itemView: ItemAddressBinding) : RecyclerView.ViewHolder(itemView.root) {
        val mBinding: ItemAddressBinding = itemView

        init {
            itemView.root.setOnClickListener {
                mListener.onItemClick(it, adapterPosition, mDataAddressList?.get(adapterPosition))
            }
            itemView.tvDelete.setOnClickListener {
                mListener.onItemClick(it, adapterPosition, mDataAddressList?.get(adapterPosition))
            }
        }
    }


}