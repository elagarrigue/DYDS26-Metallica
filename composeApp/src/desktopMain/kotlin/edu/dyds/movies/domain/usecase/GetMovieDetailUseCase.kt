package edu.dyds.movies.domain.usecase
import edu.dyds.movies.domain.model.Movie
import edu.dyds.movies.domain.repository.MovieRepository
interface GetMovieDetailUseCase {
    suspend operator fun invoke(id: Int): Movie?
}

class GetMovieDetailUseCaseImpl(
    private val repository: MovieRepository
) : GetMovieDetailUseCase {

    override suspend operator fun invoke(id: Int): Movie? {
        return repository.getMovieById(id)
    }
}