package edu.dyds.movies.data

import edu.dyds.movies.domain.model.Movie
import edu.dyds.movies.domain.repository.MovieRepository

interface MovieDataSource {
    suspend fun getMovies(): List<Movie>
    suspend fun getMovieById(id: Int): Movie?
}

interface CacheDataSource : MovieDataSource {
    suspend fun saveMovies(movies: List<Movie>)
    suspend fun saveMovie(movie: Movie)
    suspend fun isCached(): Boolean
    suspend fun clearCache()
}

class MoviesRepositoryImpl(
    private val local: CacheDataSource,
    private val external: MovieDataSource
) : MovieRepository {

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
