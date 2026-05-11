package edu.dyds.movies.presentation.detail

import edu.dyds.movies.domain.model.Movie
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
class DetailViewModelTest {

    private lateinit var getMovieDetailUseCase: FakeGetMovieDetailUseCase
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getMovieDetailUseCase = FakeGetMovieDetailUseCase()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `LoadDetail event updates state to Success`() = runTest {
        val movie = movie(1, "Movie")
        getMovieDetailUseCase.result = movie
        
        val viewModel = DetailViewModel(getMovieDetailUseCase)
        viewModel.onEvent(DetailUiEvent.LoadDetail(1))
        
        assertTrue(viewModel.uiState.value is DetailUiState.Loading)
        
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertTrue(state is DetailUiState.Success)
        assertEquals("Movie", (state as DetailUiState.Success).movie.title)
    }

    @Test
    fun `LoadDetail movie not found updates state to Error`() = runTest {
        getMovieDetailUseCase.result = null
        
        val viewModel = DetailViewModel(getMovieDetailUseCase)
        viewModel.onEvent(DetailUiEvent.LoadDetail(1))
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertTrue(state is DetailUiState.Error)
        assertEquals("Movie not found", (state as DetailUiState.Error).message)
    }

    @Test
    fun `LoadDetail error updates state to Error`() = runTest {
        getMovieDetailUseCase.shouldThrowError = true
        
        val viewModel = DetailViewModel(getMovieDetailUseCase)
        viewModel.onEvent(DetailUiEvent.LoadDetail(1))
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertTrue(state is DetailUiState.Error)
        assertTrue((state as DetailUiState.Error).message.contains("UseCase error"))
    }
}
