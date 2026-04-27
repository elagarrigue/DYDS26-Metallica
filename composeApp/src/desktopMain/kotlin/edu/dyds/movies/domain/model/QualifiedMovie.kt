package edu.dyds.movies.domain.model

data class QualifiedMovie(
    val movie: Movie,
    // rating sourced from movie.voteAverage — centralize classification logic here
    val rating: Double
) {
    // Binary classification preserved: good if rating >= 6.0
    val isGoodMovie: Boolean
        get() = rating >= 6.0

    // Human readable label for presentation — kept for compatibility
    val qualityLabel: String
        get() = if (isGoodMovie) "Good" else "Bad"
}

