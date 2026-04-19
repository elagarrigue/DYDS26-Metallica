# Contexto del proyecto

Aplicación desktop de películas construida con Kotlin y Compose Multiplatform.
Permite listar películas, verlas ordenadas por puntaje y consultar el detalle de cada una.

## Stack técnico
- Kotlin + Compose Multiplatform (target: desktop)
- Coroutines + StateFlow para manejo de estado asíncrono
- Inyección de dependencias manual (sin Koin, Hilt ni similar)
- Paquete base: `edu.dyds.movies`

## Arquitectura
Clean Architecture con cuatro capas: `presentation`, `domain`, `data` y `di`.
Las dependencias siempre apuntan hacia `domain`. Ninguna capa conoce a la que está por encima de ella.

---

## Comandos de build

```powershell
.\gradlew.bat :composeApp:assemble                               # compilar
.\gradlew.bat :composeApp:desktopTest                            # correr tests
.\gradlew.bat :composeApp:assemble :composeApp:desktopTest       # ambos
.\gradlew.bat clean :composeApp:assemble                         # limpiar y compilar
```

> La tarea de tests es `desktopTest`, no `test`. `build -x test` falla en este proyecto.

---

## Estado actual de implementación

### Completado
- **Paso 1**: Estructura de directorios creada
- **Paso 2**: Capa `domain/` refactorizada
  - `domain/model/Movie.kt` — entidad pura de dominio
  - `domain/model/QualifiedMovie.kt` — usa `qualityLabel: String` (ej. "Top rated", "Good", "Average")
  - `domain/repository/MovieRepository.kt` — interfaz con `getMovies()` y `getMovieById(id)`
  - `domain/usecase/GetMoviesUseCase.kt` y `GetMovieDetailUseCase.kt`
- **Paso 3**: Capa `data/` implementada
  - `data/local/LocalMovieDataSource.kt` — caché en memoria
  - `data/external/ExternalMovieDataSource.kt` — consume API TMDB; DTOs `RemoteMovie`/`RemoteResult` son `internal`
  - `data/repository/MovieRepositoryImpl.kt` — estrategia cache-first

### Pendiente
- **Paso 4**: `presentation/` — los siguientes archivos todavía están en el paquete raíz y deben moverse/refactorizarse:
  - `MoviesViewModel.kt` — aún usa `HttpClient` directo en lugar de los use cases del dominio; usa el `Movie`/`QualifiedMovie` del raíz, no los de `domain/model/`
  - `HomeScreen.kt` — usa `QualifiedMovie.isGoodMovie: Boolean` (raíz) en lugar de `QualifiedMovie.qualityLabel: String` (dominio); tiene un `painterResource` deprecated (migrar a Compose resources library)
  - `DetailScreen.kt`, `Navigation.kt`, `CommonComposables.kt`, `App.kt`
- **Paso 5**: `di/` — `MoviesDependencyInjector.kt` todavía está en la raíz; debe inyectar `LocalMovieDataSource`, `ExternalMovieDataSource`, `MovieRepositoryImpl` y los use cases
- **Paso 6**: Eliminar clases duplicadas del paquete raíz una vez que la presentación use `domain/model/`:
  - `Movie.kt` en raíz contiene `Movie`, `QualifiedMovie`, `RemoteResult`, `RemoteMovie` — todas a eliminar
  - `RemoteMovie`/`RemoteResult` ya existen como `internal` en `ExternalMovieDataSource.kt`

### ⚠️ Conflicto de modelos a resolver en el Paso 4

| Clase | Paquete | Campo |
|---|---|---|
| `QualifiedMovie` | `edu.dyds.movies` (raíz) | `isGoodMovie: Boolean` |
| `QualifiedMovie` | `edu.dyds.movies.domain.model` | `qualityLabel: String` |

Al migrar `MoviesViewModel` para usar `GetMoviesUseCase`, `HomeScreen` debe adaptarse para renderizar usando `qualityLabel` en lugar de `isGoodMovie`.

---

## Convenciones de código

- Usá `data class` para modelos sin comportamiento
- Preferí `sealed class` sobre `when` con strings para estados y tipos de eventos
- Inyectá dependencias por constructor, nunca instancies con `= ConcreteClass()` dentro de una clase
- Usá interfaces para todo lo que el diagrama muestre como abstracción
- Nombrá interfaces como sustantivos (`MovieRepository`), no como `IMovieRepository`
- Clases en PascalCase, funciones y variables en camelCase
- Evitá nombres genéricos: `Manager`, `Handler`, `Helper`, `Utils` son señales de SRP violado

## Reglas de arquitectura

- Las clases de alto nivel dependen de interfaces, nunca de implementaciones concretas (DIP)
- Cada clase tiene una sola razón para cambiar — si guarda, notifica Y actualiza rankings, viola SRP
- Extendé comportamiento agregando clases nuevas, no modificando `when` existentes (OCP)
- Un `when (tipo)` con strings es señal de que falta una abstracción

## Testing

- El proyecto usa fakes manuales, no MockK — ver `TestExample.kt` como referencia
- Cada test verifica una sola cosa; si el nombre tiene "y", probablemente son dos tests
- Nombrá los tests como: `` fun `dado X cuando Y entonces Z`() ``
- Al migrar `MoviesViewModel`, testearlo con fakes de `GetMoviesUseCase` y `GetMovieDetailUseCase`