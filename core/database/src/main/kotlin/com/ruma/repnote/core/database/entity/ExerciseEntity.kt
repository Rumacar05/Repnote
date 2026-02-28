package com.ruma.repnote.core.database.entity

import androidx.room.Entity
import com.ruma.repnote.core.domain.model.Exercise
import com.ruma.repnote.core.domain.model.MuscleGroup

/**
 * Room entity for caching exercises locally.
 *
 * @property id Exercise ID (part of composite primary key)
 * @property name Exercise name (localized)
 * @property description Exercise description (localized)
 * @property imageUrl Optional image URL
 * @property primaryMuscleGroup Primary muscle group targeted
 * @property secondaryMuscleGroups Secondary muscle groups (stored as comma-separated string)
 * @property isGlobal Whether this is a global exercise or custom
 * @property createdBy User ID who created this (null for global exercises)
 * @property language Language code for this cached translation (part of composite primary key)
 * @property cachedAt Timestamp when this was cached (for cache invalidation)
 */
@Entity(
    tableName = "exercises",
    primaryKeys = ["id", "language"],
)
data class ExerciseEntity(
    val id: String,
    val name: String,
    val description: String,
    val imageUrl: String?,
    val primaryMuscleGroup: MuscleGroup,
    val secondaryMuscleGroups: List<MuscleGroup>,
    val isGlobal: Boolean,
    val createdBy: String?,
    val language: String,
    val cachedAt: Long = System.currentTimeMillis(),
)

/**
 * Converts ExerciseEntity to domain Exercise model
 */
fun ExerciseEntity.toExercise(): Exercise =
    Exercise(
        id = id,
        name = name,
        description = description,
        imageUrl = imageUrl,
        primaryMuscleGroup = primaryMuscleGroup,
        secondaryMuscleGroups = secondaryMuscleGroups,
        isGlobal = isGlobal,
        createdBy = createdBy,
    )

/**
 * Converts domain Exercise to ExerciseEntity
 */
fun Exercise.toEntity(language: String): ExerciseEntity =
    ExerciseEntity(
        id = id,
        name = name,
        description = description,
        imageUrl = imageUrl,
        primaryMuscleGroup = primaryMuscleGroup,
        secondaryMuscleGroups = secondaryMuscleGroups,
        isGlobal = isGlobal,
        createdBy = createdBy,
        language = language,
    )
