package com.example.safesound
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
@Database(entities = [PlayListEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlayListDao

    // Singleton para evitar múltiples instancias de la base de datos
    companion object {
        // Volatile para hacer visible la instancia inmediatamente a otros hilos
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context.applicationContext,
                AppDatabase::class.java, "YourDatabaseName.db")
                .fallbackToDestructiveMigration()
                .build()
    }
}