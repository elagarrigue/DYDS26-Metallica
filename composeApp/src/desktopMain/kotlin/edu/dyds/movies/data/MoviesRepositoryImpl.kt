package edu.dyds.movies.data

import edu.dyds.movies.domain.model.Movie
import edu.dyds.movies.domain.repository.MovieRepository

class MoviesRepositoryImpl(
    private val localDataSource: MoviesLocalDataSource,
    private val remoteDataSource: MoviesRemoteDataSource
) : MovieRepository {

    override suspend fun getMovies(): List<Movie> {
        val localMovies = localDataSource.getMovies()
        if (localMovies.isNotEmpty()) {
            return localMovies
        }
        val externalMovies = remoteDataSource.getMovies()
        localDataSource.saveMovies(externalMovies)

        return externalMovies
    }

    override suspend fun getMovieById(id: Int): Movie? {
        val movie = remoteDataSource.getMovieById(id)
        if (movie != null) {
            localDataSource.saveMovie(movie)
        }

        return movie
    }
}
