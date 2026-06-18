package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "class_representatives")
data class ClassRepresentative(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val passcode: String,
    val department: String
)
