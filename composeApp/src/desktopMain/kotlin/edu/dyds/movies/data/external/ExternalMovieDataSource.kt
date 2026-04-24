package edu.dyds.movies.data.external

import edu.dyds.movies.data.MovieDataSource
import edu.dyds.movies.domain.model.Movie
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class ExternalMovieDataSource(private val httpClient: HttpClient) : MovieDataSource {

    override suspend fun getMovies(): List<Movie> {
        val result = httpClient.get("/3/discover/movie?sort_by=popularity.desc").body<RemoteResult>()
        return result.results.map { MovieMapper.toDomain(it) }
    }

    override suspend fun getMovieById(id: Int): Movie? {
        return try {
            val remoteMovie = httpClient.get("/3/movie/$id").body<RemoteMovie>()
            MovieMapper.toDomain(remoteMovie)
        } catch (e: Exception) {
            null
        }
    }
}

@Serializable
data class RemoteResult(
    val results: List<RemoteMovie>
)

@Serializable
data class RemoteMovie(
    val id: Int,
    val title: String,
    val overview: String,
    @SerialName("release_date") val releaseDate: String,
    @SerialName("poster_path") val posterPath: String,
    @SerialName("backdrop_path") val backdropPath: String?,
    @SerialName("original_title") val originalTitle: String,
    @SerialName("original_language") val originalLanguage: String,
    val popularity: Double,
    @SerialName("vote_average") val voteAverage: Double,
)

