package com.ruma.repnote.feature.routine.presentation.createedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruma.repnote.core.auth.domain.usecase.GetCurrentUserUseCase
import com.ruma.repnote.core.domain.repository.ExerciseRepository
import com.ruma.repnote.core.domain.repository.RoutineRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Suppress("TooManyFunctions")
class CreateEditRoutineViewModel(
    routineRepository: RoutineRepository,
    exerciseRepository: ExerciseRepository,
    private val getCurrentUser: GetCurrentUserUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CreateEditRoutineUiState())
    val uiState: StateFlow<CreateEditRoutineUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<CreateEditRoutineNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private val exerciseListManager = ExerciseListManager(_uiState)
    private val routineValidator = RoutineValidator()
    private val routineSaveHandler =
        RoutineSaveHandler(routineRepository, _uiState, _navigationEvent)
    private val routineLoader =
        RoutineLoader(
            routineRepository,
            exerciseRepository,
            getCurrentUser,
            exerciseListManager,
            _uiState,
            viewModelScope,
        )

    init {
        // Pre-load available exercises for selection
        routineLoader.loadAvailableExercises()
    }

    fun onNameChange(name: String) {
        _uiState.update {
            it.copy(
                name = name,
                isNameValid = name.isNotBlank(),
            )
        }
    }

    fun onDescriptionChange(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun onAddExerciseClick() = exerciseListManager.showExerciseSelector()

    fun onExerciseSearchQueryChange(query: String) = exerciseListManager.updateSearchQuery(query)

    fun onExerciseSelected(
        exerciseId: String,
        exerciseName: String,
    ) = exerciseListManager.selectExercise(exerciseId, exerciseName)

    fun onDismissExerciseSelector() = exerciseListManager.dismissSelector()

    fun onRemoveExercise(index: Int) = exerciseListManager.removeExercise(index)

    fun onReorderExercises(
        fromIndex: Int,
        toIndex: Int,
    ) = exerciseListManager.reorderExercises(fromIndex, toIndex)

    fun onExerciseConfigChange(
        index: Int,
        sets: Int,
        reps: Int?,
        restSeconds: Int?,
        notes: String,
    ) = exerciseListManager.updateExerciseConfig(index, sets, reps, restSeconds, notes)

    fun onSaveClick() {
        val state = _uiState.value
        val validationError = routineValidator.validate(state.name, state.exercisesWithConfig)

        if (validationError != null) {
            _uiState.update {
                it.copy(
                    isNameValid = !validationError.contains("name"),
                    errorMessage = validationError,
                )
            }
        } else {
            viewModelScope.launch {
                val user = getCurrentUser().filterNotNull().first()
                routineSaveHandler.save(state, user.uid)
            }
        }
    }

    fun onCancelClick() {
        viewModelScope.launch {
            _navigationEvent.emit(CreateEditRoutineNavigationEvent.NavigateBack)
        }
    }

    /**
     * Loads a routine for editing. Called when routineId is passed from navigation.
     * Only loads if not already loaded to avoid duplicate loads.
     */
    fun loadRoutineForEdit(id: String) {
        val currentState = _uiState.value
        // Only load if we're not already editing this routine
        if (currentState.routineId != id || !currentState.isEditMode) {
            _uiState.update { it.copy(isEditMode = true, routineId = id) }
            routineLoader.loadRoutine(id)
        }
    }
}
