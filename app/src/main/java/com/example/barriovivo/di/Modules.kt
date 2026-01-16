package com.example.barriovivo.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

import com.example.barriovivo.data.database.AppDatabase
import com.example.barriovivo.data.database.dao.UserDao
import com.example.barriovivo.data.database.dao.MealPostDao
import com.example.barriovivo.data.database.dao.NotificationDao
import com.example.barriovivo.data.database.dao.AdminDao
import com.example.barriovivo.data.database.dao.ChatConversationDao
import com.example.barriovivo.data.database.dao.ChatMessageDao

private const val USER_PREFERENCES = "user_preferences"

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = USER_PREFERENCES)

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Singleton
    @Provides
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        // Reuse the companion getDatabase to ensure singleton Room instance
        return AppDatabase.getDatabase(context)
    }

    @Singleton
    @Provides
    fun provideUserDao(appDatabase: AppDatabase): UserDao {
        return appDatabase.userDao()
    }

    @Singleton
    @Provides
    fun provideMealPostDao(appDatabase: AppDatabase): MealPostDao {
        return appDatabase.mealPostDao()
    }

    @Singleton
    @Provides
    fun provideNotificationDao(appDatabase: AppDatabase): NotificationDao {
        return appDatabase.notificationDao()
    }

    @Singleton
    @Provides
    fun provideAdminDao(appDatabase: AppDatabase): AdminDao {
        return appDatabase.adminDao()
    }

    @Singleton
    @Provides
    fun provideChatConversationDao(appDatabase: AppDatabase): ChatConversationDao {
        return appDatabase.chatConversationDao()
    }

    @Singleton
    @Provides
    fun provideChatMessageDao(appDatabase: AppDatabase): ChatMessageDao {
        return appDatabase.chatMessageDao()
    }
}
