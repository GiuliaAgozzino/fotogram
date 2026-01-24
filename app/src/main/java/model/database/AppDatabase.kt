package model.database

import androidx.room.Database
import androidx.room.RoomDatabase
import model.PostDao
import model.PostEntity
import model.UserEntity
import model.UserDao

@Database(entities = [UserEntity::class, PostEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao
}