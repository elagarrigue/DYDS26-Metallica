# Plan de Refactorización — Arquitectura en Capas (Clean Architecture)

## Contexto

El proyecto actual concentra todos sus archivos Kotlin bajo un único paquete plano:

```
composeApp/src/desktopMain/kotlin/edu/dyds/movies/
├── App.kt
├── CommonComposables.kt
├── DetailScreen.kt
├── HomeScreen.kt
├── main.kt
├── Movie.kt
├── MoviesDependencyInjector.kt
├── MoviesViewModel.kt
└── Navigation.kt
```

Este diseño mezcla responsabilidades de presentación, dominio, datos e inyección de dependencias en un mismo paquete, lo que viola el principio de separación de responsabilidades (SRP) y dificulta la escalabilidad y el testing.

El objetivo es reorganizar el proyecto para reflejar la arquitectura propuesta en la imagen: cuatro módulos claramente delimitados — **presentation**, **domain**, **data** y **di** — donde cada capa solo depende de la inmediatamente inferior (o de las abstracciones definidas en domain).

---

## Estructura objetivo

```
composeApp/src/desktopMain/kotlin/edu/dyds/movies/
│
├── App.kt                        ← raíz (sin cambios de ubicación)
├── Navigation.kt                 ← raíz (sin cambios de ubicación)
│
├── presentation/
│   ├── utils/
│   │   └── CommonComposables.kt
│   ├── home/
│   │   ├── HomeScreen.kt
│   │   └── HomeViewModel.kt
│   └── detail/
│       ├── DetailScreen.kt
│       └── DetailViewModel.kt
│
├── domain/
│   ├── model/
│   │   ├── Movie.kt              ← entidad de dominio (solo datos de negocio)
│   │   └── QualifiedMovie.kt     ← entidad resultante del mapeo de negocio
│   ├── repository/
│   │   └── MovieRepository.kt   ← interfaz (contrato, sin implementación)
│   └── usecase/
│       ├── GetMoviesUseCase.kt
│       └── GetMovieDetailUseCase.kt
│
├── data/
│   ├── repository/
│   │   └── MovieRepositoryImpl.kt  ← implementa MovieRepository de domain
│   ├── local/
│   │   └── LocalMovieDataSource.kt
│   └── external/
│       └── ExternalMovieDataSource.kt
│
└── di/
    └── MoviesDependencyInjector.kt  ← reubicado desde la raíz
```

---

## Reglas de dependencia entre capas

| Capa         | Puede depender de | No puede depender de         |
|--------------|-------------------|------------------------------|
| presentation | domain            | data, di                     |
| domain       | (nada externo)    | presentation, data, di       |
| data         | domain            | presentation, di             |
| di           | todas             | — (es el punto de ensamblado)|

> **Principio fundamental:** las flechas de dependencia siempre apuntan hacia `domain`. La capa `data` implementa las interfaces que `domain` define; nunca al revés.

---

## Pasos del plan

### Paso 1 — Crear los paquetes vacíos

Crear la jerarquía de directorios según la estructura objetivo. No mover ni modificar ningún archivo todavía.

```
presentation/utils/
presentation/home/
presentation/detail/
domain/model/
domain/repository/
domain/usecase/
data/repository/
data/local/
data/external/
di/
```

---

### Paso 2 — Refactorizar la capa `domain`

#### 2.1 Mover y adaptar `Movie.kt` → `domain/model/Movie.kt`

El archivo actual `Movie.kt` probablemente mezcla datos crudos de API con lógica de presentación. En `domain`, `Movie` debe ser una **entidad pura** (data class sin anotaciones de serialización, sin referencias a frameworks).

**Antes** (ejemplo probable):
```kotlin
// edu.dyds.movies.Movie
data class Movie(
    val id: Int,
    val title: String,
    val voteAverage: Double,
    val posterPath: String?
    // posibles anotaciones de Gson/Retrofit aquí
)
```

**Después** — `domain/model/Movie.kt`:
```kotlin
package edu.dyds.movies.domain.model

data class Movie(
    val id: Int,
    val title: String,
    val voteAverage: Double,
    val posterPath: String?
)
```

#### 2.2 Crear `domain/model/QualifiedMovie.kt`

El mapeo "ordenar por voto y etiquetar como calificado" es lógica de negocio: vive en `domain`.

```kotlin
package edu.dyds.movies.domain.model

data class QualifiedMovie(
    val movie: Movie,
    val qualityLabel: String    // e.g. "Top rated", "Average", etc.
)
```

#### 2.3 Crear `domain/repository/MovieRepository.kt`

Interfaz pura: define el contrato sin revelar la fuente de datos.

```kotlin
package edu.dyds.movies.domain.repository

import edu.dyds.movies.domain.model.Movie

interface MovieRepository {
    suspend fun getMovies(): List<Movie>
    suspend fun getMovieById(id: Int): Movie?
}
```

#### 2.4 Crear los casos de uso en `domain/usecase/`

Cada caso de uso encapsula **una** acción de negocio (SRP). El ordenamiento y el mapeo a `QualifiedMovie` se realizan aquí.

`GetMoviesUseCase.kt`:
```kotlin
package edu.dyds.movies.domain.usecase

import edu.dyds.movies.domain.model.QualifiedMovie
import edu.dyds.movies.domain.repository.MovieRepository

class GetMoviesUseCase(private val repository: MovieRepository) {

    suspend operator fun invoke(): List<QualifiedMovie> {
        return repository.getMovies()
            .sortedByDescending { it.voteAverage }
            .map { movie ->
                val label = when {
                    movie.voteAverage >= 8.0 -> "Top rated"
                    movie.voteAverage >= 6.0 -> "Good"
                    else -> "Average"
                }
                QualifiedMovie(movie = movie, qualityLabel = label)
            }
    }
}
```

`GetMovieDetailUseCase.kt`:
```kotlin
package edu.dyds.movies.domain.usecase

import edu.dyds.movies.domain.model.Movie
import edu.dyds.movies.domain.repository.MovieRepository

class GetMovieDetailUseCase(private val repository: MovieRepository) {

    suspend operator fun invoke(id: Int): Movie? =
        repository.getMovieById(id)
}
```

---

### Paso 3 — Refactorizar la capa `data`

#### 3.1 Crear fuentes de datos locales y externas

`data/local/LocalMovieDataSource.kt` — caché o base de datos local:
```kotlin
package edu.dyds.movies.data.local

import edu.dyds.movies.domain.model.Movie

class LocalMovieDataSource {
    // Lógica de caché (Room, SQLDelight, memoria, etc.)
    suspend fun getMovies(): List<Movie> = emptyList()
}
```

`data/external/ExternalMovieDataSource.kt` — API remota:
```kotlin
package edu.dyds.movies.data.external

import edu.dyds.movies.domain.model.Movie

class ExternalMovieDataSource {
    // Lógica de red (Ktor, Retrofit, etc.)
    suspend fun getMovies(): List<Movie> = emptyList()
    suspend fun getMovieById(id: Int): Movie? = null
}
```

#### 3.2 Implementar `data/repository/MovieRepositoryImpl.kt`

Implementa la interfaz de `domain`. Decide si servir datos desde local o externo (estrategia cache-first, network-first, etc.).

```kotlin
package edu.dyds.movies.data.repository

import edu.dyds.movies.data.external.ExternalMovieDataSource
import edu.dyds.movies.data.local.LocalMovieDataSource
import edu.dyds.movies.domain.model.Movie
import edu.dyds.movies.domain.repository.MovieRepository

class MovieRepositoryImpl(
    private val local: LocalMovieDataSource,
    private val external: ExternalMovieDataSource
) : MovieRepository {

    override suspend fun getMovies(): List<Movie> {
        val cached = local.getMovies()
        return cached.ifEmpty { external.getMovies() }
    }

    override suspend fun getMovieById(id: Int): Movie? =
        external.getMovieById(id)
}
```

---

### Paso 4 — Refactorizar la capa `presentation`

#### 4.1 Mover `CommonComposables.kt` → `presentation/utils/CommonComposables.kt`

Actualizar el `package` al nuevo paquete. Sin cambios de lógica.

```kotlin
package edu.dyds.movies.presentation.utils
```

#### 4.2 Dividir `MoviesViewModel.kt` en ViewModels por pantalla

El ViewModel actual probablemente maneja tanto la lista como el detalle. Separarlo respeta SRP y facilita el testing.

`presentation/home/HomeViewModel.kt`:
```kotlin
package edu.dyds.movies.presentation.home

import edu.dyds.movies.domain.model.QualifiedMovie
import edu.dyds.movies.domain.usecase.GetMoviesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel(private val getMovies: GetMoviesUseCase) {

    // UiState: lo que la View observa (flujo unidireccional)
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState

    // UiEvent: acciones que la View envía al ViewModel
    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.LoadMovies -> loadMovies()
        }
    }

    private fun loadMovies() {
        // Lanzar coroutine y actualizar _uiState
    }
}

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val movies: List<QualifiedMovie>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

sealed interface HomeUiEvent {
    data object LoadMovies : HomeUiEvent
}
```

`presentation/detail/DetailViewModel.kt`:
```kotlin
package edu.dyds.movies.presentation.detail

import edu.dyds.movies.domain.model.Movie
import edu.dyds.movies.domain.usecase.GetMovieDetailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DetailViewModel(private val getMovieDetail: GetMovieDetailUseCase) {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState

    fun onEvent(event: DetailUiEvent) {
        when (event) {
            is DetailUiEvent.LoadDetail -> loadDetail(event.movieId)
        }
    }

    private fun loadDetail(id: Int) {
        // Lanzar coroutine y actualizar _uiState
    }
}

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Success(val movie: Movie) : DetailUiState
    data class Error(val message: String) : DetailUiState
}

sealed interface DetailUiEvent {
    data class LoadDetail(val movieId: Int) : DetailUiEvent
}
```

#### 4.3 Mover screens a sus paquetes

- `HomeScreen.kt` → `presentation/home/HomeScreen.kt`
- `DetailScreen.kt` → `presentation/detail/DetailScreen.kt`

Actualizar `package` e imports en cada archivo.

#### 4.4 Mantener `App.kt` y `Navigation.kt` en la raíz

Estos composables son el punto de entrada de la UI y pueden permanecer en `edu.dyds.movies` directamente, ya que actúan como "pegamento" entre Navigation y las pantallas.

---

### Paso 5 — Refactorizar la capa `di`

#### 5.1 Mover `MoviesDependencyInjector.kt` → `di/MoviesDependencyInjector.kt`

El inyector es el único lugar del proyecto que **conoce todas las capas** y ensambla el grafo de dependencias. Debe instanciar las implementaciones concretas de `data` e inyectarlas a través de las interfaces de `domain`.

```kotlin
package edu.dyds.movies.di

import edu.dyds.movies.data.external.ExternalMovieDataSource
import edu.dyds.movies.data.local.LocalMovieDataSource
import edu.dyds.movies.data.repository.MovieRepositoryImpl
import edu.dyds.movies.domain.usecase.GetMovieDetailUseCase
import edu.dyds.movies.domain.usecase.GetMoviesUseCase
import edu.dyds.movies.presentation.detail.DetailViewModel
import edu.dyds.movies.presentation.home.HomeViewModel

object MoviesDependencyInjector {

    private val localDataSource = LocalMovieDataSource()
    private val externalDataSource = ExternalMovieDataSource()

    // data → domain (a través de la interfaz)
    private val repository = MovieRepositoryImpl(localDataSource, externalDataSource)

    // domain
    private val getMoviesUseCase = GetMoviesUseCase(repository)
    private val getMovieDetailUseCase = GetMovieDetailUseCase(repository)

    // presentation
    fun provideHomeViewModel() = HomeViewModel(getMoviesUseCase)
    fun provideDetailViewModel() = DetailViewModel(getMovieDetailUseCase)
}
```

> **Nota SOLID — DIP:** `MoviesDependencyInjector` es el único lugar donde aparece `MovieRepositoryImpl`. El resto del código solo conoce la interfaz `MovieRepository`. Esto respeta el Principio de Inversión de Dependencias.

---

### Paso 6 — Actualizar imports y referencias

Tras mover cada archivo a su nuevo paquete, actualizar:

- Todos los `package` declarations al nuevo paquete.
- Todos los `import` statements en archivos que los referencian.
- El archivo `main.kt` para que use `MoviesDependencyInjector` desde su nuevo paquete `di`.

En Android Studio / IntelliJ, el refactor **Move** (Refactor → Move) actualiza los imports automáticamente; usar esa opción para evitar errores manuales.

---

### Paso 7 — Verificación final

Checklist para validar que la refactorización respeta la arquitectura:

- [ ] Ningún archivo en `domain/` importa clases de `presentation/` o `data/`.
- [ ] Ningún archivo en `presentation/` importa clases de `data/` o `di/`.
- [ ] Ningún archivo en `data/` importa clases de `presentation/` o `di/`.
- [ ] Solo `di/` conoce las implementaciones concretas de `data/`.
- [ ] El ordenamiento por voto y el mapeo a `QualifiedMovie` están exclusivamente en un `UseCase` dentro de `domain/`.
- [ ] Cada `Screen` recibe su `ViewModel` correspondiente como parámetro (no lo instancia directamente).
- [ ] El proyecto compila sin errores.
- [ ] Los tests existentes en `desktopTest/` siguen pasando.

---

## Resumen de cambios por archivo

| Archivo original                  | Nuevo destino                                  | Acción        |
|-----------------------------------|------------------------------------------------|---------------|
| `Movie.kt`                        | `domain/model/Movie.kt`                        | Mover + limpiar |
| `MoviesViewModel.kt`              | `presentation/home/HomeViewModel.kt`           | Dividir       |
|                                   | `presentation/detail/DetailViewModel.kt`       | Dividir       |
| `HomeScreen.kt`                   | `presentation/home/HomeScreen.kt`              | Mover         |
| `DetailScreen.kt`                 | `presentation/detail/DetailScreen.kt`          | Mover         |
| `CommonComposables.kt`            | `presentation/utils/CommonComposables.kt`      | Mover         |
| `MoviesDependencyInjector.kt`     | `di/MoviesDependencyInjector.kt`               | Mover + adaptar |
| `App.kt`                          | `App.kt` (raíz, sin cambio)                    | Solo imports  |
| `Navigation.kt`                   | `Navigation.kt` (raíz, sin cambio)             | Solo imports  |
| `main.kt`                         | `main.kt` (raíz, sin cambio)                   | Solo imports  |
| *(nuevo)* `QualifiedMovie.kt`     | `domain/model/QualifiedMovie.kt`               | Crear         |
| *(nuevo)* `MovieRepository.kt`    | `domain/repository/MovieRepository.kt`         | Crear         |
| *(nuevo)* `GetMoviesUseCase.kt`   | `domain/usecase/GetMoviesUseCase.kt`           | Crear         |
| *(nuevo)* `GetMovieDetailUseCase.kt` | `domain/usecase/GetMovieDetailUseCase.kt`   | Crear         |
| *(nuevo)* `MovieRepositoryImpl.kt`| `data/repository/MovieRepositoryImpl.kt`       | Crear         |
| *(nuevo)* `LocalMovieDataSource.kt` | `data/local/LocalMovieDataSource.kt`         | Crear         |
| *(nuevo)* `ExternalMovieDataSource.kt` | `data/external/ExternalMovieDataSource.kt` | Crear        |
