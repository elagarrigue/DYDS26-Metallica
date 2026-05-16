package edu.dyds.movies.presentation.detail

import edu.dyds.movies.movie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var getMovieDetailUseCase: FakeGetMovieDetailUseCase

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
        viewModel.onEvent(DetailUiEvent.LoadDetail("Movie"))

        val state = viewModel.uiState.value
        when (state) {
            is DetailUiState.Success -> assertEquals("Movie", state.movie.title)
            else -> kotlin.test.fail("Expected Success state")
        }
    }

    @Test
    fun `LoadDetail movie not found updates state to Error`() = runTest {
        getMovieDetailUseCase.result = null

        val viewModel = DetailViewModel(getMovieDetailUseCase)
        viewModel.onEvent(DetailUiEvent.LoadDetail("Movie"))

        val state = viewModel.uiState.value
        when (state) {
            is DetailUiState.Error -> assertEquals("Movie not found", state.message)
            else -> kotlin.test.fail("Expected Error state")
        }
    }

    @Test
    fun `LoadDetail error updates state to Error`() = runTest {
        getMovieDetailUseCase.shouldThrowError = true

        val viewModel = DetailViewModel(getMovieDetailUseCase)
        viewModel.onEvent(DetailUiEvent.LoadDetail("Movie"))

        val state = viewModel.uiState.value
        when (state) {
            is DetailUiState.Error -> assertTrue(state.message.contains("UseCase error"))
            else -> kotlin.test.fail("Expected Error state")
        }
    }
}
