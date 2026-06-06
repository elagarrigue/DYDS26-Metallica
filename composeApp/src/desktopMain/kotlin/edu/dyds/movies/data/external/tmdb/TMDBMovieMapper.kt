package edu.dyds.movies.data.external.tmdb

import edu.dyds.movies.domain.model.Movie

object TMDBMovieMapper {
    private const val IMAGE_BASE_URL_W185 = "https://image.tmdb.org/t/p/w185"
    private const val IMAGE_BASE_URL_W780 = "https://image.tmdb.org/t/p/w780"

    fun toDomain(remote: RemoteMovie): Movie {
        return Movie(
            externalId = null,
            id = remote.id,
            title = remote.title,
            overview = remote.overview,
            releaseDate = remote.releaseDate,
            poster = "$IMAGE_BASE_URL_W185${remote.posterPath}",
            backdrop = remote.backdropPath?.let { "$IMAGE_BASE_URL_W780$it" },
            originalTitle = remote.originalTitle,
            originalLanguage = remote.originalLanguage,
            popularity = remote.popularity,
            voteAverage = remote.voteAverage
        )
    }
}
