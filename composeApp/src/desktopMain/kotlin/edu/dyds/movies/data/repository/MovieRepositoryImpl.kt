
package edu.dyds.movies.data.repository

import edu.dyds.movies.data.external.ExternalMovieDataSource
import edu.dyds.movies.data.local.LocalMovieDataSource
import edu.dyds.movies.domain.model.Movie
import edu.dyds.movies.domain.repository.MovieRepository

/**
 * Implementación del repositorio de películas que coordina entre fuentes de datos locales y externas.
 *
 * Estrategia: Cache-first
 * - Intenta obtener datos del caché local primero
 * - Si el caché está vacío, obtiene datos de la API externa
 * - Almacena los datos obtenidos en el caché para futuras consultas
 *
 * Invariante: Las dependencias son inyectadas por constructor y son de tipo interfaz/abstracción
 * (respeta el principio de inversión de dependencias).
 */
class MovieRepositoryImpl(
    private val local: LocalMovieDataSource,
    private val external: ExternalMovieDataSource
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

