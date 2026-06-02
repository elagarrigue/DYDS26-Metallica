package edu.dyds.movies.data.external

import edu.dyds.movies.domain.model.Movie

interface MoviesListExternalSource {
    suspend fun getMovies(): List<Movie>
}
