package com.ruma.repnote.core.auth.domain.usecase

import com.ruma.repnote.core.auth.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow

interface GetCurrentUserUseCase {
    operator fun invoke(): Flow<AuthUser?>
}
