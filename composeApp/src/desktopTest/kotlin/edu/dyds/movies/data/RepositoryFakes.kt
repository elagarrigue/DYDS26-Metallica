package edu.dyds.movies.data

import edu.dyds.movies.domain.model.Movie

class FakeMoviesRemoteDataSource : MoviesRemoteDataSource {
    var movies = mutableListOf<Movie>()
    var shouldThrowError = false

    override suspend fun getMovies(): List<Movie> {
        if (shouldThrowError) throw Exception("Remote error")
        return movies
    }

    override suspend fun getMovieByTitle(title: String): Movie? {
        if (shouldThrowError) throw Exception("Remote error")
        return movies.find { it.title.equals(title, ignoreCase = true) }
    }
}
