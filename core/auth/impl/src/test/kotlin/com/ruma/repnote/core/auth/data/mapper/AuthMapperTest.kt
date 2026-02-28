package com.ruma.repnote.core.auth.data.mapper

import android.net.Uri
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.ruma.repnote.core.auth.domain.model.AuthException
import io.mockk.every
import io.mockk.mockk
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.junit.jupiter.api.Test

class AuthMapperTest {
    @Test
    fun `GIVEN firebase user with all fields WHEN toAuthUser is called THEN auth user is returned with all fields`() {
        val uid = "test-uid-123"
        val email = "test@example.com"
        val displayName = "Test User"
        val photoUrlString = "https://example.com/photo.jpg"

        val mockUri = mockk<Uri>()
        every { mockUri.toString() } returns photoUrlString

        val firebaseUser =
            mockk<FirebaseUser> {
                every { this@mockk.uid } returns uid
                every { this@mockk.email } returns email
                every { this@mockk.displayName } returns displayName
                every { photoUrl } returns mockUri
            }

        val result = firebaseUser.toAuthUser()

        result.uid shouldBeEqualTo uid
        result.email shouldBeEqualTo email
        result.displayName shouldBeEqualTo displayName
        result.photoUrl shouldBeEqualTo photoUrlString
    }

    @Test
    fun `GIVEN firebase user with null email WHEN toAuthUser is called THEN auth user is returned with null email`() {
        val firebaseUser =
            mockk<FirebaseUser> {
                every { uid } returns "test-uid"
                every { email } returns null
                every { displayName } returns "Test User"
                every { photoUrl } returns null
            }

        val result = firebaseUser.toAuthUser()

        result.email.shouldBeNull()
    }

    @Test
    fun `GIVEN firebase user with empty display name WHEN toAuthUser is called THEN auth user is returned with null display name`() {
        val firebaseUser =
            mockk<FirebaseUser> {
                every { uid } returns "test-uid"
                every { email } returns "test@example.com"
                every { displayName } returns ""
                every { photoUrl } returns null
            }

        val result = firebaseUser.toAuthUser()

        result.displayName.shouldBeNull()
    }

    @Test
    fun `GIVEN firebase user with null display name WHEN toAuthUser is called THEN auth user is returned with null display name`() {
        val firebaseUser =
            mockk<FirebaseUser> {
                every { uid } returns "test-uid"
                every { email } returns "test@example.com"
                every { displayName } returns null
                every { photoUrl } returns null
            }

        val result = firebaseUser.toAuthUser()

        result.displayName.shouldBeNull()
    }

    @Test
    fun `GIVEN firebase user with null photo url WHEN toAuthUser is called THEN auth user is returned with null photo url`() {
        val firebaseUser =
            mockk<FirebaseUser> {
                every { uid } returns "test-uid"
                every { email } returns "test@example.com"
                every { displayName } returns "Test User"
                every { photoUrl } returns null
            }

        val result = firebaseUser.toAuthUser()

        result.photoUrl.shouldBeNull()
    }

    @Test
    fun `GIVEN firebase exception with ERROR_INVALID_CREDENTIAL WHEN toAuthException is called THEN InvalidCredentials is returned`() {
        val exception =
            mockk<FirebaseAuthException> {
                every { errorCode } returns "ERROR_INVALID_CREDENTIAL"
            }

        val result = exception.toAuthException()

        result shouldBe AuthException.InvalidCredentials
    }

    @Test
    fun `GIVEN firebase exception with ERROR_WRONG_PASSWORD WHEN toAuthException is called THEN InvalidCredentials is returned`() {
        val exception =
            mockk<FirebaseAuthException> {
                every { errorCode } returns "ERROR_WRONG_PASSWORD"
            }

        val result = exception.toAuthException()

        result shouldBe AuthException.InvalidCredentials
    }

    @Test
    fun `GIVEN firebase exception with ERROR_USER_NOT_FOUND WHEN toAuthException is called THEN UserNotFound is returned`() {
        val exception =
            mockk<FirebaseAuthException> {
                every { errorCode } returns "ERROR_USER_NOT_FOUND"
            }

        val result = exception.toAuthException()

        result shouldBe AuthException.UserNotFound
    }

    @Test
    fun `GIVEN firebase exception with ERROR_EMAIL_ALREADY_IN_USE WHEN toAuthException is called THEN EmailAlreadyInUse is returned`() {
        val exception =
            mockk<FirebaseAuthException> {
                every { errorCode } returns "ERROR_EMAIL_ALREADY_IN_USE"
            }

        val result = exception.toAuthException()

        result shouldBe AuthException.EmailAlreadyInUse
    }

    @Test
    fun `GIVEN firebase exception with ERROR_WEAK_PASSWORD WHEN toAuthException is called THEN WeakPassword is returned`() {
        val exception =
            mockk<FirebaseAuthException> {
                every { errorCode } returns "ERROR_WEAK_PASSWORD"
            }

        val result = exception.toAuthException()

        result shouldBe AuthException.WeakPassword
    }

    @Test
    fun `GIVEN firebase exception with unknown error code WHEN toAuthException is called THEN Unknown is returned with message`() {
        val errorMessage = "Unknown error occurred"
        val exception =
            mockk<FirebaseAuthException> {
                every { errorCode } returns "ERROR_UNKNOWN"
                every { message } returns errorMessage
            }

        val result = exception.toAuthException()

        result shouldBeEqualTo AuthException.Unknown(errorMessage)
    }
}
