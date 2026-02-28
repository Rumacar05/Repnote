package com.ruma.repnote.core.database.di

import androidx.room.Room
import com.ruma.repnote.core.database.RepnoteDatabase
import com.ruma.repnote.core.database.dao.ExerciseDao
import com.ruma.repnote.core.database.dao.RoutineDao
import com.ruma.repnote.core.database.dao.WorkoutSessionDao
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule =
    module {
        single<RepnoteDatabase> {
            Room
                .databaseBuilder(
                    androidContext(),
                    RepnoteDatabase::class.java,
                    RepnoteDatabase.DATABASE_NAME,
                ).fallbackToDestructiveMigration(false)
                .build()
        }

        single<ExerciseDao> { get<RepnoteDatabase>().exerciseDao() }
        single<RoutineDao> { get<RepnoteDatabase>().routineDao() }
        single<WorkoutSessionDao> { get<RepnoteDatabase>().workoutSessionDao() }
    }
