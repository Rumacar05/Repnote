package com.ruma.repnote.feature.auth.domain.usecase

import com.ruma.repnote.core.auth.domain.model.AuthResult
import com.ruma.repnote.core.auth.domain.model.AuthUser
import com.ruma.repnote.core.auth.domain.repository.AuthRepository

class DefaultSignInWithGoogleUseCase(
    private val repository: AuthRepository,
) : SignInWithGoogleUseCase {
    override suspend operator fun invoke(idToken: String): AuthResult<AuthUser> =
        repository.signInWithGoogle(idToken)
}
