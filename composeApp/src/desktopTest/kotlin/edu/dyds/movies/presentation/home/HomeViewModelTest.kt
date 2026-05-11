package edu.dyds.movies.presentation.home

import edu.dyds.movies.domain.model.Movie
import edu.dyds.movies.domain.model.QualifiedMovie
import edu.dyds.movies.movie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private lateinit var getMoviesUseCase: FakeGetMoviesUseCase
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getMoviesUseCase = FakeGetMoviesUseCase()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading and then Success`() = runTest {
        val movies = listOf(
            QualifiedMovie(movie(1, "Movie"), true)
        )
        getMoviesUseCase.result = movies
        
        val viewModel = HomeViewModel(getMoviesUseCase)
        
        assertTrue(viewModel.uiState.value is HomeUiState.Loading)
        
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertTrue(state is HomeUiState.Success)
        assertEquals(1, (state as HomeUiState.Success).movies.size)
    }

    @Test
    fun `loadMovies error updates state to Error`() = runTest {
        getMoviesUseCase.shouldThrowError = true
        
        val viewModel = HomeViewModel(getMoviesUseCase)
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertTrue(state is HomeUiState.Error)
        assertTrue((state as HomeUiState.Error).message.contains("UseCase error"))
    }
}
