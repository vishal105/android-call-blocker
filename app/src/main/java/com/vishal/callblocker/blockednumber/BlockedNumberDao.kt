package com.vishal.callblocker.blockednumber

import android.arch.persistence.room.*

@Dao
interface BlockedNumberDao {
    @get:Query("SELECT * FROM blockednumbers")
    val all: List<BlockedNumber>

    @Insert
    fun insert(number: BlockedNumber)

    @Delete
    fun delete(number: BlockedNumber)

    @Update
    fun updateGender(number: BlockedNumber)
}
