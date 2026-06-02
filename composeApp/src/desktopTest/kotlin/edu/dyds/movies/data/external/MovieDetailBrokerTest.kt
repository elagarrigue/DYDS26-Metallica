package edu.dyds.movies.data.external

import edu.dyds.movies.domain.model.Movie
import edu.dyds.movies.movie
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class MovieDetailBrokerTest {

    @Test
    fun `combines TMDB and OMDB when both are available and compatible`() = runTest {
        val tmdbMovie = movie(id = 1, title = "Interstellar", releaseDate = "2014-11-07", poster = "/tmdb.jpg", voteAverage = 8.0)
        val omdbMovie = movie(id = 0, title = "Interstellar", releaseDate = "2014-11-07", poster = "https://omdb.com/poster.jpg", voteAverage = 8.6, externalId = "tt0816692")

        val broker = MovieDetailBroker(
            tmdb = FakeDetailSource(tmdbMovie),
            omdb = FakeDetailSource(omdbMovie)
        )

        val result = broker.getMovieByTitle("Interstellar")

        assertNotNull(result)
        assertEquals("Interstellar", result.title)
        assertEquals("https://omdb.com/poster.jpg", result.poster)
        assertEquals(8.6, result.voteAverage)
    }

    @Test
    fun `returns TMDB only when OMDB is not available`() = runTest {
        val tmdbMovie = movie(id = 1, title = "Interstellar", overview = "TMDB Overview")
        
        val broker = MovieDetailBroker(
            tmdb = FakeDetailSource(tmdbMovie),
            omdb = FakeDetailSource(null)
        )

        val result = broker.getMovieByTitle("Interstellar")

        assertNotNull(result)
        assertEquals("TMDB: TMDB Overview", result.overview)
    }

    @Test
    fun `returns OMDB only when TMDB is not available`() = runTest {
        val omdbMovie = movie(id = 0, title = "Interstellar", overview = "OMDB Overview")

        val broker = MovieDetailBroker(
            tmdb = FakeDetailSource(null),
            omdb = FakeDetailSource(omdbMovie)
        )

        val result = broker.getMovieByTitle("Interstellar")

        assertNotNull(result)
        assertEquals("OMDB: OMDB Overview", result.overview)
    }

    @Test
    fun `returns null when both are unavailable`() = runTest {
        val broker = MovieDetailBroker(
            tmdb = FakeDetailSource(null),
            omdb = FakeDetailSource(null)
        )


        val result = broker.getMovieByTitle("Interstellar")

        assertNull(result)
    }

    @Test
    fun `handles source exception by treating it as null`() = runTest {
        val tmdbMovie = movie(id = 1, title = "Interstellar")

        val broker = MovieDetailBroker(
            tmdb = FakeDetailSource(tmdbMovie),
            omdb = ExceptionSource(IOException("Network error"))
        )

        val result = broker.getMovieByTitle("Interstellar")

        assertNotNull(result)
        assertEquals("TMDB: ${tmdbMovie.overview}", result.overview)
    }

    @Test
    fun `calls both sources in parallel`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        
        val source1 = DelayedSource(1000, dispatcher)
        val source2 = DelayedSource(1000, dispatcher)

        val broker = MovieDetailBroker(source1, source2)

        val deferred = async(dispatcher) {
            broker.getMovieByTitle("Movie")
        }

        advanceTimeBy(1000)
        advanceUntilIdle()

        deferred.await()

        assertEquals(1000, currentTime)
    }

    @Test
    fun `does not combine if titles are different`() = runTest {
        val tmdbMovie = movie(id = 1, title = "Interstellar", releaseDate = "2014-01-01")
        val omdbMovie = movie(id = 0, title = "Inception", releaseDate = "2014-01-01")

        val broker = MovieDetailBroker(
            tmdb = FakeDetailSource(tmdbMovie),
            omdb = FakeDetailSource(omdbMovie)
        )

        val result = broker.getMovieByTitle("Interstellar")

        assertNotNull(result)
        assertEquals("TMDB: ${tmdbMovie.overview}", result.overview)
    }

    @Test
    fun `does not combine if years are different`() = runTest {
        val tmdbMovie = movie(id = 1, title = "Interstellar", releaseDate = "2014-01-01")
        val omdbMovie = movie(id = 0, title = "Interstellar", releaseDate = "2015-01-01")

        val broker = MovieDetailBroker(
            tmdb = FakeDetailSource(tmdbMovie),
            omdb = FakeDetailSource(omdbMovie)
        )

        val result = broker.getMovieByTitle("Interstellar")

        assertNotNull(result)
        assertEquals("TMDB: ${tmdbMovie.overview}", result.overview)
    }

}
