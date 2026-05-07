package edu.dyds.movies.di

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.dyds.movies.data.MoviesRepositoryImpl
import edu.dyds.movies.data.external.ExternalMovieDataSourceImpl
import edu.dyds.movies.data.local.LocalMovieDataSourceImpl
import edu.dyds.movies.domain.usecase.GetMovieDetailUseCaseImpl
import edu.dyds.movies.domain.usecase.GetMoviesUseCaseImpl
import edu.dyds.movies.presentation.detail.DetailViewModel
import edu.dyds.movies.presentation.home.HomeViewModel
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

private const val API_KEY = "d18da1b5da16397619c688b0263cd281"

object MoviesDependencyInjector {

    private val httpClient =
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
            install(DefaultRequest) {
                url {
                    protocol = URLProtocol.HTTP
                    host = "api.themoviedb.org"
                    parameters.append("api_key", API_KEY)
                }
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 5000
            }
        }

    private val localDataSource = LocalMovieDataSourceImpl()
    private val externalDataSource = ExternalMovieDataSourceImpl(httpClient)

    private val repository = MoviesRepositoryImpl(localDataSource, externalDataSource)

    private val getMoviesUseCase = GetMoviesUseCaseImpl(repository)
    private val getMovieDetailUseCase = GetMovieDetailUseCaseImpl(repository)

    @Composable
    fun provideHomeViewModel(): HomeViewModel {
        return viewModel { HomeViewModel(getMoviesUseCase) }
    }

    @Composable
    fun provideDetailViewModel(): DetailViewModel {
        return viewModel { DetailViewModel(getMovieDetailUseCase) }
    }
}