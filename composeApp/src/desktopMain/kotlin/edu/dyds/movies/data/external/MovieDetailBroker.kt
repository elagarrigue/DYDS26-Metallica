package edu.dyds.movies.data.external

import edu.dyds.movies.data.MovieDetailExternalSource
import edu.dyds.movies.domain.model.Movie
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeout
import java.io.IOException

class MovieDetailBroker(
    private val tmdb: MovieDetailExternalSource,
    private val omdb: MovieDetailExternalSource,
    private val brokerTimeoutMs: Long = 6000L
) : MovieDetailExternalSource {

    override suspend fun getMovieByTitle(title: String): Movie? = withTimeout(brokerTimeoutMs) {
        coroutineScope {

            val tmdbDeferred = async {
                try {
                    tmdb.getMovieByTitle(title)
                } catch (e: CancellationException) {
                    throw e
                } catch (_: IOException) {
                    null
                } catch (_: ClientRequestException) {
                    null
                } catch (_: ServerResponseException) {
                    null
                }
            }

            val omdbDeferred = async {
                try {
                    omdb.getMovieByTitle(title)
                } catch (e: CancellationException) {
                    throw e
                } catch (_: IOException) {
                    null
                } catch (_: ClientRequestException) {
                    null
                } catch (_: ServerResponseException) {
                    null
                }
            }

            val tmdbMovie = tmdbDeferred.await()
            val omdbMovie = omdbDeferred.await()

            when {
                tmdbMovie != null &&
                        omdbMovie != null &&
                        canCombine(tmdbMovie, omdbMovie) -> {
                    combine(tmdbMovie, omdbMovie)
                }

                tmdbMovie != null -> {
                    tmdbMovie.copy(
                        overview = "TMDB: ${tmdbMovie.overview}"
                    )
                }

                omdbMovie != null -> {
                    omdbMovie.copy(
                        overview = "OMDB: ${omdbMovie.overview}"
                    )
                }

                else -> null
            }
        }
    }

    private fun canCombine(
        tmdb: Movie,
        omdb: Movie
    ): Boolean {

        val sameTitle = tmdb.title.equals(
            omdb.title,
            ignoreCase = true
        )

        val tmdbYear = extractYear(tmdb.releaseDate)
        val omdbYear = extractYear(omdb.releaseDate)

        val sameYear =
            tmdbYear != null &&
                    omdbYear != null &&
                    tmdbYear == omdbYear

        return sameTitle && sameYear
    }

    private fun extractYear(releaseDate: String): String? {
        return releaseDate.trim()
            .takeIf { it.length >= 4 }
            ?.take(4)
            ?.takeIf { it.all { char -> char.isDigit() } }
    }

    private fun normalizePoster(
        poster: String?
    ): String? {

        if (poster.isNullOrBlank()) {
            return null
        }

        return if (poster.startsWith("http")) {
            poster
        } else {
            val path = poster.removePrefix("/")
            "https://image.tmdb.org/t/p/w500/$path"
        }
    }

    private fun combine(
        tmdb: Movie,
        omdb: Movie
    ): Movie {
        return tmdb.copy(
            poster = normalizePoster(
                omdb.poster
            ) ?: normalizePoster(
                tmdb.poster
            ),
            voteAverage = if (
                omdb.voteAverage > 0.0
            ) {
                omdb.voteAverage
            } else {
                tmdb.voteAverage
            }
        )
    }
}
