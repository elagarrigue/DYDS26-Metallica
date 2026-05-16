package edu.dyds.movies.domain.usecase

import edu.dyds.movies.domain.model.Movie

interface GetMovieDetailUseCase {
    suspend operator fun invoke(title: String): Movie?
}
