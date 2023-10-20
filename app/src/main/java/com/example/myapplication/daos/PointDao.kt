package com.example.myapplication.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myapplication.Point

@Dao
interface PointDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPoint(vararg points: Point)

    @Query("SELECT * FROM point_table")
    fun getAll() : List<Point>
}