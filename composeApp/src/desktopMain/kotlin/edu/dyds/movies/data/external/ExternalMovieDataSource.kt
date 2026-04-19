package edu.dyds.movies.data.external

import edu.dyds.movies.domain.model.Movie
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Fuente de datos externa que consume la API de TMDB.
 * Responsable de obtener películas del servicio remoto.
 */
class ExternalMovieDataSource(private val httpClient: HttpClient) {

    /**
     * Obtiene la lista de películas populares desde la API de TMDB.
     *
     * @return Lista de películas del dominio mapeadas desde la respuesta remota
     * @throws Exception si hay un error en la llamada HTTP o serialización
     */
    suspend fun getMovies(): List<Movie> {
        return try {
            val result = httpClient.get("/3/discover/movie?sort_by=popularity.desc").body<RemoteResult>()
            result.results.map { it.toDomainMovie() }
        } catch (e: Exception) {
            throw ExternalDataSourceException("Error al obtener películas populares", e)
        }
    }

    /**
     * Obtiene los detalles de una película específica por su ID.
     *
     * @param id ID de la película en TMDB
     * @return Película del dominio o null si no se encuentra
     */
    suspend fun getMovieById(id: Int): Movie? {
        return try {
            val remoteMovie = httpClient.get("/3/movie/$id").body<RemoteMovie>()
            remoteMovie.toDomainMovie()
        } catch (e: Exception) {
            throw ExternalDataSourceException("Error al obtener detalles de la película con ID: $id", e)
        }
    }
}

/**
 * Excepción lanzada cuando ocurre un error en la fuente de datos externa.
 */
class ExternalDataSourceException(message: String, cause: Throwable? = null) : Exception(message, cause)

// DTOs para serialización desde la API TMDB

@Serializable
internal data class RemoteResult(
    val page: Int,
    val results: List<RemoteMovie>,
    @SerialName("total_pages") val totalPages: Int,
    @SerialName("total_results") val totalResults: Int
)

@Serializable
internal data class RemoteMovie(
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
) {
    /**
     * Mapea un DTO remoto a una entidad de dominio.
     * Construye las URLs completas para los posters y backdrops.
     */
    fun toDomainMovie(): Movie {
        return Movie(
            id = id,
            title = title,
            overview = overview,
            releaseDate = releaseDate,
            poster = "https://image.tmdb.org/t/p/w185$posterPath",
            backdrop = backdropPath?.let { "https://image.tmdb.org/t/p/w780$it" },
            originalTitle = originalTitle,
            originalLanguage = originalLanguage,
            popularity = popularity,
            voteAverage = voteAverage
        )
    }
}

