package edu.dyds.movies.domain.usecase
import edu.dyds.movies.domain.model.Movie
import edu.dyds.movies.domain.repository.MovieRepository
class GetMovieDetailUseCase(private val repository: MovieRepository) {
    suspend operator fun invoke(id: Int): Movie? =
        repository.getMovieById(id)
}
