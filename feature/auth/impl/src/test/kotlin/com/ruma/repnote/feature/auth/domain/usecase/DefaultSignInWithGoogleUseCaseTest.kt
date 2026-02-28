package com.ruma.repnote.feature.auth.domain.usecase

import com.ruma.repnote.core.auth.domain.model.AuthException
import com.ruma.repnote.core.auth.domain.model.AuthResult
import com.ruma.repnote.core.auth.domain.model.AuthUser
import com.ruma.repnote.core.auth.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class DefaultSignInWithGoogleUseCaseTest {
    @Test
    fun `WHEN invoke is called with valid token THEN repository signInWithGoogle is called and Success is returned`() =
        runTest {
            val idToken = "valid-google-token"
            val mockUser =
                AuthUser(
                    uid = "google-uid",
                    email = "google@example.com",
                    displayName = "Google User",
                    photoUrl = "https://example.com/photo.jpg",
                )
            val expectedResult = AuthResult.Success(mockUser)
            val repository =
                mockk<AuthRepository> {
                    coEvery { signInWithGoogle(idToken) } returns expectedResult
                }
            val useCase = DefaultSignInWithGoogleUseCase(repository)

            val result = useCase(idToken)

            result shouldBeEqualTo expectedResult
        }

    @Test
    fun `WHEN invoke is called with invalid token THEN Error with InvalidCredentials is returned`() =
        runTest {
            val idToken = "invalid-token"
            val expectedResult = AuthResult.Error(AuthException.InvalidCredentials)
            val repository =
                mockk<AuthRepository> {
                    coEvery { signInWithGoogle(idToken) } returns expectedResult
                }
            val useCase = DefaultSignInWithGoogleUseCase(repository)

            val result = useCase(idToken)

            result shouldBeEqualTo expectedResult
        }

    @Test
    fun `WHEN invoke is called and repository returns NetworkError THEN Error with NetworkError is returned`() =
        runTest {
            val idToken = "token"
            val expectedResult = AuthResult.Error(AuthException.NetworkError)
            val repository =
                mockk<AuthRepository> {
                    coEvery { signInWithGoogle(idToken) } returns expectedResult
                }
            val useCase = DefaultSignInWithGoogleUseCase(repository)

            val result = useCase(idToken)

            result shouldBeEqualTo expectedResult
        }

    @Test
    fun `WHEN invoke is called and repository returns UserNotFound THEN Error with UserNotFound is returned`() =
        runTest {
            val idToken = "token"
            val expectedResult = AuthResult.Error(AuthException.UserNotFound)
            val repository =
                mockk<AuthRepository> {
                    coEvery { signInWithGoogle(idToken) } returns expectedResult
                }
            val useCase = DefaultSignInWithGoogleUseCase(repository)

            val result = useCase(idToken)

            result shouldBeEqualTo expectedResult
        }
}
