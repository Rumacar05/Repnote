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

class DefaultSignInWithEmailUseCaseTest {
    @Test
    fun `WHEN invoke is called with valid credentials THEN repository signInWithEmail is called and Success is returned`() =
        runTest {
            val email = "test@example.com"
            val password = "password123"
            val mockUser =
                AuthUser(
                    uid = "test-uid",
                    email = email,
                    displayName = "Test User",
                    photoUrl = null,
                )
            val expectedResult = AuthResult.Success(mockUser)
            val repository =
                mockk<AuthRepository> {
                    coEvery { signInWithEmail(email, password) } returns expectedResult
                }
            val useCase = DefaultSignInWithEmailUseCase(repository)

            val result = useCase(email, password)

            result shouldBeEqualTo expectedResult
        }

    @Test
    fun `WHEN invoke is called and repository returns InvalidCredentials THEN Error with InvalidCredentials is returned`() =
        runTest {
            val email = "test@example.com"
            val password = "wrongpassword"
            val expectedResult = AuthResult.Error(AuthException.InvalidCredentials)
            val repository =
                mockk<AuthRepository> {
                    coEvery { signInWithEmail(email, password) } returns expectedResult
                }
            val useCase = DefaultSignInWithEmailUseCase(repository)

            val result = useCase(email, password)

            result shouldBeEqualTo expectedResult
        }

    @Test
    fun `WHEN invoke is called and repository returns UserNotFound THEN Error with UserNotFound is returned`() =
        runTest {
            val email = "nonexistent@example.com"
            val password = "password123"
            val expectedResult = AuthResult.Error(AuthException.UserNotFound)
            val repository =
                mockk<AuthRepository> {
                    coEvery { signInWithEmail(email, password) } returns expectedResult
                }
            val useCase = DefaultSignInWithEmailUseCase(repository)

            val result = useCase(email, password)

            result shouldBeEqualTo expectedResult
        }

    @Test
    fun `WHEN invoke is called and repository returns NetworkError THEN Error with NetworkError is returned`() =
        runTest {
            val email = "test@example.com"
            val password = "password123"
            val expectedResult = AuthResult.Error(AuthException.NetworkError)
            val repository =
                mockk<AuthRepository> {
                    coEvery { signInWithEmail(email, password) } returns expectedResult
                }
            val useCase = DefaultSignInWithEmailUseCase(repository)

            val result = useCase(email, password)

            result shouldBeEqualTo expectedResult
        }
}
