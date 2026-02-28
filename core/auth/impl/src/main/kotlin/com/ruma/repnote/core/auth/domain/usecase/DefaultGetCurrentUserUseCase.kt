package com.ruma.repnote.core.auth.domain.usecase

import com.ruma.repnote.core.auth.domain.model.AuthUser
import com.ruma.repnote.core.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class DefaultGetCurrentUserUseCase(
    private val repository: AuthRepository,
) : GetCurrentUserUseCase {
    override operator fun invoke(): Flow<AuthUser?> = repository.currentUser
}
