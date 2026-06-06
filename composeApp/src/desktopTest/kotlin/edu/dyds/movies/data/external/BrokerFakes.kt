package edu.dyds.movies.data.external

import edu.dyds.movies.domain.model.Movie
import edu.dyds.movies.movie
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class FakeDetailSource(private val movie: Movie?) : MovieDetailExternalSource {
    override suspend fun getMovieByTitle(title: String): Movie? = movie
}

class ExceptionSource(private val exception: Exception) : MovieDetailExternalSource {
    override suspend fun getMovieByTitle(title: String): Movie? {
        throw exception
    }
}

class DelayedSource(
    private val delayMillis: Long,
    private val dispatcher: CoroutineDispatcher
) : MovieDetailExternalSource {
    override suspend fun getMovieByTitle(title: String): Movie? {
        withContext(dispatcher) {
            delay(delayMillis)
        }
        return movie(id = 1, title = title)
    }
}
