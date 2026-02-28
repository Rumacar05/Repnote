package com.ruma.repnote.feature.auth.presentation.login

import app.cash.turbine.test
import com.ruma.repnote.core.analytics.domain.service.AnalyticsService
import com.ruma.repnote.core.auth.domain.model.AuthException
import com.ruma.repnote.core.auth.domain.model.AuthResult
import com.ruma.repnote.core.auth.domain.model.AuthUser
import com.ruma.repnote.feature.auth.domain.usecase.SignInWithEmailUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var signInWithEmailUseCase: SignInWithEmailUseCase
    private lateinit var analyticsService: AnalyticsService
    private lateinit var viewModel: LoginViewModel

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        signInWithEmailUseCase = mockk()
        analyticsService = mockk(relaxed = true)
        viewModel = LoginViewModel(signInWithEmailUseCase, analyticsService)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `WHEN onEmailChange is called THEN email is updated and validation is reset`() =
        runTest {
            viewModel.uiState.test {
                val initialState = awaitItem()
                initialState.email shouldBeEqualTo ""

                viewModel.onEmailChange("test@example.com")
                advanceUntilIdle()

                val updatedState = awaitItem()
                updatedState.email shouldBeEqualTo "test@example.com"
                updatedState.isEmailValid shouldBe true
                updatedState.errorMessage.shouldBeNull()
            }
        }

    @Test
    fun `WHEN onPasswordChange is called THEN password is updated and validation is reset`() =
        runTest {
            viewModel.uiState.test {
                val initialState = awaitItem()
                initialState.password shouldBeEqualTo ""

                viewModel.onPasswordChange("password123")
                advanceUntilIdle()

                val updatedState = awaitItem()
                updatedState.password shouldBeEqualTo "password123"
                updatedState.isPasswordValid shouldBe true
                updatedState.errorMessage.shouldBeNull()
            }
        }

    @Test
    fun `WHEN onSignInClick is called with valid credentials and sign in succeeds THEN NavigateToHome is emitted`() =
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
            coEvery { signInWithEmailUseCase(email, password) } returns AuthResult.Success(mockUser)

            viewModel.onEmailChange(email)
            viewModel.onPasswordChange(password)
            advanceUntilIdle()

            viewModel.navigationEvent.test {
                viewModel.onSignInClick()
                advanceUntilIdle()

                val event = awaitItem()
                event shouldBeEqualTo LoginNavigationEvent.NavigateToHome
            }

            coVerify { signInWithEmailUseCase(email, password) }
        }

    @Test
    fun `WHEN onSignInClick is called with valid credentials and sign in fails THEN error message is shown`() =
        runTest {
            val email = "test@example.com"
            val password = "wrongpassword"
            coEvery {
                signInWithEmailUseCase(email, password)
            } returns AuthResult.Error(AuthException.InvalidCredentials)

            viewModel.onEmailChange(email)
            viewModel.onPasswordChange(password)
            advanceUntilIdle()

            viewModel.uiState.test {
                skipItems(1)

                viewModel.onSignInClick()
                advanceUntilIdle()

                skipItems(1)
                val finalState = awaitItem()
                finalState.isLoading shouldBe false
                finalState.errorMessage shouldBeEqualTo "Invalid email or password"
            }

            coVerify { signInWithEmailUseCase(email, password) }
        }

    @Test
    fun `WHEN onSignInClick is called with UserNotFound error THEN user not found message is shown`() =
        runTest {
            val email = "nonexistent@example.com"
            val password = "password123"
            coEvery {
                signInWithEmailUseCase(email, password)
            } returns AuthResult.Error(AuthException.UserNotFound)

            viewModel.onEmailChange(email)
            viewModel.onPasswordChange(password)
            advanceUntilIdle()

            viewModel.uiState.test {
                skipItems(1)

                viewModel.onSignInClick()
                advanceUntilIdle()

                skipItems(1)
                val finalState = awaitItem()
                finalState.errorMessage shouldBeEqualTo "User not found"
            }
        }

    @Test
    fun `WHEN onSignInClick is called with NetworkError THEN network error message is shown`() =
        runTest {
            val email = "test@example.com"
            val password = "password123"
            coEvery {
                signInWithEmailUseCase(email, password)
            } returns AuthResult.Error(AuthException.NetworkError)

            viewModel.onEmailChange(email)
            viewModel.onPasswordChange(password)
            advanceUntilIdle()

            viewModel.uiState.test {
                skipItems(1)

                viewModel.onSignInClick()
                advanceUntilIdle()

                skipItems(1)
                val finalState = awaitItem()
                finalState.errorMessage shouldBeEqualTo "Network error. Please check your connection"
            }
        }

    @Test
    fun `WHEN onSignInClick is called with invalid email THEN email validation fails`() =
        runTest {
            viewModel.onEmailChange("invalidemail")
            viewModel.onPasswordChange("password123")
            advanceUntilIdle()

            viewModel.uiState.test {
                skipItems(1)

                viewModel.onSignInClick()
                advanceUntilIdle()

                val updatedState = awaitItem()
                updatedState.isEmailValid shouldBe false
                updatedState.isPasswordValid shouldBe true
            }

            coVerify(exactly = 0) { signInWithEmailUseCase(any(), any()) }
        }

    @Test
    fun `WHEN onSignInClick is called with empty email THEN email validation fails`() =
        runTest {
            viewModel.onEmailChange("")
            viewModel.onPasswordChange("password123")
            advanceUntilIdle()

            viewModel.uiState.test {
                skipItems(1)

                viewModel.onSignInClick()
                advanceUntilIdle()

                val updatedState = awaitItem()
                updatedState.isEmailValid shouldBe false
            }

            coVerify(exactly = 0) { signInWithEmailUseCase(any(), any()) }
        }

    @Test
    fun `WHEN onSignInClick is called with short password THEN password validation fails`() =
        runTest {
            viewModel.onEmailChange("test@example.com")
            viewModel.onPasswordChange("12345")
            advanceUntilIdle()

            viewModel.uiState.test {
                skipItems(1)

                viewModel.onSignInClick()
                advanceUntilIdle()

                val updatedState = awaitItem()
                updatedState.isEmailValid shouldBe true
                updatedState.isPasswordValid shouldBe false
            }

            coVerify(exactly = 0) { signInWithEmailUseCase(any(), any()) }
        }

    @Test
    fun `WHEN onSignInClick is called with empty password THEN password validation fails`() =
        runTest {
            viewModel.onEmailChange("test@example.com")
            viewModel.onPasswordChange("")
            advanceUntilIdle()

            viewModel.uiState.test {
                skipItems(1)

                viewModel.onSignInClick()
                advanceUntilIdle()

                val updatedState = awaitItem()
                updatedState.isPasswordValid shouldBe false
            }

            coVerify(exactly = 0) { signInWithEmailUseCase(any(), any()) }
        }

    @Test
    fun `WHEN onRegisterClick is called THEN NavigateToRegister is emitted`() =
        runTest {
            viewModel.navigationEvent.test {
                viewModel.onRegisterClick()
                advanceUntilIdle()

                val event = awaitItem()
                event shouldBeEqualTo LoginNavigationEvent.NavigateToRegister
            }
        }

    @Test
    fun `WHEN onSignInClick is called THEN loading state is updated correctly`() =
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
            coEvery { signInWithEmailUseCase(email, password) } returns AuthResult.Success(mockUser)

            viewModel.onEmailChange(email)
            viewModel.onPasswordChange(password)
            advanceUntilIdle()

            viewModel.uiState.test {
                skipItems(1)

                viewModel.onSignInClick()
                advanceUntilIdle()

                val loadingState = awaitItem()
                loadingState.isLoading shouldBe true

                val finalState = awaitItem()
                finalState.isLoading shouldBe false
            }
        }
}
