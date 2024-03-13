package com.app.atoz.models

data class HomeData(
    var serviceName: String,
    var lstService: List<ServiceItemModel>
)

data class ServiceItemModel(
        var serviceCategory: String,
        var serviceImage: Int
)
