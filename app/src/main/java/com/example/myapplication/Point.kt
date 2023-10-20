package com.example.myapplication

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "point_table")
data class Point (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id : Int,
    @ColumnInfo(name = "type")
    val type: String,
    @ColumnInfo(name = "reward")
    val reward: String,
    @ColumnInfo(name = "earn")
    val earn: String
)
//  {"type":"nfc","reward":"point","earn":"5"}