package edu.dyds.movies.data.local

import edu.dyds.movies.movie
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MoviesLocalDataSourceImplTest {

    private lateinit var dataSource: MoviesLocalDataSourceImpl

    @BeforeTest
    fun setUp() {
        dataSource = MoviesLocalDataSourceImpl()
    }

    @Test
    fun `getMovies returns empty list initially`() = runTest {
        val movies = dataSource.getMovies()
        assertEquals(0, movies.size)
    }

    @Test
    fun `saveMovies stores movies and getMovies returns them`() = runTest {
        val moviesToSave = listOf(
            movie(id = 1, title = "Movie 1"),
            movie(id = 2, title = "Movie 2")
        )
        dataSource.saveMovies(moviesToSave)
        
        val retrievedMovies = dataSource.getMovies()
        assertEquals(2, retrievedMovies.size)
        assertEquals("Movie 1", retrievedMovies[0].title)
    }

    @Test
    fun `saveMovie adds a new movie`() = runTest {
        val movie = movie(id = 1, title = "New Movie")
        dataSource.saveMovie(movie)
        
        val retrieved = dataSource.getMovieByTitle("New Movie")
        assertEquals("New Movie", retrieved?.title)
    }

    @Test
    fun `saveMovie updates existing movie`() = runTest {
        val movie1 = movie(id = 1, title = "Original", voteAverage = 7.0)
        val movie2 = movie(id = 1, title = "Updated", voteAverage = 8.0)
        
        dataSource.saveMovie(movie1)
        dataSource.saveMovie(movie2)
        
        val retrieved = dataSource.getMovieByTitle("Updated")
        assertEquals("Updated", retrieved?.title)
        assertEquals(1, dataSource.getMovies().size)
    }

    @Test
    fun `getMovieByTitle returns null if not found`() = runTest {
        val movie = dataSource.getMovieByTitle("Non existing")
        assertNull(movie)
    }
}
