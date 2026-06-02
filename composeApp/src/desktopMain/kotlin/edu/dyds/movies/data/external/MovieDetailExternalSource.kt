package edu.dyds.movies.data.external

import edu.dyds.movies.domain.model.Movie

interface MovieDetailExternalSource {
    suspend fun getMovieByTitle(title: String): Movie?
}
