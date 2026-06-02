package edu.dyds.movies.data.external.tmdb

import edu.dyds.movies.data.external.MovieDetailExternalSource
import edu.dyds.movies.data.external.MoviesListExternalSource
import edu.dyds.movies.domain.model.Movie
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.*
import kotlinx.coroutines.CancellationException
import java.io.IOException

class TMDBMoviesExternalSource(private val httpClient: HttpClient) : MoviesListExternalSource, MovieDetailExternalSource {

    override suspend fun getMovies(): List<Movie> {
        val result = httpClient.get("/3/discover/movie?sort_by=popularity.desc").body<RemoteResult>()
        return result.results.map { TMDBMovieMapper.toDomain(it) }
    }

    override suspend fun getMovieByTitle(title: String): Movie? {
        return try {
            val result = httpClient.get("/3/search/movie") {
                parameter("query", title)
            }.body<RemoteResult>()

            result.results.firstOrNull()?.let { TMDBMovieMapper.toDomain(it) }
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
