package edu.dyds.movies.domain.repository
import edu.dyds.movies.domain.model.Movie
interface MovieRepository {
    suspend fun getMovies(): List<Movie>
    suspend fun getMovieById(id: Int): Movie?
}
