package com.app.atoz.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.app.atoz.models.CitiesItem

@Dao
abstract class CityStateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertAll(cityState: List<CitiesItem>)

    @Query("SELECT * FROM CitiesItem")
    abstract fun getAllCities(): List<CitiesItem>
}