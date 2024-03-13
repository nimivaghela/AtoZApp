package com.app.atoz.models

import com.google.gson.annotations.SerializedName

data class CityModel(

	@field:SerializedName("cities")
	val cities: MutableList<CitiesItem>? = null
)