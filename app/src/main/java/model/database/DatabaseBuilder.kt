package model.database

import android.content.Context
import androidx.room.Room

object DatabaseBuilder {
    fun getInstance(context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "myfotogramapp-db"
        )//.fallbackToDestructiveMigration()
            .build()
    }
}