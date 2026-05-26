package edu.dyds.movies.data

import edu.dyds.movies.data.local.FakeMoviesLocalDataSource
import edu.dyds.movies.movie
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MoviesRepositoryImplTest {

    private lateinit var localDataSource: FakeMoviesLocalDataSource
    private lateinit var remoteDataSource: FakeMoviesRemoteDataSource
    private lateinit var repository: MoviesRepositoryImpl

    @BeforeTest
    fun setUp() {
        localDataSource = FakeMoviesLocalDataSource()
        remoteDataSource = FakeMoviesRemoteDataSource()
        repository = MoviesRepositoryImpl(
            localDataSource = localDataSource,
            listExternalSource = remoteDataSource,
            detailExternalSource = remoteDataSource
        )
    }

    @Test
    fun `getMovies returns local movies when not empty`() = runTest {
        val localMovies = listOf(movie(1, "Local"))
        localDataSource.movies.addAll(localMovies)
        
        val result = repository.getMovies()
        
        assertEquals(1, result.size)
        assertEquals("Local", result[0].title)
        assertTrue(localDataSource.getMoviesCalled)
    }

    @Test
    fun `getMovies calls remote and saves to local when local is empty`() = runTest {
        val remoteMovies = listOf(movie(2, "Remote"))
        remoteDataSource.movies.addAll(remoteMovies)
        
        val result = repository.getMovies()
        
        assertEquals(1, result.size)
        assertEquals("Remote", result[0].title)
        assertTrue(localDataSource.saveMoviesCalled)
        assertEquals(1, localDataSource.movies.size)
    }

    @Test
    fun `getMovies propagates error from remote when local is empty`() = runTest {
        remoteDataSource.shouldThrowError = true
        
        try {
            repository.getMovies()
            assertTrue(false, "Should have thrown exception")
        } catch (e: Exception) {
            assertEquals("Remote error", e.message)
        }
    }

    @Test
    fun `getMovieByTitle calls remote and saves to local`() = runTest {
        val movie = movie(1, "Remote Movie")
        remoteDataSource.movies.add(movie)
        
        val result = repository.getMovieByTitle("Remote Movie")

        assertNotNull(result)
        assertEquals("Remote Movie", result.title)
        assertTrue(localDataSource.saveMovieCalled)
        assertEquals(1, localDataSource.movies.size)
    }

    @Test
    fun `getMovieByTitle returns null when not found in remote`() = runTest {
        val result = repository.getMovieByTitle("Non existing")
        assertTrue(result == null)
    }

    @Test
    fun `getMovieByTitle propagates error from remote`() = runTest {
        remoteDataSource.shouldThrowError = true
        
        try {
            repository.getMovieByTitle("Any")
            assertTrue(false, "Should have thrown exception")
        } catch (e: Exception) {
            assertEquals("Remote error", e.message)
        }
    }
}
