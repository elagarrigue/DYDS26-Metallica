package edu.dyds.movies.domain.usecase

import edu.dyds.movies.domain.model.Movie
import edu.dyds.movies.movie
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UseCaseTests {

    private lateinit var repository: FakeMovieRepository
    private lateinit var getMoviesUseCase: GetMoviesUseCaseImpl
    private lateinit var getMovieDetailUseCase: GetMovieDetailUseCaseImpl

    @BeforeTest
    fun setUp() {
        repository = FakeMovieRepository()
        getMoviesUseCase = GetMoviesUseCaseImpl(repository)
        getMovieDetailUseCase = GetMovieDetailUseCaseImpl(repository)
    }

    @Test
    fun `GetMoviesUseCase returns sorted qualified movies`() = runTest {
        val movies = listOf(
            movie(1, "Bad Movie", voteAverage = 5.0),
            movie(2, "Good Movie", voteAverage = 8.0)
        )
        repository.movies.addAll(movies)
        
        val result = getMoviesUseCase()
        
        assertEquals(2, result.size)
        assertEquals("Good Movie", result[0].movie.title)
        assertTrue(result[0].isGoodMovie)
        assertFalse(result[1].isGoodMovie)
    }

    @Test
    fun `GetMoviesUseCase returns empty list when no movies`() = runTest {
        val result = getMoviesUseCase()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `GetMoviesUseCase propagates repository error`() = runTest {
        repository.shouldThrowError = true
        try {
            getMoviesUseCase()
            assertTrue(false, "Should have thrown exception")
        } catch (e: Exception) {
            assertEquals("Repository error", e.message)
        }
    }

    @Test
    fun `GetMovieDetailUseCase returns movie when found`() = runTest {
        val movie = movie(1, "Movie", voteAverage = 7.0)
        repository.movies.add(movie)
        
        val result = getMovieDetailUseCase(1)
        
        assertEquals("Movie", result?.title)
    }

    @Test
    fun `GetMovieDetailUseCase returns null when not found`() = runTest {
        val result = getMovieDetailUseCase(999)
        assertNull(result)
    }

    @Test
    fun `GetMovieDetailUseCase propagates repository error`() = runTest {
        repository.shouldThrowError = true
        try {
            getMovieDetailUseCase(1)
            assertTrue(false, "Should have thrown exception")
        } catch (e: Exception) {
            assertEquals("Repository error", e.message)
        }
    }
}
