package com.ruma.repnote.core.domain.service

import com.ruma.repnote.core.domain.model.ExerciseResult

interface ImageStorageService {
    suspend fun uploadImage(
        imageId: String,
        imageBytes: ByteArray,
    ): ExerciseResult<String>

    suspend fun deleteImage(imageUrl: String): ExerciseResult<Unit>
}
