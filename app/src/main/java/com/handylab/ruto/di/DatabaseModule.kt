package com.handylab.ruto.di

import android.content.Context
import androidx.room.Room
import com.handylab.ruto.data.local.AppDatabase
import com.handylab.ruto.data.local.MIGRATION_1_2
import com.handylab.ruto.data.local.RoutineCompletionDao
import com.handylab.ruto.data.local.complete.PendingCompleteDao
import com.handylab.ruto.data.local.routine.RoutineDao
import com.handylab.ruto.data.local.statistics.StatisticsDao
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

    @Provides
    @Singleton
    fun provideRoutineCompletionDao(db: AppDatabase): RoutineCompletionDao =
        db.routineCompletionDao()

    @Provides
    @Singleton
    fun provideStatisticsDao(db: AppDatabase): StatisticsDao =
        db.statisticsDao()

    @Provides
    @Singleton
    fun provideRoutineDao(db: AppDatabase): RoutineDao =
        db.routineDao()
}