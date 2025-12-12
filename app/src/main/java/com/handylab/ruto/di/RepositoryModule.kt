package com.handylab.ruto.di

import android.os.Build
import androidx.annotation.RequiresApi
import com.handylab.ruto.data.profile.ProfileRepositoryImpl
import com.handylab.ruto.data.routine.RoutineRepositoryImpl
import com.handylab.ruto.data.routine.RoutineStatisticsRepositoryImpl
import com.handylab.ruto.domain.profile.ProfileRepository
import com.handylab.ruto.domain.routine.RoutineRepository
import com.handylab.ruto.domain.routine.RoutineStatisticsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository

    @Binds
    @Singleton
    @RequiresApi(Build.VERSION_CODES.O)
    abstract fun bindRoutineRepository(impl: RoutineRepositoryImpl): RoutineRepository

    @Binds
    @Singleton
    @RequiresApi(Build.VERSION_CODES.O)
    abstract fun bindRoutineStatisticsRepository(
        impl: RoutineStatisticsRepositoryImpl
    ): RoutineStatisticsRepository
}
