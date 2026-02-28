package com.ruma.repnote.feature.auth.presentation.register

import app.cash.turbine.test
import com.ruma.repnote.core.auth.domain.model.AuthException
import com.ruma.repnote.core.auth.domain.model.AuthResult
import com.ruma.repnote.core.auth.domain.model.AuthUser
import com.ruma.repnote.feature.auth.domain.usecase.SignUpWithEmailUseCase
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
class RegisterViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var signUpWithEmailUseCase: SignUpWithEmailUseCase
    private lateinit var viewModel: RegisterViewModel

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        signUpWithEmailUseCase = mockk()
        viewModel = RegisterViewModel(signUpWithEmailUseCase)
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
    fun `WHEN onConfirmPasswordChange is called THEN confirmPassword is updated and validation is reset`() =
        runTest {
            viewModel.uiState.test {
                val initialState = awaitItem()
                initialState.confirmPassword shouldBeEqualTo ""

                viewModel.onConfirmPasswordChange("password123")
                advanceUntilIdle()

                val updatedState = awaitItem()
                updatedState.confirmPassword shouldBeEqualTo "password123"
                updatedState.isConfirmPasswordValid shouldBe true
                updatedState.errorMessage.shouldBeNull()
            }
        }

    @Test
    fun `WHEN onRegisterClick is called with valid credentials and registration succeeds THEN NavigateToHome is emitted`() =
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
            coEvery { signUpWithEmailUseCase(email, password) } returns AuthResult.Success(mockUser)

            viewModel.onEmailChange(email)
            viewModel.onPasswordChange(password)
            viewModel.onConfirmPasswordChange(password)
            advanceUntilIdle()

            viewModel.navigationEvent.test {
                viewModel.onRegisterClick()
                advanceUntilIdle()

                val event = awaitItem()
                event shouldBeEqualTo RegisterNavigationEvent.NavigateToHome
            }

            coVerify { signUpWithEmailUseCase(email, password) }
        }

    @Test
    fun `WHEN onRegisterClick is called with valid credentials and registration fails THEN error message is shown`() =
        runTest {
            val email = "existing@example.com"
            val password = "password123"
            coEvery {
                signUpWithEmailUseCase(email, password)
            } returns AuthResult.Error(AuthException.EmailAlreadyInUse)

            viewModel.onEmailChange(email)
            viewModel.onPasswordChange(password)
            viewModel.onConfirmPasswordChange(password)
            advanceUntilIdle()

            viewModel.uiState.test {
                skipItems(1)

                viewModel.onRegisterClick()
                advanceUntilIdle()

                skipItems(1)
                val finalState = awaitItem()
                finalState.isLoading shouldBe false
                finalState.errorMessage shouldBeEqualTo "Email already in use"
            }

            coVerify { signUpWithEmailUseCase(email, password) }
        }

    @Test
    fun `WHEN onRegisterClick is called with WeakPassword error THEN weak password message is shown`() =
        runTest {
            val email = "test@example.com"
            val password = "123456"
            coEvery {
                signUpWithEmailUseCase(email, password)
            } returns AuthResult.Error(AuthException.WeakPassword)

            viewModel.onEmailChange(email)
            viewModel.onPasswordChange(password)
            viewModel.onConfirmPasswordChange(password)
            advanceUntilIdle()

            viewModel.uiState.test {
                skipItems(1)

                viewModel.onRegisterClick()
                advanceUntilIdle()

                skipItems(1)
                val finalState = awaitItem()
                finalState.errorMessage shouldBeEqualTo "Password is too weak"
            }
        }

    @Test
    fun `WHEN onRegisterClick is called with NetworkError THEN network error message is shown`() =
        runTest {
            val email = "test@example.com"
            val password = "password123"
            coEvery {
                signUpWithEmailUseCase(email, password)
            } returns AuthResult.Error(AuthException.NetworkError)

            viewModel.onEmailChange(email)
            viewModel.onPasswordChange(password)
            viewModel.onConfirmPasswordChange(password)
            advanceUntilIdle()

            viewModel.uiState.test {
                skipItems(1)

                viewModel.onRegisterClick()
                advanceUntilIdle()

                skipItems(1)
                val finalState = awaitItem()
                finalState.errorMessage shouldBeEqualTo "Network error. Please check your connection"
            }
        }

    @Test
    fun `WHEN onRegisterClick is called with invalid email THEN email validation fails`() =
        runTest {
            viewModel.onEmailChange("invalidemail")
            viewModel.onPasswordChange("password123")
            viewModel.onConfirmPasswordChange("password123")
            advanceUntilIdle()

            viewModel.uiState.test {
                skipItems(1)

                viewModel.onRegisterClick()
                advanceUntilIdle()

                val updatedState = awaitItem()
                updatedState.isEmailValid shouldBe false
                updatedState.isPasswordValid shouldBe true
                updatedState.isConfirmPasswordValid shouldBe true
            }

            coVerify(exactly = 0) { signUpWithEmailUseCase(any(), any()) }
        }

    @Test
    fun `WHEN onRegisterClick is called with empty email THEN email validation fails`() =
        runTest {
            viewModel.onEmailChange("")
            viewModel.onPasswordChange("password123")
            viewModel.onConfirmPasswordChange("password123")
            advanceUntilIdle()

            viewModel.uiState.test {
                skipItems(1)

                viewModel.onRegisterClick()
                advanceUntilIdle()

                val updatedState = awaitItem()
                updatedState.isEmailValid shouldBe false
            }

            coVerify(exactly = 0) { signUpWithEmailUseCase(any(), any()) }
        }

    @Test
    fun `WHEN onRegisterClick is called with short password THEN password validation fails`() =
        runTest {
            viewModel.onEmailChange("test@example.com")
            viewModel.onPasswordChange("12345")
            viewModel.onConfirmPasswordChange("12345")
            advanceUntilIdle()

            viewModel.uiState.test {
                skipItems(1)

                viewModel.onRegisterClick()
                advanceUntilIdle()

                val updatedState = awaitItem()
                updatedState.isEmailValid shouldBe true
                updatedState.isPasswordValid shouldBe false
                updatedState.isConfirmPasswordValid shouldBe true
            }

            coVerify(exactly = 0) { signUpWithEmailUseCase(any(), any()) }
        }

    @Test
    fun `WHEN onRegisterClick is called with empty password THEN password validation fails`() =
        runTest {
            viewModel.onEmailChange("test@example.com")
            viewModel.onPasswordChange("")
            viewModel.onConfirmPasswordChange("")
            advanceUntilIdle()

            viewModel.uiState.test {
                skipItems(1)

                viewModel.onRegisterClick()
                advanceUntilIdle()

                val updatedState = awaitItem()
                updatedState.isPasswordValid shouldBe false
            }

            coVerify(exactly = 0) { signUpWithEmailUseCase(any(), any()) }
        }

    @Test
    fun `WHEN onRegisterClick is called with mismatched passwords THEN confirmPassword validation fails`() =
        runTest {
            viewModel.onEmailChange("test@example.com")
            viewModel.onPasswordChange("password123")
            viewModel.onConfirmPasswordChange("different123")
            advanceUntilIdle()

            viewModel.uiState.test {
                skipItems(1)

                viewModel.onRegisterClick()
                advanceUntilIdle()

                val updatedState = awaitItem()
                updatedState.isEmailValid shouldBe true
                updatedState.isPasswordValid shouldBe true
                updatedState.isConfirmPasswordValid shouldBe false
            }

            coVerify(exactly = 0) { signUpWithEmailUseCase(any(), any()) }
        }

    @Test
    fun `WHEN onBackClick is called THEN NavigateBack is emitted`() =
        runTest {
            viewModel.navigationEvent.test {
                viewModel.onBackClick()
                advanceUntilIdle()

                val event = awaitItem()
                event shouldBeEqualTo RegisterNavigationEvent.NavigateBack
            }
        }

    @Test
    fun `WHEN onRegisterClick is called THEN loading state is updated correctly`() =
        runTest {
            val email = "test@example.com"
            val password = "password123"
            val mockUser =
                AuthUser(
                    uid = "new-uid",
                    email = email,
                    displayName = null,
                    photoUrl = null,
                )
            coEvery { signUpWithEmailUseCase(email, password) } returns AuthResult.Success(mockUser)

            viewModel.onEmailChange(email)
            viewModel.onPasswordChange(password)
            viewModel.onConfirmPasswordChange(password)
            advanceUntilIdle()

            viewModel.uiState.test {
                skipItems(1)

                viewModel.onRegisterClick()
                advanceUntilIdle()

                val loadingState = awaitItem()
                loadingState.isLoading shouldBe true

                val finalState = awaitItem()
                finalState.isLoading shouldBe false
            }
        }
}
