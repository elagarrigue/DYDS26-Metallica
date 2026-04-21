package edu.dyds.movies.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.dyds.movies.domain.model.Movie
import edu.dyds.movies.domain.usecase.GetMovieDetailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DetailViewModel(private val getMovieDetail: GetMovieDetailUseCase) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState

    fun onEvent(event: DetailUiEvent) {
        when (event) {
            is DetailUiEvent.LoadDetail -> loadDetail(event.movieId)
        }
    }

    private fun loadDetail(id: Int) {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            try {
                val movie = getMovieDetail(id)
                if (movie != null) {
                    _uiState.value = DetailUiState.Success(movie)
                } else {
                    _uiState.value = DetailUiState.Error("Movie not found")
                }
            } catch (e: Exception) {
                _uiState.value = DetailUiState.Error("Error loading movie: ${e.message}")
            }
        }
    }
}

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Success(val movie: Movie) : DetailUiState
    data class Error(val message: String) : DetailUiState
}

sealed interface DetailUiEvent {
    data class LoadDetail(val movieId: Int) : DetailUiEvent
}
