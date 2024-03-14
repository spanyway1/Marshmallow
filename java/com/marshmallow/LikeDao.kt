package com.marshmallow
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LikeDao {
    @Query("SELECT * FROM LikeEntity")
    fun getLikes() : List<LikeEntity>

    @Query("DELETE FROM LikeEntity")
    fun deleteLikes()

    @Query("DELETE FROM LikeEntity WHERE id = :id")
    fun deleteLikeById(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLike(likeEntity: LikeEntity)
}