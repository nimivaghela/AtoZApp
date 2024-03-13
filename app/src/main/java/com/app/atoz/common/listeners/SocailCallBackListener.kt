package com.app.atoz.common.listeners

interface SocailCallBackListener {

    fun onSocialSuccess(socialType:Int,statusCode:Int,data:Any)
    fun onSocialCancel(socialType:Int,statusCode:Int,msg:String)
    fun onSocialFailed(socialType:Int,statusCode:Int,errorMsg:String)
    fun signout(success: Boolean)
}