package edu.dyds.movies.domain.usecase

import edu.dyds.movies.domain.model.Movie
import edu.dyds.movies.domain.repository.MovieRepository

class GetMovieDetailUseCaseImpl(
    private val repository: MovieRepository
) : GetMovieDetailUseCase {
    override suspend operator fun invoke(title: String): Movie? {
        return repository.getMovieByTitle(title)
    }
}

