package com.ruma.repnote.core.data.mapper

import com.google.firebase.firestore.FirebaseFirestoreException
import com.ruma.repnote.core.data.model.CompletedSetDocument
import com.ruma.repnote.core.data.model.WorkoutExerciseDocument
import com.ruma.repnote.core.data.model.WorkoutSessionDocument
import com.ruma.repnote.core.domain.model.CompletedSet
import com.ruma.repnote.core.domain.model.WorkoutException
import com.ruma.repnote.core.domain.model.WorkoutExercise
import com.ruma.repnote.core.domain.model.WorkoutSession
import com.ruma.repnote.core.domain.model.WorkoutStatus

// Document -> Domain mappings

/**
 * Maps WorkoutSessionDocument to WorkoutSession domain model.
 */
internal fun WorkoutSessionDocument.toWorkoutSession(): WorkoutSession =
    WorkoutSession(
        id = id,
        userId = userId,
        routineId = routineId,
        routineName = routineName,
        status = status.toWorkoutStatus(),
        exercises = exercises.map { it.toWorkoutExercise() },
        startTime = startTime,
        endTime = endTime,
        totalDurationSeconds = totalDurationSeconds,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

/**
 * Maps string status to WorkoutStatus enum.
 */
private fun String.toWorkoutStatus(): WorkoutStatus =
    when (this) {
        "IN_PROGRESS" -> WorkoutStatus.IN_PROGRESS
        "COMPLETED" -> WorkoutStatus.COMPLETED
        "ABANDONED" -> WorkoutStatus.ABANDONED
        else -> WorkoutStatus.IN_PROGRESS // Default fallback
    }

/**
 * Maps WorkoutExerciseDocument to WorkoutExercise domain model.
 */
internal fun WorkoutExerciseDocument.toWorkoutExercise(): WorkoutExercise =
    WorkoutExercise(
        exerciseId = exerciseId,
        exerciseName = exerciseName,
        order = order,
        targetSets = targetSets,
        targetReps = targetReps,
        targetRestSeconds = targetRestSeconds,
        completedSets = completedSets.map { it.toCompletedSet() },
        notes = notes,
    )

/**
 * Maps CompletedSetDocument to CompletedSet domain model.
 */
internal fun CompletedSetDocument.toCompletedSet(): CompletedSet =
    CompletedSet(
        setNumber = setNumber,
        reps = reps,
        weight = weight,
        completedAt = completedAt,
        restTimerSeconds = restTimerSeconds,
        notes = notes,
    )

// Domain -> Document mappings

/**
 * Maps WorkoutSession domain model to WorkoutSessionDocument.
 */
internal fun WorkoutSession.toWorkoutSessionDocument(): WorkoutSessionDocument =
    WorkoutSessionDocument(
        id = id,
        userId = userId,
        routineId = routineId,
        routineName = routineName,
        status = status.toStatusString(),
        exercises = exercises.map { it.toWorkoutExerciseDocument() },
        startTime = startTime,
        endTime = endTime,
        totalDurationSeconds = totalDurationSeconds,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

/**
 * Maps WorkoutStatus enum to string.
 */
private fun WorkoutStatus.toStatusString(): String =
    when (this) {
        WorkoutStatus.IN_PROGRESS -> "IN_PROGRESS"
        WorkoutStatus.COMPLETED -> "COMPLETED"
        WorkoutStatus.ABANDONED -> "ABANDONED"
    }

/**
 * Maps WorkoutExercise domain model to WorkoutExerciseDocument.
 */
internal fun WorkoutExercise.toWorkoutExerciseDocument(): WorkoutExerciseDocument =
    WorkoutExerciseDocument(
        exerciseId = exerciseId,
        exerciseName = exerciseName,
        order = order,
        targetSets = targetSets,
        targetReps = targetReps,
        targetRestSeconds = targetRestSeconds,
        completedSets = completedSets.map { it.toCompletedSetDocument() },
        notes = notes,
    )

/**
 * Maps CompletedSet domain model to CompletedSetDocument.
 */
internal fun CompletedSet.toCompletedSetDocument(): CompletedSetDocument =
    CompletedSetDocument(
        setNumber = setNumber,
        reps = reps,
        weight = weight,
        completedAt = completedAt,
        restTimerSeconds = restTimerSeconds,
        notes = notes,
    )

// Exception mapping

/**
 * Maps FirebaseFirestoreException to WorkoutException.
 */
internal fun FirebaseFirestoreException.toWorkoutException(): WorkoutException =
    when (code) {
        FirebaseFirestoreException.Code.NOT_FOUND -> WorkoutException.SessionNotFound
        FirebaseFirestoreException.Code.PERMISSION_DENIED -> WorkoutException.UnauthorizedAccess
        FirebaseFirestoreException.Code.UNAVAILABLE -> WorkoutException.NetworkError
        else -> WorkoutException.Unknown(message)
    }
