package edu.dyds.movies.data

import edu.dyds.movies.domain.model.Movie
import edu.dyds.movies.domain.repository.MovieRepository

class MoviesRepositoryImpl(
    private val local: MoviesLocalDataSource,
    private val external: MoviesRemoteDataSource
) : MovieRepository {

    override suspend fun getMovies(): List<Movie> {
        val cachedMovies = local.getMovies()
        if (cachedMovies.isNotEmpty()) {
            return cachedMovies
        }
        val externalMovies = external.getMovies()
        local.saveMovies(externalMovies)

        return externalMovies
    }

    override suspend fun getMovieById(id: Int): Movie? {
        val movie = external.getMovieById(id)
        if (movie != null) {
            local.saveMovie(movie)
        }

        return movie
    }
}
