package edu.dyds.movies.domain.usecase
import edu.dyds.movies.domain.model.QualifiedMovie
import edu.dyds.movies.domain.repository.MovieRepository
class GetMoviesUseCase(private val repository: MovieRepository) {
    suspend operator fun invoke(): List<QualifiedMovie> {
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
