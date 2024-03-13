package com.app.atoz.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class CitiesItem(
    @ColumnInfo(name = "city_state")
    @field:SerializedName("city_state")
    val cityState: String? = null,

    @ColumnInfo(name = "name")
    @field:SerializedName("name")
    val name: String? = null,

    @ColumnInfo(name = "city_state_id")
    @field:SerializedName("city_state_id")
    val cityStateId: String? = null,

    @PrimaryKey
    @field:SerializedName("id")
    val id: Int,

    @ColumnInfo(name = "state_id")
    @field:SerializedName("state_id")
    val stateId: Int? = null
) {
    override fun toString(): String {
        return cityState.toString()
    }
}