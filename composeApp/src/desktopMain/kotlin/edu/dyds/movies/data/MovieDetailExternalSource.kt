package edu.dyds.movies.data

import edu.dyds.movies.domain.model.Movie

interface MovieDetailExternalSource {
    suspend fun getMovieByTitle(title: String): Movie?
}
