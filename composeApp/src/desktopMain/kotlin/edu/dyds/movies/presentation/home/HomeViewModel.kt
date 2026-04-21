package edu.dyds.movies.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.dyds.movies.domain.model.QualifiedMovie
import edu.dyds.movies.domain.usecase.GetMoviesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val getMovies: GetMoviesUseCase) : ViewModel() {

    // UiState: lo que la View observa (flujo unidireccional)
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState

    // UiEvent: acciones que la View envía al ViewModel
    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.LoadMovies -> loadMovies()
        }
    }

    private fun loadMovies() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                val movies = getMovies()
                _uiState.value = HomeUiState.Success(movies)
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error("Error loading movies: ${e.message}")
            }
        }
    }

    init {
        loadMovies()
    }
}

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val movies: List<QualifiedMovie>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

sealed interface HomeUiEvent {
    data object LoadMovies : HomeUiEvent
}
