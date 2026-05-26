package edu.dyds.movies.di

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.dyds.movies.data.MoviesRepositoryImpl
import edu.dyds.movies.data.external.MovieDetailBroker
import edu.dyds.movies.data.external.omdb.OMDBMoviesExternalSource
import edu.dyds.movies.data.external.tmdb.TMDBMoviesExternalSource
import edu.dyds.movies.data.local.MoviesLocalDataSourceImpl
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

private const val TMDB_API_KEY = "d18da1b5da16397619c688b0263cd281"
private const val OMDB_API_KEY = "f86603a1"
private const val NETWORK_TIMEOUT_MS = 5000L

object MoviesDependencyInjector {

    private val tmdbHttpClient =
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
                    parameters.append("api_key", TMDB_API_KEY)
                }
            }
            install(HttpTimeout) {
                requestTimeoutMillis = NETWORK_TIMEOUT_MS
            }
        }

    private val omdbHttpClient =
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
            install(DefaultRequest) {
                url {
                    protocol = URLProtocol.HTTPS
                    host = "www.omdbapi.com"
                    parameters.append("apikey", OMDB_API_KEY)
                }
            }
            install(HttpTimeout) {
                requestTimeoutMillis = NETWORK_TIMEOUT_MS
            }
        }

    private val localDataSource = MoviesLocalDataSourceImpl()
    private val tmdbDataSource = TMDBMoviesExternalSource(tmdbHttpClient)
    private val omdbDataSource = OMDBMoviesExternalSource(omdbHttpClient)

    private val movieDetailBroker = MovieDetailBroker(
        tmdb = tmdbDataSource,
        omdb = omdbDataSource
    )

    private val repository = MoviesRepositoryImpl(
        localDataSource = localDataSource,
        listExternalSource = tmdbDataSource,
        detailExternalSource = movieDetailBroker
    )

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
