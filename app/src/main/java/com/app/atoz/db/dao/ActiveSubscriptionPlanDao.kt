package com.app.atoz.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.app.atoz.models.ActiveSubscriptionPlan

@Dao
abstract class ActiveSubscriptionPlanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(activeSubscriptionPlan: ActiveSubscriptionPlan)

    @Query("DELETE FROM ActiveSubscriptionPlan")
    abstract fun deleteAllData()

    @Query("select * from ActiveSubscriptionPlan")
    abstract fun getActiveSubscription(): List<ActiveSubscriptionPlan>
}