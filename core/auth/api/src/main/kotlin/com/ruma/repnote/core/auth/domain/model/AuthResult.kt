package com.ruma.repnote.core.auth.domain.model

sealed class AuthResult<out T> {
    data class Success<T>(
        val data: T,
    ) : AuthResult<T>()
    data class Error(
        val exception: AuthException,
    ) : AuthResult<Nothing>()
}
