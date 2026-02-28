package com.ruma.repnote.core.data.mapper

import com.google.firebase.firestore.FirebaseFirestoreException
import com.ruma.repnote.core.data.model.RoutineDocument
import com.ruma.repnote.core.data.model.RoutineExerciseDocument
import com.ruma.repnote.core.domain.model.Routine
import com.ruma.repnote.core.domain.model.RoutineException
import com.ruma.repnote.core.domain.model.RoutineExercise

/**
 * Maps RoutineDocument to Routine domain model.
 */
internal fun RoutineDocument.toRoutine(): Routine =
    Routine(
        id = id,
        userId = userId,
        name = name,
        description = description,
        exercises = exercises.map { it.toRoutineExercise() },
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

/**
 * Maps RoutineExerciseDocument to RoutineExercise domain model.
 */
internal fun RoutineExerciseDocument.toRoutineExercise(): RoutineExercise =
    RoutineExercise(
        exerciseId = exerciseId,
        order = order,
        sets = sets,
        reps = reps,
        restSeconds = restSeconds,
        notes = notes,
    )

/**
 * Maps Routine domain model to RoutineDocument.
 */
internal fun Routine.toRoutineDocument(): RoutineDocument =
    RoutineDocument(
        id = id,
        userId = userId,
        name = name,
        description = description,
        exercises = exercises.map { it.toRoutineExerciseDocument() },
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

/**
 * Maps RoutineExercise domain model to RoutineExerciseDocument.
 */
internal fun RoutineExercise.toRoutineExerciseDocument(): RoutineExerciseDocument =
    RoutineExerciseDocument(
        exerciseId = exerciseId,
        order = order,
        sets = sets,
        reps = reps,
        restSeconds = restSeconds,
        notes = notes,
    )

/**
 * Maps FirebaseFirestoreException to RoutineException.
 */
internal fun FirebaseFirestoreException.toRoutineException(): RoutineException =
    when (code) {
        FirebaseFirestoreException.Code.NOT_FOUND -> RoutineException.RoutineNotFound
        FirebaseFirestoreException.Code.PERMISSION_DENIED -> RoutineException.UnauthorizedAccess
        FirebaseFirestoreException.Code.UNAVAILABLE -> RoutineException.NetworkError
        else -> RoutineException.Unknown(message)
    }
