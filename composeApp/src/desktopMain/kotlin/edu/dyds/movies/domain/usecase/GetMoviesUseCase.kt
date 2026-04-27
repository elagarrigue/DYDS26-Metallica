package edu.dyds.movies.domain.usecase
import edu.dyds.movies.domain.model.QualifiedMovie
import edu.dyds.movies.domain.repository.MovieRepository

interface GetMoviesUseCase {
    suspend operator fun invoke(): List<QualifiedMovie>
}

class GetMoviesUseCaseImpl(private val repository: MovieRepository) : GetMoviesUseCase {
    override suspend operator fun invoke(): List<QualifiedMovie> {
        return repository.getMovies()
            .sortedByDescending { it.voteAverage }
            .map { movie ->
                val label = when {
                    movie.voteAverage >= 8.0 -> "Top rated"
                    movie.voteAverage >= 6.0 -> "Good"
                    else -> "Average"
                }
                QualifiedMovie(movie = movie, qualityLabel = label)
            }
    }
}
