package edu.dyds.movies.presentation.detail

import edu.dyds.movies.domain.model.Movie
import edu.dyds.movies.domain.usecase.GetMovieDetailUseCase

class FakeGetMovieDetailUseCase : GetMovieDetailUseCase {
    var result: Movie? = null
    var shouldThrowError = false

    override suspend fun invoke(id: Int): Movie? {
        if (shouldThrowError) throw Exception("UseCase error")
        return result
    }
}
