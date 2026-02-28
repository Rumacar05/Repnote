package com.ruma.repnote.core.database.converter

import androidx.room.TypeConverter
import com.ruma.repnote.core.domain.model.MuscleGroup
import com.ruma.repnote.core.domain.model.RoutineExercise
import com.ruma.repnote.core.domain.model.SyncStatus
import com.ruma.repnote.core.domain.model.WorkoutExercise
import com.ruma.repnote.core.domain.model.WorkoutStatus
import kotlinx.serialization.json.Json

@Suppress("TooGenericExceptionCaught", "SwallowedException")
class TypeConverters {
    companion object {
        private const val MIN_ROUTINE_EXERCISE_PARTS = 3
        private const val EXERCISE_ID_INDEX = 0
        private const val ORDER_INDEX = 1
        private const val SETS_INDEX = 2
        private const val REPS_INDEX = 3
        private const val REST_SECONDS_INDEX = 4
        private const val NOTES_INDEX = 5
    }

    @TypeConverter
    fun fromMuscleGroupList(value: List<MuscleGroup>?): String? =
        value?.joinToString(",") { it.name }

    @TypeConverter
    fun toMuscleGroupList(value: String?): List<MuscleGroup>? =
        value?.split(",")?.mapNotNull { name ->
            try {
                MuscleGroup.valueOf(name)
            } catch (_: IllegalArgumentException) {
                // Ignore invalid muscle group names
                null
            }
        }

    @TypeConverter
    fun fromRoutineExerciseList(value: List<RoutineExercise>?): String? =
        value?.joinToString(";") { exercise ->
            "${exercise.exerciseId}," +
                "${exercise.order}," +
                "${exercise.sets}," +
                "${exercise.reps ?: ""}," +
                "${exercise.restSeconds ?: ""}," +
                (exercise.notes?.replace(",", "\\,")?.replace(";", "\\;") ?: "")
        }

    @TypeConverter
    fun toRoutineExerciseList(value: String?): List<RoutineExercise>? {
        if (value.isNullOrBlank()) return emptyList()

        return value.split(";").mapNotNull { exerciseStr ->
            try {
                val parts = exerciseStr.split(",")
                if (parts.size >= MIN_ROUTINE_EXERCISE_PARTS) {
                    RoutineExercise(
                        exerciseId = parts[EXERCISE_ID_INDEX],
                        order = parts[ORDER_INDEX].toInt(),
                        sets = parts[SETS_INDEX].toInt(),
                        reps = parts.getOrNull(REPS_INDEX)?.toIntOrNull(),
                        restSeconds = parts.getOrNull(REST_SECONDS_INDEX)?.toIntOrNull(),
                        notes =
                            parts
                                .getOrNull(NOTES_INDEX)
                                ?.replace("\\,", ",")
                                ?.replace("\\;", ";")
                                ?.takeIf { it.isNotBlank() },
                    )
                } else {
                    null
                }
            } catch (_: Exception) {
                // Ignore malformed exercise data
                null
            }
        }
    }

    // WorkoutStatus converters
    @TypeConverter
    fun fromWorkoutStatus(value: WorkoutStatus?): String? = value?.name

    @TypeConverter
    fun toWorkoutStatus(value: String?): WorkoutStatus? =
        value?.let {
            try {
                WorkoutStatus.valueOf(it)
            } catch (_: IllegalArgumentException) {
                null
            }
        }

    // SyncStatus converters
    @TypeConverter
    fun fromSyncStatus(value: SyncStatus?): String? = value?.name

    @TypeConverter
    fun toSyncStatus(value: String?): SyncStatus? =
        value?.let {
            try {
                SyncStatus.valueOf(it)
            } catch (_: IllegalArgumentException) {
                null
            }
        }

    // WorkoutExercise list converters (using JSON serialization for robustness)
    @TypeConverter
    fun fromWorkoutExerciseList(value: List<WorkoutExercise>?): String? {
        if (value == null) return null
        return try {
            Json.encodeToString(value)
        } catch (e: Exception) {
            null
        }
    }

    @TypeConverter
    fun toWorkoutExerciseList(value: String?): List<WorkoutExercise>? {
        if (value.isNullOrBlank()) return emptyList()
        return try {
            Json.decodeFromString<List<WorkoutExercise>>(value)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
