# Plan de Integración OMDB — v3 (corregido y consolidado)

## Objetivo

Integrar OMDB como segunda fuente de detalle de películas respetando SOLID, evitando violaciones de ISP y agregando un broker que combine información de TMDB y OMDB de forma segura.

---

# Arquitectura Final Esperada

```text
presentation/
  Navigation.kt
  detail/DetailViewModel.kt

domain/
  model/Movie.kt
  repository/MovieRepository.kt
  usecase/

data/
  MoviesListExternalSource.kt
  MovieDetailExternalSource.kt
  MoviesLocalDataSource.kt
  MoviesRepositoryImpl.kt
  external/
    MovieDetailBroker.kt
    tmdb/
      TMDBMoviesExternalSource.kt
      TMDBMovieMapper.kt
      TMDBRemoteModels.kt
    omdb/
      OMDBMoviesExternalSource.kt
  local/
    MoviesLocalDataSourceImpl.kt

di/
  MoviesDependencyInjector.kt
```

---

# COMMIT 1 — Reemplazar búsqueda por ID por búsqueda por título

## Objetivo

Eliminar la dependencia de IDs para obtener detalles y pasar a búsquedas por título.

---

## `data/MoviesRemoteDataSource.kt`

```kotlin
interface MoviesRemoteDataSource {
    suspend fun getMovies(): List<Movie>
    suspend fun getMovieByTitle(title: String): Movie?
}
```

Eliminar:

```kotlin
suspend fun getMovieById(id: Int): Movie?
```

---

## `data/MoviesLocalDataSource.kt`

> No eliminar completamente el lookup local de detalle.
> Debe reemplazarse por búsqueda por título.

```kotlin
interface MoviesLocalDataSource {
    suspend fun getMovies(): List<Movie>
    suspend fun getMovieByTitle(title: String): Movie?
    suspend fun saveMovies(movies: List<Movie>)
    suspend fun saveMovie(movie: Movie)
}
```

---

## `data/external/MoviesRemoteDataSourceImpl.kt`

```kotlin
override suspend fun getMovieByTitle(title: String): Movie? {
    return try {
        val result = httpClient.get("/3/search/movie") {
            parameter("query", title)
        }.body<RemoteResult>()

        result.results.firstOrNull()?.let {
            MovieMapper.toDomain(it)
        }
    } catch (e: CancellationException) {
        throw e
    } catch (_: IOException) {
        null
    } catch (_: ClientRequestException) {
        null
    } catch (_: ServerResponseException) {
        null
    }
}
```

No capturar `Exception` genérico.
Errores de serialización y bugs deben propagarse.

---

## `domain/repository/MovieRepository.kt`

```kotlin
interface MovieRepository {
    suspend fun getMovies(): List<Movie>
    suspend fun getMovieByTitle(title: String): Movie?
}
```

---

## `data/MoviesRepositoryImpl.kt`

```kotlin
override suspend fun getMovieByTitle(title: String): Movie? {
    val remoteMovie = remoteDataSource.getMovieByTitle(title)

    if (remoteMovie != null) {
        localDataSource.saveMovie(remoteMovie)
        return remoteMovie
    }

    return localDataSource.getMovieByTitle(title)
}
```

El fallback local es obligatorio para mantener soporte offline y cache de detalle.

---

## `domain/usecase/GetMovieDetailUseCase.kt`

```kotlin
interface GetMovieDetailUseCase {
    suspend operator fun invoke(title: String): Movie?
}
```

---

## `presentation/detail/DetailViewModel.kt`

```kotlin
private fun loadDetail(title: String) {
    if (title.isBlank()) {
        _state.value = DetailState.Error("No movie title provided")
        return
    }

    viewModelScope.launch {
        //...
    }
}
```

---

## `presentation/Navigation.kt`

Si el proyecto es Android-only:

```kotlin
navController.navigate(
    "$DETAIL_ROUTE?title=${URLEncoder.encode(movie.title, "UTF-8")}"
)
```

Si el proyecto es KMP real:

- no usar `java.net.URLEncoder`
- usar una solución multiplatform
- o serialización de argumentos propia de navegación

---

# COMMIT 2 — Mover TMDB a `data/external/tmdb/`

## Objetivo

Separar explícitamente la implementación TMDB.

---

## Estructura final

```text
data/external/tmdb/
  TMDBMoviesExternalSource.kt
  TMDBMovieMapper.kt
  TMDBRemoteModels.kt
```

---

## `TMDBMoviesExternalSource.kt`

En este commit todavía implementa `MoviesRemoteDataSource`.
La separación ISP ocurre recién en el Commit 3.

```kotlin
class TMDBMoviesExternalSource(
    private val httpClient: HttpClient
) : MoviesRemoteDataSource
```

---

# COMMIT 3 — Separar interfaces (ISP)

## Objetivo

Evitar que OMDB tenga que implementar lista.

---

## Crear `MoviesListExternalSource.kt`

```kotlin
interface MoviesListExternalSource {
    suspend fun getMovies(): List<Movie>
}
```

---

## Crear `MovieDetailExternalSource.kt`

```kotlin
interface MovieDetailExternalSource {
    suspend fun getMovieByTitle(title: String): Movie?
}
```

---

## `TMDBMoviesExternalSource.kt`

```kotlin
class TMDBMoviesExternalSource(
    private val httpClient: HttpClient
) : MoviesListExternalSource,
    MovieDetailExternalSource
```

---

## `MoviesRepositoryImpl.kt`

```kotlin
class MoviesRepositoryImpl(
    private val localDataSource: MoviesLocalDataSource,
    private val listExternalSource: MoviesListExternalSource,
    private val detailExternalSource: MovieDetailExternalSource
) : MovieRepository {

    override suspend fun getMovies(): List<Movie> {
        val local = localDataSource.getMovies()

        if (local.isNotEmpty()) {
            return local
        }

        val remote = listExternalSource.getMovies()
        localDataSource.saveMovies(remote)
        return remote
    }

    override suspend fun getMovieByTitle(title: String): Movie? {
        val remoteMovie = detailExternalSource.getMovieByTitle(title)

        if (remoteMovie != null) {
            localDataSource.saveMovie(remoteMovie)
            return remoteMovie
        }

        return localDataSource.getMovieByTitle(title)
    }
}
```

---

# COMMIT 4 — Agregar OMDB

## Objetivo

Agregar una segunda fuente de detalle.

---

## Estructura correcta

```text
data/external/omdb/
  OMDBMoviesExternalSource.kt
```

No existe un archivo separado `OMDBMovieMapper.kt`.

---

## `OMDBMoviesExternalSource.kt`

> Este archivo contiene:
>
> - `OMDBMoviesExternalSource`
> - `OMDBMovie`
> - `private object OMDBMovieMapper`
>
> No debe existir ninguna segunda definición de `OMDBMoviesExternalSource`.

```kotlin
package edu.dyds.movies.data.external.omdb

import edu.dyds.movies.data.MovieDetailExternalSource
import edu.dyds.movies.domain.model.Movie
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.get
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.IOException

class OMDBMoviesExternalSource(
    private val httpClient: HttpClient
) : MovieDetailExternalSource {

    override suspend fun getMovieByTitle(title: String): Movie? {
        return try {
            val result = httpClient.get("/") {
                parameter("t", title)
            }.body<OMDBMovie>()

            if (result.response == "False") {
                null
            } else {
                OMDBMovieMapper.toDomain(result)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: IOException) {
            null
        } catch (_: ClientRequestException) {
            null
        } catch (_: ServerResponseException) {
            null
        }
    }
}

@Serializable
data class OMDBMovie(
    @SerialName("Title") val title: String = "",
    @SerialName("Plot") val plot: String = "",
    @SerialName("Released") val released: String = "",
    @SerialName("Poster") val poster: String = "",
    @SerialName("imdbRating") val imdbRating: String = "",
    @SerialName("imdbID") val imdbId: String = "",
    @SerialName("Response") val response: String = "False"
)

private object OMDBMovieMapper {

    fun toDomain(remote: OMDBMovie): Movie {
        return Movie(
            externalId = remote.imdbId,
            id = 0,
            title = remote.title,
            overview = remote.plot,
            releaseDate = remote.released,
            poster = remote.poster.takeIf { it != "N/A" },
            backdrop = null,
            originalTitle = remote.title,
            originalLanguage = "",
            popularity = 0.0,
            voteAverage = remote.imdbRating.toDoubleOrNull() ?: 0.0
        )
    }
}
```

---

## Restricciones importantes

### `Movie.poster`

`Movie.poster` debe ser `String?`.

Si actualmente es `String`, modificar el modelo de dominio.

---

### `Movie.externalId`

Para evitar colisiones y dependencia de `hashCode()`, el modelo de dominio debe agregar:

```kotlin
val externalId: String?
```

TMDB puede dejarlo en `null`.
OMDB debe usar `imdbID`.

Esto evita IDs sintéticos inestables y elimina riesgo de colisiones persistentes.

---

# COMMIT 5 — Agregar `MovieDetailBroker`

## Objetivo

Consultar TMDB y OMDB en paralelo y combinar resultados.

---

## Reglas de negocio

| Escenario | Resultado |
|---|---|
| Ambos válidos y compatibles | Combinar |
| Solo TMDB | Retornar TMDB |
| Solo OMDB | Retornar OMDB |
| Ninguno | `null` |

---

## `MovieDetailBroker.kt`

Imports requeridos:

```kotlin
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeout
import java.io.IOException
```

```kotlin
class MovieDetailBroker(
    private val tmdb: MovieDetailExternalSource,
    private val omdb: MovieDetailExternalSource
) : MovieDetailExternalSource {

    override suspend fun getMovieByTitle(title: String): Movie? = withTimeout(6000) {
        coroutineScope {

        val tmdbDeferred = async {
            try {
                tmdb.getMovieByTitle(title)
            } catch (e: CancellationException) {
                throw e
            } catch (_: IOException) {
                null
            } catch (_: ClientRequestException) {
                null
            } catch (_: ServerResponseException) {
                null
            }
        }

        val omdbDeferred = async {
            try {
                omdb.getMovieByTitle(title)
            } catch (e: CancellationException) {
                throw e
            } catch (_: IOException) {
                null
            } catch (_: ClientRequestException) {
                null
            } catch (_: ServerResponseException) {
                null
            }
        }

        val tmdbMovie = tmdbDeferred.await()
        val omdbMovie = omdbDeferred.await()

        when {
            tmdbMovie != null &&
                omdbMovie != null &&
                canCombine(tmdbMovie, omdbMovie) -> {
                combine(tmdbMovie, omdbMovie)
            }

            tmdbMovie != null -> {
                tmdbMovie.copy(
                    overview = "TMDB: ${tmdbMovie.overview}"
                )
            }

            omdbMovie != null -> {
                omdbMovie.copy(
                    overview = "OMDB: ${omdbMovie.overview}"
                )
            }

            else -> null
        }
    }
}

    private fun canCombine(
        tmdb: Movie,
        omdb: Movie
    ): Boolean {

        val sameTitle = tmdb.title.equals(
            omdb.title,
            ignoreCase = true
        )

        val tmdbYear = tmdb.releaseDate.take(4)
        val omdbYear = omdb.releaseDate.take(4)

        val sameYear =
            tmdbYear.isNotBlank() &&
            omdbYear.isNotBlank() &&
            tmdbYear == omdbYear

        return sameTitle && sameYear
    }

    private fun normalizePoster(
        poster: String?
    ): String? {

        if (poster.isNullOrBlank()) {
            return null
        }

        return if (poster.startsWith("http")) {
            poster
        } else {
            "https://image.tmdb.org/t/p/w500$poster"
        }
    }

    private fun combine(
        tmdb: Movie,
        omdb: Movie
    ): Movie {
        return tmdb.copy(
            poster = normalizePoster(
                omdb.poster
            ) ?: normalizePoster(
                tmdb.poster
            ),
            voteAverage = if (
                omdb.voteAverage > 0.0
            ) {
                omdb.voteAverage
            } else {
                tmdb.voteAverage
            }
        )
    }
}
```

---

## Decisiones explícitas de combinación

| Campo | Fuente ganadora |
|---|---|
| `id` | TMDB |
| `title` | TMDB |
| `overview` | TMDB |
| `backdrop` | TMDB |
| `popularity` | TMDB |
| `voteAverage` | OMDB |
| `poster` | OMDB si existe |

---

# Tests obligatorios

---

## Broker

Debe verificarse:

- combinación correcta
- fallback individual
- excepciones de una fuente
- excepciones de ambas
- paralelismo real
- validación de títulos incompatibles
- prioridad de poster
- prioridad de voteAverage

---

## Test de paralelismo

Evitar tests basados estrictamente en tiempo real porque pueden ser flaky.

Usar `StandardTestDispatcher` y tiempo virtual.

```kotlin
@Test
fun `calls both sources in parallel`() = runTest {

    val dispatcher = StandardTestDispatcher(testScheduler)

    val source1 = DelayedSource(
        delayMillis = 1000,
        dispatcher = dispatcher
    )

    val source2 = DelayedSource(
        delayMillis = 1000,
        dispatcher = dispatcher
    )

    val broker = MovieDetailBroker(
        source1,
        source2
    )

    val deferred = async(dispatcher) {
        broker.getMovieByTitle("Movie")
    }

    advanceTimeBy(1000)
    advanceUntilIdle()

    deferred.await()

    // Si fuese secuencial todavía faltaría avanzar tiempo.
    assertEquals(1000, currentTime)
}
```

---

# Resumen de Commits

| # | Commit | SOLID |
|---|---|---|
| 1 | Reemplazar búsqueda por ID por búsqueda por título | SRP |
| 2 | Separar TMDB en su propio paquete | SRP |
| 3 | Dividir interfaces de lista y detalle | ISP |
| 4 | Agregar OMDB con mapper privado consolidado | OCP |
| 5 | Agregar broker paralelo de detalle | DIP |

---

# Problemas corregidos en esta versión

| ID | Corrección |
|---|---|
| R14 | Recuperado fallback local de detalle |
| R15 | Eliminado `catch (Exception)` excesivamente amplio |
| R16 | Validación de títulos antes de combinar |
| R17 | Eliminada colisión de `id = 0` en OMDB |
| R18 | Definida prioridad explícita de posters |
| R19 | Agregado test real de paralelismo |
| R20 | Aclarada incompatibilidad KMP de `URLEncoder` |
| Duplicación | Eliminada segunda definición residual de `OMDBMoviesExternalSource` |
| Inconsistencia | Eliminado encabezado falso `OMDBMovieMapper.kt` |
| Resumen | Actualizada tabla de commits para reflejar mapper privado |

