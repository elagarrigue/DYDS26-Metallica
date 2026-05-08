package edu.dyds.movies.data

import edu.dyds.movies.domain.model.Movie

interface MoviesRemoteDataSource {
    suspend fun getMovies(): List<Movie>
    suspend fun getMovieById(id: Int): Movie?
}

