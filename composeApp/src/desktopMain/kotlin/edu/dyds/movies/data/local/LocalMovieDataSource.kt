package edu.dyds.movies.data.local

import edu.dyds.movies.data.CacheDataSource
import edu.dyds.movies.domain.model.Movie

class LocalMovieDataSource : CacheDataSource {

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

    override suspend fun isCached(): Boolean {
        return movieCache.isNotEmpty()
    }

    override suspend fun clearCache() {
        movieCache.clear()
    }
}

