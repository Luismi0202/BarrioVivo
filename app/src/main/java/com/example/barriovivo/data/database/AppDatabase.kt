package com.example.barriovivo.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.barriovivo.data.database.dao.UserDao
import com.example.barriovivo.data.database.dao.MealPostDao
import com.example.barriovivo.data.database.dao.NotificationDao
import com.example.barriovivo.data.database.dao.AdminDao
import com.example.barriovivo.data.database.entity.UserEntity
import com.example.barriovivo.data.database.entity.MealPostEntity
import com.example.barriovivo.data.database.entity.NotificationEntity
import com.example.barriovivo.data.database.entity.AdminEntity

@Database(
    entities = [
        UserEntity::class,
        MealPostEntity::class,
        NotificationEntity::class,
        AdminEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateTimeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun mealPostDao(): MealPostDao
    abstract fun notificationDao(): NotificationDao
    abstract fun adminDao(): AdminDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "barriovivo_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

