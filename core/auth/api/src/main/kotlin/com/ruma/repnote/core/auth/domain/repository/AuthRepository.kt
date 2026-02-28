package com.ruma.repnote.core.auth.domain.repository

import com.ruma.repnote.core.auth.domain.model.AuthResult
import com.ruma.repnote.core.auth.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<AuthUser?>

    suspend fun signInWithEmail(
        email: String,
        password: String,
    ): AuthResult<AuthUser>
    suspend fun signUpWithEmail(
        email: String,
        password: String,
    ): AuthResult<AuthUser>
    suspend fun signInWithGoogle(idToken: String): AuthResult<AuthUser>
    suspend fun signOut(): AuthResult<Unit>
    suspend fun isUserAuthenticated(): Boolean
}
