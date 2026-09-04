package br.com.watchusee.android.viewmodel

import br.com.watchusee.android.data.dto.UserProfileResponse
import br.com.watchusee.android.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val userRepository = mock(UserRepository::class.java)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadProfile should update uiState to Success when repository returns profile`() = runTest {
        val mockProfile = UserProfileResponse(
            id = 1,
            nick = "test",
            createdAt = "2026-08-30T00:00:00Z",
            watchedMovies = 10,
            toWatchMovies = 5
        )
        `when`(userRepository.getProfile()).thenReturn(mockProfile)

        val viewModel = ProfileViewModel(userRepository)

        assertTrue(viewModel.uiState.value is ProfileUiState.Success)
        assertEquals(mockProfile, (viewModel.uiState.value as ProfileUiState.Success).profile)
    }

    @Test
    fun `loadProfile should update uiState to Error when repository throws exception`() = runTest {
        `when`(userRepository.getProfile()).thenThrow(RuntimeException("Network error"))

        val viewModel = ProfileViewModel(userRepository)

        assertTrue(viewModel.uiState.value is ProfileUiState.Error)
        assertEquals("Network error", (viewModel.uiState.value as ProfileUiState.Error).message)
    }
}
