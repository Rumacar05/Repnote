package com.ruma.repnote.core.auth.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.ruma.repnote.core.auth.domain.model.AuthException
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import com.ruma.repnote.core.auth.domain.model.AuthResult as DomainAuthResult

class DefaultAuthRepositoryTest {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var repository: DefaultAuthRepository

    @BeforeEach
    fun setup() {
        firebaseAuth = mockk(relaxed = true)
        repository = DefaultAuthRepository(firebaseAuth)
    }

    @Test
    fun `WHEN signInWithEmail is called with valid credentials THEN Success with AuthUser is returned`() =
        runTest {
            val email = "test@example.com"
            val password = "password123"
            val mockUser =
                mockk<FirebaseUser>(relaxed = true) {
                    every { uid } returns "test-uid"
                    every { this@mockk.email } returns email
                    every { displayName } returns "Test User"
                    every { photoUrl } returns null
                }
            val mockAuthResult =
                mockk<AuthResult> {
                    every { user } returns mockUser
                }
            val mockTask =
                mockk<Task<AuthResult>> {
                    every { isComplete } returns true
                    every { exception } returns null
                    every { isCanceled } returns false
                    every { result } returns mockAuthResult
                }

            coEvery { firebaseAuth.signInWithEmailAndPassword(email, password) } returns mockTask

            val result = repository.signInWithEmail(email, password)

            result shouldBeInstanceOf DomainAuthResult.Success::class.java
            (result as DomainAuthResult.Success).data.email shouldBeEqualTo email
        }

    @Test
    fun `WHEN signInWithEmail is called and FirebaseAuthException is thrown THEN Error with mapped exception is returned`() =
        runTest {
            val email = "test@example.com"
            val password = "wrongpassword"
            val mockException =
                mockk<FirebaseAuthException> {
                    every { errorCode } returns "ERROR_WRONG_PASSWORD"
                    every { message } returns "Wrong password"
                }

            coEvery {
                firebaseAuth.signInWithEmailAndPassword(email, password)
            } throws mockException

            val result = repository.signInWithEmail(email, password)

            result shouldBeInstanceOf DomainAuthResult.Error::class.java
            (result as DomainAuthResult.Error).exception shouldBe AuthException.InvalidCredentials
        }

    @Test
    fun `WHEN signInWithEmail is called and generic exception is thrown THEN Error with NetworkError is returned`() =
        runTest {
            val email = "test@example.com"
            val password = "password123"

            coEvery {
                firebaseAuth.signInWithEmailAndPassword(email, password)
            } throws RuntimeException("Network error")

            val result = repository.signInWithEmail(email, password)

            result shouldBeInstanceOf DomainAuthResult.Error::class.java
            (result as DomainAuthResult.Error).exception shouldBe AuthException.NetworkError
        }

    @Test
    fun `WHEN signInWithEmail is called and user is null THEN Error with UserNotFound is returned`() =
        runTest {
            val email = "test@example.com"
            val password = "password123"
            val mockAuthResult =
                mockk<AuthResult> {
                    every { user } returns null
                }
            val mockTask =
                mockk<Task<AuthResult>> {
                    every { isComplete } returns true
                    every { exception } returns null
                    every { isCanceled } returns false
                    every { result } returns mockAuthResult
                }

            coEvery { firebaseAuth.signInWithEmailAndPassword(email, password) } returns mockTask

            val result = repository.signInWithEmail(email, password)

            result shouldBeInstanceOf DomainAuthResult.Error::class.java
            (result as DomainAuthResult.Error).exception shouldBe AuthException.UserNotFound
        }

    @Test
    fun `WHEN signUpWithEmail is called with valid data THEN Success with AuthUser is returned`() =
        runTest {
            val email = "newuser@example.com"
            val password = "password123"
            val mockUser =
                mockk<FirebaseUser>(relaxed = true) {
                    every { uid } returns "new-uid"
                    every { this@mockk.email } returns email
                    every { displayName } returns null
                    every { photoUrl } returns null
                }
            val mockAuthResult =
                mockk<AuthResult> {
                    every { user } returns mockUser
                }
            val mockTask =
                mockk<Task<AuthResult>> {
                    every { isComplete } returns true
                    every { exception } returns null
                    every { isCanceled } returns false
                    every { result } returns mockAuthResult
                }

            coEvery { firebaseAuth.createUserWithEmailAndPassword(email, password) } returns mockTask

            val result = repository.signUpWithEmail(email, password)

            result shouldBeInstanceOf DomainAuthResult.Success::class.java
            (result as DomainAuthResult.Success).data.email shouldBeEqualTo email
        }

    @Test
    fun `WHEN signUpWithEmail is called with existing email THEN Error with EmailAlreadyInUse is returned`() =
        runTest {
            val email = "existing@example.com"
            val password = "password123"
            val mockException =
                mockk<FirebaseAuthException> {
                    every { errorCode } returns "ERROR_EMAIL_ALREADY_IN_USE"
                    every { message } returns "Email already in use"
                }

            coEvery {
                firebaseAuth.createUserWithEmailAndPassword(email, password)
            } throws mockException

            val result = repository.signUpWithEmail(email, password)

            result shouldBeInstanceOf DomainAuthResult.Error::class.java
            (result as DomainAuthResult.Error).exception shouldBe AuthException.EmailAlreadyInUse
        }

    @Test
    fun `WHEN signUpWithEmail is called with weak password THEN Error with WeakPassword is returned`() =
        runTest {
            val email = "test@example.com"
            val password = "123"
            val mockException =
                mockk<FirebaseAuthException> {
                    every { errorCode } returns "ERROR_WEAK_PASSWORD"
                    every { message } returns "Weak password"
                }

            coEvery {
                firebaseAuth.createUserWithEmailAndPassword(email, password)
            } throws mockException

            val result = repository.signUpWithEmail(email, password)

            result shouldBeInstanceOf DomainAuthResult.Error::class.java
            (result as DomainAuthResult.Error).exception shouldBe AuthException.WeakPassword
        }

    @Test
    fun `WHEN signUpWithEmail is called and user creation fails THEN Error with Unknown is returned`() =
        runTest {
            val email = "test@example.com"
            val password = "password123"
            val mockAuthResult =
                mockk<AuthResult> {
                    every { user } returns null
                }
            val mockTask =
                mockk<Task<AuthResult>> {
                    every { isComplete } returns true
                    every { exception } returns null
                    every { isCanceled } returns false
                    every { result } returns mockAuthResult
                }

            coEvery { firebaseAuth.createUserWithEmailAndPassword(email, password) } returns mockTask

            val result = repository.signUpWithEmail(email, password)

            result shouldBeInstanceOf DomainAuthResult.Error::class.java
            val exception = (result as DomainAuthResult.Error).exception
            exception shouldBeInstanceOf AuthException.Unknown::class.java
            (exception as AuthException.Unknown).message shouldBeEqualTo "User creation failed"
        }

    @Test
    fun `WHEN signInWithGoogle is called with valid token THEN Success with AuthUser is returned`() =
        runTest {
            val idToken = "valid-google-token"
            val mockUser =
                mockk<FirebaseUser>(relaxed = true) {
                    every { uid } returns "google-uid"
                    every { email } returns "google@example.com"
                    every { displayName } returns "Google User"
                    every { photoUrl } returns null
                }
            val mockAuthResult =
                mockk<AuthResult> {
                    every { user } returns mockUser
                }
            val mockTask =
                mockk<Task<AuthResult>> {
                    every { isComplete } returns true
                    every { exception } returns null
                    every { isCanceled } returns false
                    every { result } returns mockAuthResult
                }

            coEvery { firebaseAuth.signInWithCredential(any()) } returns mockTask

            val result = repository.signInWithGoogle(idToken)

            result shouldBeInstanceOf DomainAuthResult.Success::class.java
            (result as DomainAuthResult.Success).data.email shouldBeEqualTo "google@example.com"
        }

    @Test
    fun `WHEN signInWithGoogle is called with invalid token THEN Error is returned`() =
        runTest {
            val idToken = "invalid-token"
            val mockException =
                mockk<FirebaseAuthException> {
                    every { errorCode } returns "ERROR_INVALID_CREDENTIAL"
                    every { message } returns "Invalid credential"
                }

            coEvery { firebaseAuth.signInWithCredential(any()) } throws mockException

            val result = repository.signInWithGoogle(idToken)

            result shouldBeInstanceOf DomainAuthResult.Error::class.java
            (result as DomainAuthResult.Error).exception shouldBe AuthException.InvalidCredentials
        }

    @Test
    fun `WHEN signInWithGoogle is called and user is null THEN Error with UserNotFound is returned`() =
        runTest {
            val idToken = "valid-token"
            val mockAuthResult =
                mockk<AuthResult> {
                    every { user } returns null
                }
            val mockTask =
                mockk<Task<AuthResult>> {
                    every { isComplete } returns true
                    every { exception } returns null
                    every { isCanceled } returns false
                    every { result } returns mockAuthResult
                }

            coEvery { firebaseAuth.signInWithCredential(any()) } returns mockTask

            val result = repository.signInWithGoogle(idToken)

            result shouldBeInstanceOf DomainAuthResult.Error::class.java
            (result as DomainAuthResult.Error).exception shouldBe AuthException.UserNotFound
        }

    @Test
    fun `WHEN signOut is called THEN Success with Unit is returned`() =
        runTest {
            every { firebaseAuth.signOut() } returns Unit

            val result = repository.signOut()

            result shouldBeInstanceOf DomainAuthResult.Success::class.java
            (result as DomainAuthResult.Success).data shouldBe Unit
            verify { firebaseAuth.signOut() }
        }

    @Test
    fun `WHEN signOut is called and exception is thrown THEN Error with Unknown is returned`() =
        runTest {
            val errorMessage = "Sign out failed"
            every { firebaseAuth.signOut() } throws RuntimeException(errorMessage)

            val result = repository.signOut()

            result shouldBeInstanceOf DomainAuthResult.Error::class.java
            val exception = (result as DomainAuthResult.Error).exception
            exception shouldBeInstanceOf AuthException.Unknown::class.java
            (exception as AuthException.Unknown).message shouldBeEqualTo errorMessage
        }

    @Test
    fun `WHEN isUserAuthenticated is called with authenticated user THEN true is returned`() =
        runTest {
            val mockUser = mockk<FirebaseUser>()
            every { firebaseAuth.currentUser } returns mockUser

            val result = repository.isUserAuthenticated()

            result shouldBe true
        }

    @Test
    fun `WHEN isUserAuthenticated is called with no user THEN false is returned`() =
        runTest {
            every { firebaseAuth.currentUser } returns null

            val result = repository.isUserAuthenticated()

            result shouldBe false
        }
}
