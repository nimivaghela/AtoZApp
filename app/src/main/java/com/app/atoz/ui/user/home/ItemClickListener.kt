package com.app.atoz.ui.user.home

import android.view.View

interface ItemClickListener {

    fun onItemClick(view: View, position: Int, data: Any?)
}