package com.ruma.repnote.core.auth.domain.model

sealed class AuthException : Exception() {
    data object InvalidCredentials : AuthException()
    data object UserNotFound : AuthException()
    data object EmailAlreadyInUse : AuthException()
    data object WeakPassword : AuthException()
    data object NetworkError : AuthException()
    data class Unknown(
        override val message: String?,
    ) : AuthException()
}
