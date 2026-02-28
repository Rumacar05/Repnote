package com.ruma.repnote.feature.profile.presentation

import app.cash.turbine.test
import com.ruma.repnote.core.auth.domain.model.AuthException
import com.ruma.repnote.core.auth.domain.model.AuthResult
import com.ruma.repnote.core.auth.domain.model.AuthUser
import com.ruma.repnote.core.auth.domain.usecase.GetCurrentUserUseCase
import com.ruma.repnote.core.auth.domain.usecase.SignOutUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var signOutUseCase: SignOutUseCase
    private lateinit var getCurrentUserUseCase: GetCurrentUserUseCase
    private lateinit var viewModel: ProfileViewModel

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        signOutUseCase = mockk()
        getCurrentUserUseCase = mockk()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `WHEN ViewModel is initialized with user THEN user data is loaded into state`() =
        runTest {
            val mockUser =
                AuthUser(
                    uid = "test-uid",
                    email = "test@example.com",
                    displayName = "Test User",
                    photoUrl = null,
                )
            every { getCurrentUserUseCase() } returns flowOf(mockUser)

            viewModel = ProfileViewModel(signOutUseCase, getCurrentUserUseCase)
            advanceUntilIdle()

            viewModel.uiState.test {
                val state = awaitItem()
                state.userEmail shouldBeEqualTo "test@example.com"
                state.displayName shouldBeEqualTo "Test User"
            }
        }

    @Test
    fun `WHEN ViewModel is initialized with no user THEN Guest is shown`() =
        runTest {
            every { getCurrentUserUseCase() } returns flowOf(null)

            viewModel = ProfileViewModel(signOutUseCase, getCurrentUserUseCase)
            advanceUntilIdle()

            viewModel.uiState.test {
                val state = awaitItem()
                state.userEmail shouldBeEqualTo "Guest"
                state.displayName shouldBe null
            }
        }

    @Test
    fun `WHEN onSignOutClick is called and sign out succeeds THEN NavigateToLogin is emitted`() =
        runTest {
            val mockUser =
                AuthUser(
                    uid = "test-uid",
                    email = "test@example.com",
                    displayName = "Test User",
                    photoUrl = null,
                )
            every { getCurrentUserUseCase() } returns flowOf(mockUser)
            coEvery { signOutUseCase() } returns AuthResult.Success(Unit)

            viewModel = ProfileViewModel(signOutUseCase, getCurrentUserUseCase)
            advanceUntilIdle()

            viewModel.navigationEvent.test {
                viewModel.onSignOutClick()
                advanceUntilIdle()

                val event = awaitItem()
                event shouldBeEqualTo ProfileNavigationEvent.NavigateToLogin
            }

            coVerify { signOutUseCase() }
        }

    @Test
    fun `WHEN onSignOutClick is called and sign out fails THEN no navigation event is emitted`() =
        runTest {
            val mockUser =
                AuthUser(
                    uid = "test-uid",
                    email = "test@example.com",
                    displayName = "Test User",
                    photoUrl = null,
                )
            every { getCurrentUserUseCase() } returns flowOf(mockUser)
            coEvery { signOutUseCase() } returns AuthResult.Error(AuthException.Unknown("Sign out failed"))

            viewModel = ProfileViewModel(signOutUseCase, getCurrentUserUseCase)
            advanceUntilIdle()

            viewModel.navigationEvent.test {
                viewModel.onSignOutClick()
                advanceUntilIdle()

                expectNoEvents()
            }

            coVerify { signOutUseCase() }
        }
}
