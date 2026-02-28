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

class DefaultSignUpWithEmailUseCaseTest {
    @Test
    fun `WHEN invoke is called with valid credentials THEN repository signUpWithEmail is called and Success is returned`() =
        runTest {
            val email = "newuser@example.com"
            val password = "password123"
            val mockUser =
                AuthUser(
                    uid = "new-uid",
                    email = email,
                    displayName = null,
                    photoUrl = null,
                )
            val expectedResult = AuthResult.Success(mockUser)
            val repository =
                mockk<AuthRepository> {
                    coEvery { signUpWithEmail(email, password) } returns expectedResult
                }
            val useCase = DefaultSignUpWithEmailUseCase(repository)

            val result = useCase(email, password)

            result shouldBeEqualTo expectedResult
        }

    @Test
    fun `WHEN invoke is called with existing email THEN Error with EmailAlreadyInUse is returned`() =
        runTest {
            val email = "existing@example.com"
            val password = "password123"
            val expectedResult = AuthResult.Error(AuthException.EmailAlreadyInUse)
            val repository =
                mockk<AuthRepository> {
                    coEvery { signUpWithEmail(email, password) } returns expectedResult
                }
            val useCase = DefaultSignUpWithEmailUseCase(repository)

            val result = useCase(email, password)

            result shouldBeEqualTo expectedResult
        }

    @Test
    fun `WHEN invoke is called with weak password THEN Error with WeakPassword is returned`() =
        runTest {
            val email = "test@example.com"
            val password = "123"
            val expectedResult = AuthResult.Error(AuthException.WeakPassword)
            val repository =
                mockk<AuthRepository> {
                    coEvery { signUpWithEmail(email, password) } returns expectedResult
                }
            val useCase = DefaultSignUpWithEmailUseCase(repository)

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
                    coEvery { signUpWithEmail(email, password) } returns expectedResult
                }
            val useCase = DefaultSignUpWithEmailUseCase(repository)

            val result = useCase(email, password)

            result shouldBeEqualTo expectedResult
        }
}
