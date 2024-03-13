package com.app.atoz.ui.auth.verification

interface VerificationNavigator {
    fun onRequestFocus(position: Int)
    fun onStatusChange(status:Int,msg:Any)
    fun fillOTPRuntime()
}