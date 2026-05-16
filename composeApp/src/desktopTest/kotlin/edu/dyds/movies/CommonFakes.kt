package edu.dyds.movies

import edu.dyds.movies.domain.model.Movie

fun movie(
    id: Int,
    title: String = "Movie $id",
    overview: String = "Overview $id",
    releaseDate: String = "2026-05-11",
    poster: String = "poster-$id",
    backdrop: String? = "backdrop-$id",
    originalTitle: String = "Original $id",
    originalLanguage: String = "en",
    popularity: Double = 10.0,
    voteAverage: Double = 5.0,
): Movie {
    return Movie(
        id = id,
        title = title,
        overview = overview,
        releaseDate = releaseDate,
        poster = poster,
        backdrop = backdrop,
        originalTitle = originalTitle,
        originalLanguage = originalLanguage,
        popularity = popularity,
        voteAverage = voteAverage
    )
}
