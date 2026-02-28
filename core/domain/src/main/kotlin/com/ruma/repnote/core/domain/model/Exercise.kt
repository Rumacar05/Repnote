package com.ruma.repnote.core.domain.model

data class Exercise(
    val id: String,
    val name: String,
    val description: String,
    val imageUrl: String?,
    val primaryMuscleGroup: MuscleGroup,
    val secondaryMuscleGroups: List<MuscleGroup>,
    val isGlobal: Boolean,
    val createdBy: String?,
)
