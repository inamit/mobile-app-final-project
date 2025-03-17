package com.group147.appartmentblog.database.post

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.group147.appartmentblog.base.Collections
import com.group147.appartmentblog.model.Post
import com.group147.appartmentblog.util.converter.GeoPointConverter

@Database(entities = [Post::class], version = 2, exportSchema = false)
@TypeConverters(GeoPointConverter::class)
abstract class PostDatabase : RoomDatabase() {
    abstract fun postDao(): PostDao

    companion object {
        @Volatile
        private var INSTANCE: PostDatabase? = null

        fun getDatabase(context: Context): PostDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PostDatabase::class.java,
                    Collections.POSTS.collectionName
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}