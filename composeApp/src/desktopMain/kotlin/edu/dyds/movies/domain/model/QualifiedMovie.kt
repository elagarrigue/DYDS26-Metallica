package edu.dyds.movies.domain.model

data class QualifiedMovie(
    val movie: Movie,
    val qualityLabel: String    // e.g. "Top rated", "Average", etc.
)

