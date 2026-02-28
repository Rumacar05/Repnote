package com.ruma.repnote.core.auth.domain.usecase

import com.ruma.repnote.core.auth.domain.model.AuthException
import com.ruma.repnote.core.auth.domain.model.AuthResult
import com.ruma.repnote.core.auth.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class DefaultSignOutUseCaseTest {
    @Test
    fun `WHEN invoke is called THEN repository signOut is called and Success is returned`() =
        runTest {
            val expectedResult = AuthResult.Success(Unit)
            val repository =
                mockk<AuthRepository> {
                    coEvery { signOut() } returns expectedResult
                }
            val useCase = DefaultSignOutUseCase(repository)

            val result = useCase()

            result shouldBeEqualTo expectedResult
        }

    @Test
    fun `WHEN invoke is called and repository returns Error THEN Error is returned`() =
        runTest {
            val expectedResult = AuthResult.Error(AuthException.NetworkError)
            val repository =
                mockk<AuthRepository> {
                    coEvery { signOut() } returns expectedResult
                }
            val useCase = DefaultSignOutUseCase(repository)

            val result = useCase()

            result shouldBeEqualTo expectedResult
        }
}
