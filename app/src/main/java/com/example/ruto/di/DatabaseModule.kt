package com.example.ruto.di

import android.content.Context
import androidx.room.Room
import com.example.ruto.data.local.AppDatabase
import com.example.ruto.data.local.MIGRATION_1_2
import com.example.ruto.data.local.RoutineCompletionDao
import com.example.ruto.data.local.complete.PendingCompleteDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDb(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "app.db")
            .addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigrationOnDowngrade(false)
            .build()

    @Provides
    @Singleton
    fun providePendingDao(db: AppDatabase): PendingCompleteDao =
        db.pendingCompleteDao()

    @Provides @Singleton
    fun provideRoutineCompletionDao(db: AppDatabase): RoutineCompletionDao =
        db.routineCompletionDao()
}