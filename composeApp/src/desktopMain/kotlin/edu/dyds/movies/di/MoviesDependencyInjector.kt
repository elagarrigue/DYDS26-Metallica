package edu.dyds.movies.di

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.dyds.movies.data.external.ExternalMovieDataSource
import edu.dyds.movies.data.local.LocalMovieDataSource
import edu.dyds.movies.data.repository.MovieRepositoryImpl
import edu.dyds.movies.domain.usecase.GetMovieDetailUseCase
import edu.dyds.movies.domain.usecase.GetMoviesUseCase
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
                    protocol = URLProtocol.HTTPS
                    host = "api.themoviedb.org"
                    parameters.append("api_key", API_KEY)
                }
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 5000
            }
        }

    private val localDataSource = LocalMovieDataSource()
    private val externalDataSource = ExternalMovieDataSource(httpClient)

    // data → domain (a través de la interfaz)
    private val repository = MovieRepositoryImpl(localDataSource, externalDataSource)

    // domain
    private val getMoviesUseCase = GetMoviesUseCase(repository)
    private val getMovieDetailUseCase = GetMovieDetailUseCase(repository)

    // presentation
    @Composable
    fun provideHomeViewModel(): HomeViewModel {
        return viewModel { HomeViewModel(getMoviesUseCase) }
    }

    @Composable
    fun provideDetailViewModel(): DetailViewModel {
        return viewModel { DetailViewModel(getMovieDetailUseCase) }
    }
}