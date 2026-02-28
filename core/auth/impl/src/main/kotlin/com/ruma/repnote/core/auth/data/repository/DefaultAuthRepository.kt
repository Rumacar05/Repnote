package com.ruma.repnote.core.auth.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider
import com.ruma.repnote.core.auth.data.mapper.toAuthException
import com.ruma.repnote.core.auth.data.mapper.toAuthUser
import com.ruma.repnote.core.auth.domain.model.AuthException
import com.ruma.repnote.core.auth.domain.model.AuthResult
import com.ruma.repnote.core.auth.domain.model.AuthUser
import com.ruma.repnote.core.auth.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private const val TAG = "DefaultAuthRepository"

class DefaultAuthRepository(
    private val firebaseAuth: FirebaseAuth,
) : AuthRepository {
    override val currentUser: Flow<AuthUser?> =
        callbackFlow {
            val authStateListener =
                FirebaseAuth.AuthStateListener { auth ->
                    trySend(auth.currentUser?.toAuthUser())
                }
            firebaseAuth.addAuthStateListener(authStateListener)
            awaitClose { firebaseAuth.removeAuthStateListener(authStateListener) }
        }

    @Suppress("TooGenericExceptionCaught")
    override suspend fun signInWithEmail(
        email: String,
        password: String,
    ): AuthResult<AuthUser> =
        try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            result.user?.let { user ->
                AuthResult.Success(user.toAuthUser())
            } ?: AuthResult.Error(AuthException.UserNotFound)
        } catch (e: FirebaseAuthException) {
            AuthResult.Error(e.toAuthException())
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during sign in with email", e)
            AuthResult.Error(AuthException.NetworkError)
        }

    @Suppress("TooGenericExceptionCaught")
    override suspend fun signUpWithEmail(
        email: String,
        password: String,
    ): AuthResult<AuthUser> =
        try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { user ->
                AuthResult.Success(user.toAuthUser())
            } ?: AuthResult.Error(AuthException.Unknown("User creation failed"))
        } catch (e: FirebaseAuthException) {
            AuthResult.Error(e.toAuthException())
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during sign up with email", e)
            AuthResult.Error(AuthException.NetworkError)
        }

    @Suppress("TooGenericExceptionCaught")
    override suspend fun signInWithGoogle(idToken: String): AuthResult<AuthUser> =
        try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            result.user?.let { user ->
                AuthResult.Success(user.toAuthUser())
            } ?: AuthResult.Error(AuthException.UserNotFound)
        } catch (e: FirebaseAuthException) {
            AuthResult.Error(e.toAuthException())
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during sign in with Google", e)
            AuthResult.Error(AuthException.NetworkError)
        }

    @Suppress("TooGenericExceptionCaught")
    override suspend fun signOut(): AuthResult<Unit> =
        try {
            firebaseAuth.signOut()
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during sign out", e)
            AuthResult.Error(AuthException.Unknown(e.message))
        }

    override suspend fun isUserAuthenticated(): Boolean = firebaseAuth.currentUser != null
}
