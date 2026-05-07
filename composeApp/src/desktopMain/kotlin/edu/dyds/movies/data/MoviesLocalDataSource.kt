package edu.dyds.movies.data

import edu.dyds.movies.domain.model.Movie

interface MoviesLocalDataSource : MoviesRemoteDataSource {
    suspend fun saveMovies(movies: List<Movie>)
    suspend fun saveMovie(movie: Movie)
}

