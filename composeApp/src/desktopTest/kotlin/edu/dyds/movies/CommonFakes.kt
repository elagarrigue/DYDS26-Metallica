package edu.dyds.movies

import edu.dyds.movies.data.external.RemoteMovie
import edu.dyds.movies.data.external.RemoteResult
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

fun remoteMovie(
    id: Int,
    title: String = "Remote $id",
    overview: String = "Remote overview $id",
    releaseDate: String = "2026-05-11",
    posterPath: String = "/poster-$id.png",
    backdropPath: String? = "/backdrop-$id.png",
    originalTitle: String = "Remote original $id",
    originalLanguage: String = "en",
    popularity: Double = 10.0,
    voteAverage: Double = 5.0,
): RemoteMovie {
    return RemoteMovie(
        id = id,
        title = title,
        overview = overview,
        releaseDate = releaseDate,
        posterPath = posterPath,
        backdropPath = backdropPath,
        originalTitle = originalTitle,
        originalLanguage = originalLanguage,
        popularity = popularity,
        voteAverage = voteAverage
    )
}

fun remoteResult(
    results: List<RemoteMovie>
): RemoteResult {
    return RemoteResult(
        results = results
    )
}
