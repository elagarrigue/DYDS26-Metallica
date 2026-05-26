package edu.dyds.movies.data.local

import edu.dyds.movies.data.MoviesLocalDataSource
import edu.dyds.movies.domain.model.Movie

class MoviesLocalDataSourceImpl : MoviesLocalDataSource {

    private val movieCache: MutableList<Movie> = mutableListOf()

    override suspend fun getMovies(): List<Movie> {
        return movieCache.toList()
    }

    override suspend fun getMovieByTitle(title: String): Movie? {
        return movieCache.find { it.title.equals(title, ignoreCase = true) }
    }

    override suspend fun saveMovies(movies: List<Movie>) {
        movieCache.clear()
        movieCache.addAll(movies)
    }

    override suspend fun saveMovie(movie: Movie) {
        val existingIndex = movieCache.indexOfFirst { 
            (it.id != 0 && it.id == movie.id) || 
            (it.externalId != null && it.externalId == movie.externalId)
        }
        if (existingIndex >= 0) {
            movieCache[existingIndex] = movie
        } else {
            movieCache.add(movie)
        }
    }

}

