package com.ruma.repnote.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ruma.repnote.core.domain.model.Routine
import com.ruma.repnote.core.domain.model.RoutineExercise

/**
 * Room entity for caching routines locally.
 *
 * @property id Routine ID (primary key)
 * @property userId User ID who owns this routine
 * @property name Routine name
 * @property description Optional routine description
 * @property exercises List of exercises in this routine (stored as encoded string)
 * @property createdAt Creation timestamp
 * @property updatedAt Last update timestamp
 * @property cachedAt Timestamp when this was cached (for cache invalidation)
 */
@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val name: String,
    val description: String?,
    val exercises: List<RoutineExercise>,
    val createdAt: Long,
    val updatedAt: Long,
    val cachedAt: Long = System.currentTimeMillis(),
)

/**
 * Converts RoutineEntity to domain Routine model
 */
fun RoutineEntity.toRoutine(): Routine =
    Routine(
        id = id,
        userId = userId,
        name = name,
        description = description,
        exercises = exercises,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

/**
 * Converts domain Routine to RoutineEntity
 */
fun Routine.toEntity(): RoutineEntity =
    RoutineEntity(
        id = id,
        userId = userId,
        name = name,
        description = description,
        exercises = exercises,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
