package com.ruma.repnote.core.auth.domain.usecase

import com.ruma.repnote.core.auth.domain.model.AuthResult

interface SignOutUseCase {
    suspend operator fun invoke(): AuthResult<Unit>
}
