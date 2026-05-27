package edu.dyds.movies.data.external.omdb

import edu.dyds.movies.data.MovieDetailExternalSource
import edu.dyds.movies.domain.model.Movie
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.IOException

class OMDBMoviesExternalSource(
    private val httpClient: HttpClient
) : MovieDetailExternalSource {

    override suspend fun getMovieByTitle(title: String): Movie? {
        return try {
            val result = httpClient.get("/") {
                parameter("t", title)
            }.body<OMDBMovie>()

            if (result.response == "False") {
                null
            } else {
                OMDBMovieMapper.toDomain(result)
            }
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
}

@Serializable
data class OMDBMovie(
    @SerialName("Title") val title: String = "",
    @SerialName("Plot") val plot: String = "",
    @SerialName("Released") val released: String = "",
    @SerialName("Poster") val poster: String = "",
    @SerialName("imdbRating") val imdbRating: String = "",
    @SerialName("imdbID") val imdbId: String = "",
    @SerialName("Response") val response: String = "False"
)

private object OMDBMovieMapper {

    fun toDomain(remote: OMDBMovie): Movie {
        return Movie(
            externalId = remote.imdbId.takeIf { it.isNotBlank() },
            id = 0,
            title = remote.title,
            overview = remote.plot,
            releaseDate = remote.released,
            poster = remote.poster.takeIf { it != "N/A" },
            backdrop = null,
            originalTitle = remote.title,
            originalLanguage = "",
            popularity = 0.0,
            voteAverage = remote.imdbRating.toDoubleOrNull() ?: 0.0
        )
    }
}
