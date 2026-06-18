package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val idNo: String, // e.g., "q251064" or similar registration number
    val email: String,             // e.g., "q251064@ugrad.iiuc.ac.bd"
    val password: String           // user passcode or password
)
