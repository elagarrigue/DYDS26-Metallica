package edu.dyds.movies.data.external

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

            val tmdbDeferred = async { safeCall { tmdb.getMovieByTitle(title) } }
            val omdbDeferred = async { safeCall { omdb.getMovieByTitle(title) } }

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

    private suspend fun <T> safeCall(block: suspend () -> T): T? {
        return try {
            block()
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

    private fun canCombine(
        tmdbMovie: Movie,
        omdbMovie: Movie
    ): Boolean {

        val sameTitle = tmdbMovie.title.equals(
            omdbMovie.title,
            ignoreCase = true
        )

        val tmdbYear = extractYear(tmdbMovie.releaseDate)
        val omdbYear = extractYear(omdbMovie.releaseDate)

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
        tmdbMovie: Movie,
        omdbMovie: Movie
    ): Movie {
        return tmdbMovie.copy(
            poster = normalizePoster(
                omdbMovie.poster
            ) ?: normalizePoster(
                tmdbMovie.poster
            ),
            voteAverage = if (
                omdbMovie.voteAverage > 0.0
            ) {
                omdbMovie.voteAverage
            } else {
                tmdbMovie.voteAverage
            }
        )
    }
}
