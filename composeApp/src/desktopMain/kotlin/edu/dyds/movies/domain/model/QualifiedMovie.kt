package edu.dyds.movies.domain.model

data class QualifiedMovie(
    val movie: Movie,
    val rating: Double
) {
    val isGoodMovie: Boolean
        get() = rating >= 6.0

}

