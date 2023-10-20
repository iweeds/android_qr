package com.example.myapplication.daos

import androidx.room.Insert
import androidx.room.Query
import com.example.myapplication.Point

interface PointDao {
    @Insert
    suspend fun insertSubscriber(point: Point)

    @Query("SELECT * FROM point_table")
    fun getAll() : List<Point>
}