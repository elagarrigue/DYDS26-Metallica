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
                // centralize classification in QualifiedMovie: pass rating only
                QualifiedMovie(movie = movie, rating = movie.voteAverage)
            }
    }
}
