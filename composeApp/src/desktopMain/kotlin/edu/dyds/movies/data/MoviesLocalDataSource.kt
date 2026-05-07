package edu.dyds.movies.data

import edu.dyds.movies.domain.model.Movie

interface MoviesLocalDataSource {
    suspend fun getMovies(): List<Movie>
    suspend fun getMovieById(id: Int): Movie?
    suspend fun saveMovies(movies: List<Movie>)
    suspend fun saveMovie(movie: Movie)
}

