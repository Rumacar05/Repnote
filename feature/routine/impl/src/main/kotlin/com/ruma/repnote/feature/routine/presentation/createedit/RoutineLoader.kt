package com.ruma.repnote.feature.routine.presentation.createedit

import com.ruma.repnote.core.auth.domain.usecase.GetCurrentUserUseCase
import com.ruma.repnote.core.domain.model.ExerciseException
import com.ruma.repnote.core.domain.model.ExerciseResult
import com.ruma.repnote.core.domain.model.Routine
import com.ruma.repnote.core.domain.model.RoutineResult
import com.ruma.repnote.core.domain.repository.ExerciseRepository
import com.ruma.repnote.core.domain.repository.RoutineRepository
import com.ruma.repnote.core.domain.util.getNormalizedLanguage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class RoutineLoader(
    private val routineRepository: RoutineRepository,
    private val exerciseRepository: ExerciseRepository,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val exerciseListManager: ExerciseListManager,
    private val uiState: MutableStateFlow<CreateEditRoutineUiState>,
    private val coroutineScope: CoroutineScope,
) {
    fun loadRoutine(routineId: String) {
        coroutineScope.launch {
            uiState.update { it.copy(isLoading = true) }

            val user = getCurrentUser().filterNotNull().first()
            routineRepository.getRoutineById(user.uid, routineId).collect { result ->
                when (result) {
                    is RoutineResult.Success -> {
                        val routine = result.data
                        loadExercisesForRoutine(routine)
                    }

                    is RoutineResult.Error -> {
                        uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Failed to load routine",
                            )
                        }
                    }
                }
            }
        }
    }

    fun loadAvailableExercises() {
        val language = getNormalizedLanguage()
        uiState.update { it.copy(availableExerciseLoading = true) }
        coroutineScope.launch {
            val user = getCurrentUser().filterNotNull().first()
            when (val result = exerciseRepository.getAllExercisesForUser(user.uid, language)) {
                is ExerciseResult.Success -> {
                    exerciseListManager.updateAvailableExercises(result.data)
                    uiState.update {
                        it.copy(
                            availableExerciseLoading = false,
                            errorMessage = null,
                        )
                    }
                }

                is ExerciseResult.Error -> {
                    val errorMessage =
                        when (result.exception) {
                            is ExerciseException.NetworkError -> {
                                "No network connection. Please check your internet and try again."
                            }

                            is ExerciseException.StorageError -> {
                                "Storage error. Please try again later."
                            }

                            is ExerciseException.UnauthorizedAccess -> {
                                "Permission denied. Please check your account permissions."
                            }

                            else -> {
                                "Failed to load exercises. Please try again."
                            }
                        }
                    uiState.update {
                        it.copy(
                            availableExerciseLoading = false,
                            errorMessage = errorMessage,
                        )
                    }
                }
            }
        }
    }

    private suspend fun loadExercisesForRoutine(routine: Routine) {
        val user = getCurrentUser().filterNotNull().first()
        val language = getNormalizedLanguage()

        val exercisesWithConfig =
            routine.exercises.mapNotNull { routineExercise ->
                when (
                    val result =
                        exerciseRepository.getExerciseById(
                            routineExercise.exerciseId,
                            user.uid,
                            language,
                        )
                ) {
                    is ExerciseResult.Success -> {
                        ExerciseWithConfig(
                            exerciseId = routineExercise.exerciseId,
                            exerciseName = result.data.name,
                            order = routineExercise.order,
                            sets = routineExercise.sets,
                            reps = routineExercise.reps,
                            restSeconds = routineExercise.restSeconds,
                            notes = routineExercise.notes,
                        )
                    }

                    is ExerciseResult.Error -> {
                        null
                    }
                }
            }

        uiState.update {
            it.copy(
                isLoading = false,
                name = routine.name,
                description = routine.description ?: "",
                exercisesWithConfig = exercisesWithConfig,
            )
        }
    }
}
