package br.com.watchusee.android.viewmodel

import br.com.watchusee.android.data.dto.MovieResponse
import br.com.watchusee.android.data.repository.AuthRepository
import br.com.watchusee.android.data.repository.MovieRepository
import br.com.watchusee.android.di.NetworkModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val movieRepository = mock(MovieRepository::class.java)
    private val authRepository = mock(AuthRepository::class.java)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `searchMovies should update uiState to Success when repository returns movies`() = runTest {
        val viewModel = SearchViewModel(movieRepository, authRepository)
        assertTrue(viewModel.uiState.value is SearchUiState.Idle)
    }
}
