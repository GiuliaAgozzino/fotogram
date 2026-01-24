package model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PostDao {

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity)

    @Query("SELECT id FROM PostEntity")
    suspend fun getAllPostIds(): List<Int>

    @Query("SELECT * FROM PostEntity WHERE id = :id")
    suspend fun getPostById(id: Int): PostEntity?

    // prendo tutti i post che hanno id nella lista postIds
    @Query("SELECT * FROM PostEntity WHERE id IN (:postIds)")
    suspend fun getPostsByIds(postIds: List<Int>): List<PostEntity>

    @Query("SELECT * FROM PostEntity WHERE authorId = :authorId ORDER BY createdAt DESC")
    suspend fun getPostsByAuthorId(authorId: Int): List<PostEntity>

    @Query("DELETE FROM PostEntity")
    suspend fun clearPost()

}