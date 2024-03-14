package com.marshmallow

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [LikeEntity::class],
    version = 1,
    exportSchema = false)
@TypeConverters(OrmConverter::class)
abstract class LikeDatabase :RoomDatabase(){
    abstract  fun likeDao():LikeDao
    companion object{
        private var instance: LikeDatabase? = null
        @Synchronized
        fun getInstance(context:Context):LikeDatabase?{
            if(instance == null){
                synchronized(LikeDatabase::class){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        LikeDatabase::class.java,
                        "like-database.db"
                    )
                        .allowMainThreadQueries()
                        .build()
                }

            }
            return instance
        }
    }
}