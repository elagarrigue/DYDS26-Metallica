package edu.dyds.movies.data

import edu.dyds.movies.domain.model.Movie
import edu.dyds.movies.domain.repository.MovieRepository

class MoviesRepositoryImpl(
    private val localDataSource: MoviesLocalDataSource,
    private val listExternalSource: MoviesListExternalSource,
    private val detailExternalSource: MovieDetailExternalSource
) : MovieRepository {

    override suspend fun getMovies(): List<Movie> {
        val localMovies = localDataSource.getMovies()
        if (localMovies.isNotEmpty()) {
            return localMovies
        }
        val externalMovies = listExternalSource.getMovies()
        localDataSource.saveMovies(externalMovies)

        return externalMovies
    }

    override suspend fun getMovieByTitle(title: String): Movie? {
        val remoteMovie = detailExternalSource.getMovieByTitle(title)

        if (remoteMovie != null) {
            localDataSource.saveMovie(remoteMovie)
            return remoteMovie
        }

        return localDataSource.getMovieByTitle(title)
    }
}
