package com.ruma.repnote.core.auth.data.mapper

import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.ruma.repnote.core.auth.domain.model.AuthException
import com.ruma.repnote.core.auth.domain.model.AuthUser

internal fun FirebaseUser.toAuthUser() =
    AuthUser(
        uid = uid,
        email = email,
        displayName = if (displayName.isNullOrEmpty()) null else displayName,
        photoUrl = photoUrl?.toString(),
    )

internal fun FirebaseAuthException.toAuthException(): AuthException =
    when (errorCode) {
        "ERROR_INVALID_CREDENTIAL", "ERROR_WRONG_PASSWORD" -> AuthException.InvalidCredentials
        "ERROR_USER_NOT_FOUND" -> AuthException.UserNotFound
        "ERROR_EMAIL_ALREADY_IN_USE" -> AuthException.EmailAlreadyInUse
        "ERROR_WEAK_PASSWORD" -> AuthException.WeakPassword
        else -> AuthException.Unknown(message)
    }
