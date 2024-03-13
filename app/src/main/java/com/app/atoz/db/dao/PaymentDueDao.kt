package com.app.atoz.db.dao

import androidx.room.*
import com.app.atoz.models.PaymentDue

@Dao
abstract class PaymentDueDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertPaymentDue(paymentDue: PaymentDue)

    @Query("SELECT * FROM PaymentDue")
    abstract fun getAllPaymentDue(): List<PaymentDue>

    @Delete
    abstract fun deletePaymentDue(paymentDue: PaymentDue)

    @Query("DELETE FROM PaymentDue")
    abstract fun deleteAllData()
}