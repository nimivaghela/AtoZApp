package com.app.atoz.common.extentions

fun String.removeUnnecessaryDecimalPoint(): String {
    return if (this.contains(".00")) {
        this.split(".")[0]
    } else this
}