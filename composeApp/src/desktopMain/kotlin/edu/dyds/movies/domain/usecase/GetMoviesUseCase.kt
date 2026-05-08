package edu.dyds.movies.domain.usecase

import edu.dyds.movies.domain.model.QualifiedMovie

interface GetMoviesUseCase {
    suspend operator fun invoke(): List<QualifiedMovie>
}
