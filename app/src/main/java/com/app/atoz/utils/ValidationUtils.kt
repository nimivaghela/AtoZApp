package com.app.atoz.utils

object ValidationUtils {

    fun doesEmailValid(emailId: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(emailId).matches()
    }

    fun doesMobileNumberValid(phone: String): Boolean {
        return android.util.Patterns.PHONE.matcher(phone).matches() && phone.length == 10 &&
                !phone.startsWith("+91") && !phone.startsWith("+")
    }

    fun doesPasswordLengthValid(password: String): Boolean {
        return password.length >= 8
    }

    fun doesOldPasswordAndNewPasswordSame(oldPassword: String, newPassword: String) = oldPassword == newPassword

    fun doesNewPasswordAndConfirmPasswordSame(newPassword: String, confirmPassword: String) =
        newPassword == confirmPassword

    fun isNotEmpty(text: String?): Boolean = !text.isNullOrBlank()
}