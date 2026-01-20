package com.example.barriovivo.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.barriovivo.data.database.dao.*
import com.example.barriovivo.data.database.entity.*

@Database(
    entities = [
        UserEntity::class,
        MealPostEntity::class,
        NotificationEntity::class,
        AdminEntity::class,
        ChatConversationEntity::class,
        ChatMessageEntity::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(DateTimeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun mealPostDao(): MealPostDao
    abstract fun notificationDao(): NotificationDao
    abstract fun adminDao(): AdminDao
    abstract fun chatConversationDao(): ChatConversationDao
    abstract fun chatMessageDao(): ChatMessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Agregar nuevas columnas a meal_posts
                database.execSQL("ALTER TABLE meal_posts ADD COLUMN isAvailable INTEGER NOT NULL DEFAULT 1")
                database.execSQL("ALTER TABLE meal_posts ADD COLUMN claimedByUserId TEXT")
                database.execSQL("ALTER TABLE meal_posts ADD COLUMN claimedAt TEXT")

                // Renombrar columna photoUri a photoUris y migrar datos
                database.execSQL("ALTER TABLE meal_posts RENAME COLUMN photoUri TO photoUris")

                // Crear tabla de conversaciones de chat
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS chat_conversations (
                        id TEXT PRIMARY KEY NOT NULL,
                        mealPostId TEXT NOT NULL,
                        creatorUserId TEXT NOT NULL,
                        claimerUserId TEXT NOT NULL,
                        createdAt TEXT NOT NULL,
                        lastMessageAt TEXT NOT NULL,
                        isActive INTEGER NOT NULL DEFAULT 1,
                        closedAt TEXT,
                        unreadCountCreator INTEGER NOT NULL DEFAULT 0,
                        unreadCountClaimer INTEGER NOT NULL DEFAULT 0
                    )
                """)

                // Crear tabla de mensajes de chat
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS chat_messages (
                        id TEXT PRIMARY KEY NOT NULL,
                        conversationId TEXT NOT NULL,
                        senderId TEXT NOT NULL,
                        senderName TEXT NOT NULL,
                        message TEXT NOT NULL,
                        sentAt TEXT NOT NULL,
                        isRead INTEGER NOT NULL DEFAULT 0
                    )
                """)
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Agregar campos de reporte a meal_posts
                database.execSQL("ALTER TABLE meal_posts ADD COLUMN reportCount INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE meal_posts ADD COLUMN reportedByUsers TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE meal_posts ADD COLUMN lastReportReason TEXT NOT NULL DEFAULT ''")

                // Actualizar posts PENDING y APPROVED a ACTIVE
                database.execSQL("UPDATE meal_posts SET status = 'ACTIVE' WHERE status = 'PENDING' OR status = 'APPROVED'")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Agregar campo name a users
                database.execSQL("ALTER TABLE users ADD COLUMN name TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Agregar campos para nombres de usuarios y título de comida en chat_conversations
                database.execSQL("ALTER TABLE chat_conversations ADD COLUMN mealPostTitle TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE chat_conversations ADD COLUMN creatorUserName TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE chat_conversations ADD COLUMN claimerUserName TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE chat_conversations ADD COLUMN lastMessage TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Añadir columnas para soportar media en mensajes de chat
                database.execSQL("ALTER TABLE chat_messages ADD COLUMN mediaUri TEXT")
                // messageType debe tener un valor por defecto
                database.execSQL("ALTER TABLE chat_messages ADD COLUMN messageType TEXT NOT NULL DEFAULT 'TEXT'")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "barriovivo_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    .fallbackToDestructiveMigration() // Solo para desarrollo
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
