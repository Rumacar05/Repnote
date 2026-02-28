package com.ruma.repnote.core.auth.domain.usecase

import com.ruma.repnote.core.auth.domain.model.AuthUser
import com.ruma.repnote.core.auth.domain.repository.AuthRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class DefaultGetCurrentUserUseCaseTest {
    @Test
    fun `WHEN invoke is called THEN repository currentUser is returned`() =
        runTest {
            val mockUser =
                AuthUser(
                    uid = "test-uid",
                    email = "test@example.com",
                    displayName = "Test User",
                    photoUrl = null,
                )
            val userFlow = flowOf(mockUser)
            val repository =
                mockk<AuthRepository> {
                    every { currentUser } returns userFlow
                }
            val useCase = DefaultGetCurrentUserUseCase(repository)

            val result = useCase().first()

            result shouldBeEqualTo mockUser
        }

    @Test
    fun `WHEN invoke is called with null user THEN repository currentUser with null is returned`() =
        runTest {
            val userFlow = flowOf(null)
            val repository =
                mockk<AuthRepository> {
                    every { currentUser } returns userFlow
                }
            val useCase = DefaultGetCurrentUserUseCase(repository)

            val result = useCase().first()

            result shouldBeEqualTo null
        }
}
