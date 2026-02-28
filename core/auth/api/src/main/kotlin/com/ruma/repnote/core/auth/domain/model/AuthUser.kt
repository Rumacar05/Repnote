package com.ruma.repnote.core.auth.domain.model

data class AuthUser(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
)
