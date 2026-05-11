package edu.dyds.movies.presentation.home

import edu.dyds.movies.domain.model.QualifiedMovie
import edu.dyds.movies.domain.usecase.GetMoviesUseCase

class FakeGetMoviesUseCase : GetMoviesUseCase {
    var result: List<QualifiedMovie> = emptyList()
    var shouldThrowError = false

    override suspend fun invoke(): List<QualifiedMovie> {
        if (shouldThrowError) throw Exception("UseCase error")
        return result
    }
}
