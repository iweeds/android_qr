package com.example.myapplication.repo

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.myapplication.Point
import com.example.myapplication.daos.PointDao

@Database(entities = [Point::class], version = 1)
abstract class PointRepo : RoomDatabase() {
    abstract val pointDao: PointDao

    companion object {
        @Volatile
        private var INSTANCE: PointRepo? = null
        fun getInstance(context: Context): PointRepo {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        PointRepo::class.java,
                        "point_database"
                    ).build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}