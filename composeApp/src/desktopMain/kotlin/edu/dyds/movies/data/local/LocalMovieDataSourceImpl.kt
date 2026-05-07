package edu.dyds.movies.data.local

import edu.dyds.movies.data.MoviesLocalDataSource
import edu.dyds.movies.domain.model.Movie

class LocalMovieDataSourceImpl : MoviesLocalDataSource {

    private val movieCache: MutableList<Movie> = mutableListOf()

    override suspend fun getMovies(): List<Movie> {
        return movieCache.toList()
    }

    override suspend fun getMovieById(id: Int): Movie? {
        return movieCache.find { it.id == id }
    }

    override suspend fun saveMovies(movies: List<Movie>) {
        movieCache.clear()
        movieCache.addAll(movies)
    }

    override suspend fun saveMovie(movie: Movie) {
        val existingIndex = movieCache.indexOfFirst { it.id == movie.id }
        if (existingIndex >= 0) {
            movieCache[existingIndex] = movie
        } else {
            movieCache.add(movie)
        }
    }

}

