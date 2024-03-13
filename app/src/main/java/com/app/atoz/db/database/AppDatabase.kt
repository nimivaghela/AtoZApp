package com.app.atoz.db.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.app.atoz.db.Converters
import com.app.atoz.db.dao.ActiveSubscriptionPlanDao
import com.app.atoz.db.dao.CityStateDao
import com.app.atoz.db.dao.PaymentDueDao
import com.app.atoz.models.ActiveSubscriptionPlan
import com.app.atoz.models.CitiesItem
import com.app.atoz.models.PaymentDue

@Database(entities = [CitiesItem::class, PaymentDue::class, ActiveSubscriptionPlan::class], version = 4)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    companion object {
        private const val DB_NAME = "atoz.db"
        private var instance: AppDatabase? = null
        private val lock = Any()
        fun getInstance(context: Context): AppDatabase {
            if (instance == null) {
                synchronized(lock) {
                    instance = Room
                        .databaseBuilder(
                            context.applicationContext,
                            AppDatabase::class.java,
                            DB_NAME
                        )
                        .allowMainThreadQueries()
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return instance as AppDatabase
        }
    }

    abstract fun cityStateDao(): CityStateDao

    abstract fun paymentDueDao(): PaymentDueDao

    abstract fun activeSubscriptionPlanDao(): ActiveSubscriptionPlanDao
}