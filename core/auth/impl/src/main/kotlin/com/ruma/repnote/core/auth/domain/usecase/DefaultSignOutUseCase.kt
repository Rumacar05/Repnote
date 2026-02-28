package com.ruma.repnote.core.auth.domain.usecase

import com.ruma.repnote.core.auth.domain.model.AuthResult
import com.ruma.repnote.core.auth.domain.repository.AuthRepository

class DefaultSignOutUseCase(
    private val repository: AuthRepository,
) : SignOutUseCase {
    override suspend operator fun invoke(): AuthResult<Unit> = repository.signOut()
}
