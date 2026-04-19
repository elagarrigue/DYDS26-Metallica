package edu.dyds.movies.data.local

import edu.dyds.movies.domain.model.Movie

/**
 * Fuente de datos local que gestiona el caché en memoria de películas.
 * Proporciona almacenamiento temporal para evitar llamadas redundantes a la API.
 */
class LocalMovieDataSource {

    private val movieCache: MutableList<Movie> = mutableListOf()

    /**
     * Obtiene las películas del caché local.
     *
     * @return Lista de películas en caché, vacía si el caché está vacío
     */
    suspend fun getMovies(): List<Movie> {
        return movieCache.toList()
    }

    /**
     * Obtiene una película específica del caché por su ID.
     *
     * @param id ID de la película a buscar
     * @return Película encontrada o null si no existe en el caché
     */
    suspend fun getMovieById(id: Int): Movie? {
        return movieCache.find { it.id == id }
    }

    /**
     * Almacena una lista de películas en el caché, reemplazando el contenido anterior.
     *
     * @param movies Lista de películas a cachear
     */
    suspend fun saveMovies(movies: List<Movie>) {
        movieCache.clear()
        movieCache.addAll(movies)
    }

    /**
     * Almacena una película individual en el caché.
     *
     * @param movie Película a cachear
     */
    suspend fun saveMovie(movie: Movie) {
        val existingIndex = movieCache.indexOfFirst { it.id == movie.id }
        if (existingIndex >= 0) {
            movieCache[existingIndex] = movie
        } else {
            movieCache.add(movie)
        }
    }

    /**
     * Verifica si el caché contiene películas.
     *
     * @return true si el caché no está vacío, false en caso contrario
     */
    suspend fun isCached(): Boolean {
        return movieCache.isNotEmpty()
    }

    /**
     * Limpia todo el contenido del caché.
     */
    suspend fun clearCache() {
        movieCache.clear()
    }
}

