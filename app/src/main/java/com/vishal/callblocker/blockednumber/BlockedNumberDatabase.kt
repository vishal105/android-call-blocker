package com.vishal.callblocker.blockednumber

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.content.Context

@Database(entities = [BlockedNumber::class], version = 1)
@TypeConverters(BlockNumberTypeConvertor::class)
abstract class BlockedNumberDatabase : RoomDatabase() {

    abstract fun blockedNumberDao(): BlockedNumberDao

    companion object {
        private val DB_NAME = "blockednumbers.db"

        @Volatile
        private var INSTANCE: BlockedNumberDatabase? = null

        fun getInstance(context: Context): BlockedNumberDatabase? {
            if (INSTANCE == null) {
                synchronized(BlockedNumberDatabase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(context,
                                BlockedNumberDatabase::class.java, DB_NAME)
                                .build()
                    }
                }
            }
            return INSTANCE
        }
    }
}
