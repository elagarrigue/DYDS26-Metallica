package edu.dyds.movies.domain.usecase

import edu.dyds.movies.domain.model.QualifiedMovie
import edu.dyds.movies.domain.repository.MovieRepository

private const val MIN_VOTE_AVERAGE = 6.0
class GetMoviesUseCaseImpl(private val repository: MovieRepository) : GetMoviesUseCase {
    override suspend operator fun invoke(): List<QualifiedMovie> {
        return repository.getMovies()
            .sortedByDescending { it.voteAverage }
            .map { movie ->
                QualifiedMovie(
                    movie = movie,
                    isGoodMovie = movie.voteAverage >= MIN_VOTE_AVERAGE
                )
            }
    }
}

