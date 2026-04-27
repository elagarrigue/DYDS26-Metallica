package edu.dyds.movies.data

import edu.dyds.movies.domain.model.Movie
import edu.dyds.movies.domain.repository.MovieRepository

interface MovieDataSource {
    suspend fun getMovies(): List<Movie>
    suspend fun getMovieById(id: Int): Movie?
}

interface CacheDataSource : MovieDataSource {
    suspend fun saveMovies(movies: List<Movie>)
    suspend fun saveMovie(movie: Movie)

}

class MoviesRepositoryImpl(
    private val local: CacheDataSource,
    private val external: MovieDataSource
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
