package com.ruma.repnote.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ruma.repnote.core.database.dao.ExerciseDao
import com.ruma.repnote.core.database.dao.RoutineDao
import com.ruma.repnote.core.database.dao.WorkoutSessionDao
import com.ruma.repnote.core.database.entity.ExerciseEntity
import com.ruma.repnote.core.database.entity.RoutineEntity
import com.ruma.repnote.core.database.entity.WorkoutSessionEntity
import com.ruma.repnote.core.database.converter.TypeConverters as RepnoteTypeConverters

/**
 * Main Room database for Repnote app.
 *
 * Provides local caching for exercises, routines, and workout sessions
 * to improve performance and enable offline functionality.
 */
@Database(
    entities = [
        ExerciseEntity::class,
        RoutineEntity::class,
        WorkoutSessionEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
@TypeConverters(RepnoteTypeConverters::class)
abstract class RepnoteDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao

    abstract fun routineDao(): RoutineDao

    abstract fun workoutSessionDao(): WorkoutSessionDao

    companion object {
        const val DATABASE_NAME = "repnote_database"
    }
}
