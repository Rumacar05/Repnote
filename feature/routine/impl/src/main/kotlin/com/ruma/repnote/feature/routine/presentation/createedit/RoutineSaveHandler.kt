package com.ruma.repnote.feature.routine.presentation.createedit

import com.ruma.repnote.core.domain.model.Routine
import com.ruma.repnote.core.domain.model.RoutineException
import com.ruma.repnote.core.domain.model.RoutineExercise
import com.ruma.repnote.core.domain.model.RoutineResult
import com.ruma.repnote.core.domain.repository.RoutineRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class RoutineSaveHandler(
    private val routineRepository: RoutineRepository,
    private val uiState: MutableStateFlow<CreateEditRoutineUiState>,
    private val navigationEvent: MutableSharedFlow<CreateEditRoutineNavigationEvent>,
) {
    suspend fun save(
        state: CreateEditRoutineUiState,
        userId: String,
    ) {
        uiState.update { it.copy(isSaving = true, errorMessage = null) }
        val routine = buildRoutine(state, userId)

        if (state.isEditMode && state.routineId != null) {
            updateRoutine(routine, state.routineId)
        } else {
            createRoutine(routine)
        }
    }

    private fun buildRoutine(
        state: CreateEditRoutineUiState,
        userId: String,
    ): Routine {
        val routineExercises =
            state.exercisesWithConfig.map { exercise ->
                RoutineExercise(
                    exerciseId = exercise.exerciseId,
                    order = exercise.order,
                    sets = exercise.sets,
                    reps = exercise.reps,
                    restSeconds = exercise.restSeconds,
                    notes = exercise.notes,
                )
            }

        val currentTimestamp = System.currentTimeMillis()
        return Routine(
            id = state.routineId ?: "",
            userId = userId,
            name = state.name.trim(),
            description = state.description.trim().ifBlank { null },
            exercises = routineExercises,
            createdAt = currentTimestamp,
            updatedAt = currentTimestamp,
        )
    }

    private suspend fun updateRoutine(
        routine: Routine,
        routineId: String,
    ) {
        when (val result = routineRepository.updateRoutine(routine)) {
            is RoutineResult.Success -> {
                navigationEvent.emit(
                    CreateEditRoutineNavigationEvent.NavigateToDetail(routineId),
                )
            }

            is RoutineResult.Error -> {
                val errorMessage =
                    when (val exception = result.exception) {
                        is RoutineException.NetworkError -> {
                            "Network error. Please check your connection."
                        }

                        is RoutineException.UnauthorizedAccess -> {
                            "You don't have permission to update this routine."
                        }

                        is RoutineException.RoutineNotFound -> {
                            "Routine not found."
                        }

                        is RoutineException.InvalidRoutineData -> {
                            "Invalid routine data. Please check your inputs."
                        }

                        is RoutineException.Unknown -> {
                            exception.message
                                ?: "Failed to update routine"
                        }
                    }
                uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = errorMessage,
                    )
                }
            }
        }
    }

    private suspend fun createRoutine(routine: Routine) {
        when (val result = routineRepository.createRoutine(routine)) {
            is RoutineResult.Success -> {
                navigationEvent.emit(
                    CreateEditRoutineNavigationEvent.NavigateToDetail(result.data),
                )
            }

            is RoutineResult.Error -> {
                val errorMessage =
                    when (val exception = result.exception) {
                        is RoutineException.NetworkError -> {
                            "Network error. Please check your connection."
                        }

                        is RoutineException.UnauthorizedAccess -> {
                            "You don't have permission to create routines."
                        }

                        is RoutineException.InvalidRoutineData -> {
                            "Invalid routine data. Please check your inputs."
                        }

                        is RoutineException.RoutineNotFound -> {
                            "Routine not found."
                        }

                        is RoutineException.Unknown -> {
                            exception.message
                                ?: "Failed to create routine"
                        }
                    }
                uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = errorMessage,
                    )
                }
            }
        }
    }
}
