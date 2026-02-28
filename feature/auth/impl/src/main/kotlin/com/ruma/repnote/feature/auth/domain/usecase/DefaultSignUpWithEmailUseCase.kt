package com.ruma.repnote.feature.auth.domain.usecase

import com.ruma.repnote.core.auth.domain.model.AuthResult
import com.ruma.repnote.core.auth.domain.model.AuthUser
import com.ruma.repnote.core.auth.domain.repository.AuthRepository

class DefaultSignUpWithEmailUseCase(
    private val repository: AuthRepository,
) : SignUpWithEmailUseCase {
    override suspend operator fun invoke(
        email: String,
        password: String,
    ): AuthResult<AuthUser> = repository.signUpWithEmail(email, password)
}
