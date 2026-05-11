package edu.dyds.movies.data.local

import edu.dyds.movies.data.MoviesLocalDataSource
import edu.dyds.movies.domain.model.Movie

class FakeMoviesLocalDataSource : MoviesLocalDataSource {
    var movies = mutableListOf<Movie>()
    var getMoviesCalled = false
    var saveMoviesCalled = false
    var saveMovieCalled = false

    override suspend fun getMovies(): List<Movie> {
        getMoviesCalled = true
        return movies
    }

    override suspend fun getMovieById(id: Int): Movie? {
        return movies.find { it.id == id }
    }

    override suspend fun saveMovies(movies: List<Movie>) {
        saveMoviesCalled = true
        this.movies.clear()
        this.movies.addAll(movies)
    }

    override suspend fun saveMovie(movie: Movie) {
        saveMovieCalled = true
        val index = movies.indexOfFirst { it.id == movie.id }
        if (index >= 0) movies[index] = movie else movies.add(movie)
    }
}
