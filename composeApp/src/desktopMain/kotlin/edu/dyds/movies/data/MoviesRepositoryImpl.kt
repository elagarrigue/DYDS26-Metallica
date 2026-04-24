package edu.dyds.movies.data

import edu.dyds.movies.domain.model.Movie
import edu.dyds.movies.domain.repository.MovieRepository

/**
 * Interfaz base para fuentes de datos de películas.
 * Define las operaciones de lectura comunes.
 */
interface MovieDataSource {
    suspend fun getMovies(): List<Movie>
    suspend fun getMovieById(id: Int): Movie?
}

/**
 * Interfaz para fuentes de datos que permiten persistencia local (Caché).
 * Extiende [MovieDataSource] añadiendo operaciones de escritura.
 */
interface CacheDataSource : MovieDataSource {
    suspend fun saveMovies(movies: List<Movie>)
    suspend fun saveMovie(movie: Movie)
    suspend fun isCached(): Boolean
    suspend fun clearCache()
}

/**
 * Implementación del repositorio de películas que coordina entre fuentes de datos locales y externas.
 *
 * Estrategia: Cache-first
 * - Intenta obtener datos del caché local primero
 * - Si el caché está vacío, obtiene datos de la API externa
 * - Almacena los datos obtenidos en el caché para futuras consultas
 */
class MoviesRepositoryImpl(
    private val local: CacheDataSource,
    private val external: MovieDataSource
) : MovieRepository {

    /**
     * Obtiene la lista de películas con estrategia cache-first.
     *
     * @return Lista de películas
     * @throws ExternalDataSourceException si hay error al consultar la API y el caché está vacío
     */
    override suspend fun getMovies(): List<Movie> {
        // Intenta obtener del caché
        val cachedMovies = local.getMovies()
        if (cachedMovies.isNotEmpty()) {
            return cachedMovies
        }

        // Si el caché está vacío, obtiene de la fuente externa
        val externalMovies = external.getMovies()

        // Almacena en caché para futuras consultas
        local.saveMovies(externalMovies)

        return externalMovies
    }

    /**
     * Obtiene una película específica por su ID.
     *
     * Estrategia: Consulta siempre la API externa para obtener datos frescos,
     * pero almacena el resultado en caché local.
     *
     * @param id ID de la película
     * @return Película encontrada o null si no existe
     */
    override suspend fun getMovieById(id: Int): Movie? {
        // Intenta obtener de la API externa (para datos frescos)
        val movie = external.getMovieById(id)

        // Si se obtuvo exitosamente, almacena en caché
        if (movie != null) {
            local.saveMovie(movie)
        }

        return movie
    }
}
