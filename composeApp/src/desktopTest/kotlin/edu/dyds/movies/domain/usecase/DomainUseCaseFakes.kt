package edu.dyds.movies.domain.usecase

import edu.dyds.movies.domain.model.Movie
import edu.dyds.movies.domain.repository.MovieRepository

class FakeMovieRepository(
    var movies: MutableList<Movie> = mutableListOf(),
    var shouldThrowError: Boolean = false
) : MovieRepository {
    var getMoviesCalls = 0
    var getMovieByTitleCalls = 0

    override suspend fun getMovies(): List<Movie> {
        getMoviesCalls += 1
        if (shouldThrowError) throw Exception("Repository error")
        return movies
    }

    override suspend fun getMovieByTitle(title: String): Movie? {
        getMovieByTitleCalls += 1
        if (shouldThrowError) throw Exception("Repository error")
        return movies.find { it.title.equals(title, ignoreCase = true) }
    }
}
