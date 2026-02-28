package com.ruma.repnote.feature.auth.domain.usecase

import com.ruma.repnote.core.auth.domain.model.AuthResult
import com.ruma.repnote.core.auth.domain.model.AuthUser

interface SignInWithEmailUseCase {
    suspend operator fun invoke(
        email: String,
        password: String,
    ): AuthResult<AuthUser>
}
