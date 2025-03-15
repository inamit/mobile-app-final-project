package com.group147.appartmentblog.database

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

interface IDao<T> {
    @Insert
    fun insert(entity: T)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(entity: T)

    @Delete
    fun delete(entity: T)
}