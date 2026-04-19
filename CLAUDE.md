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
# Compilar (sin tests)
.\gradlew.bat :composeApp:assemble

# Correr tests (la tarea se llama desktopTest, NO test)
.\gradlew.bat :composeApp:desktopTest

# Compilar y testear
.\gradlew.bat :composeApp:assemble :composeApp:desktopTest

# Limpiar y compilar
.\gradlew.bat clean :composeApp:assemble

# INCORRECTO — esta tarea no existe en este proyecto:
# .\gradlew.bat clean build -x test   
```

---

## Estado actual del proyecto

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
- **Paso 6**: Eliminar clases duplicadas del paquete raíz:
  - `Movie.kt` en raíz contiene `Movie`, `QualifiedMovie`, `RemoteResult`, `RemoteMovie` — todas deben eliminarse una vez que la capa presentation use las de `domain/model/`
  - `RemoteMovie`/`RemoteResult` ya existen como `internal` en `ExternalMovieDataSource.kt`

### ⚠️ Conflicto de modelos a resolver en el Paso 4
El dominio y la capa de presentación usan distintas versiones de `QualifiedMovie`:

| Clase | Paquete | Campo |
|---|---|---|
| `QualifiedMovie` | `edu.dyds.movies` (raíz) | `isGoodMovie: Boolean` |
| `QualifiedMovie` | `edu.dyds.movies.domain.model` | `qualityLabel: String` |

Al migrar `MoviesViewModel` para usar `GetMoviesUseCase`, `HomeScreen` debe adaptarse para renderizar usando `qualityLabel` en lugar de `isGoodMovie`.

---

## Skills

### Kotlin Conventions
- Usá `data class` para modelos sin comportamiento
- Preferí `sealed class` sobre `when` con strings para estados y tipos de eventos
- Inyectá dependencias por constructor, nunca instancies con `= ConcreteClass()` dentro de una clase
- Usá interfaces para todo lo que el diagrama muestre como abstracción (Repo, CanalNotificacion, etc.)
- Nombrá interfaces como sustantivos (`Repositorio`, `CanalNotificacion`), no como `IRepositorio`

### Architecture — Event Bus Pattern
- `Sesion` solo emite eventos al Bus; nunca llama directamente a módulos como Ranking, Torneo o Estadísticas
- Los eventos se modelan como `sealed class Evento`: `data class SesionTerminada(...)`, `data class JugadorConectado(...)`, etc.
- Cada módulo (ModuloRanking, ModuloTorneo, ModuloEstadisticas) se suscribe al Bus de forma independiente
- El Bus no conoce a los suscriptores; los suscriptores se registran a sí mismos

### Architecture — SOLID
- SRP: cada clase tiene una sola razón para cambiar. Si una clase guarda, notifica Y actualiza rankings, está violando SRP
- OCP: extendé comportamiento agregando clases nuevas, no modificando `when` existentes
- DIP: las clases de alto nivel (Sesion, ApiRest) dependen de interfaces, nunca de implementaciones concretas
- Cuando veas un `when (tipo)` o `when (formato)` con strings, es señal de que falta una abstracción

### Testing
- El proyecto usa fakes (objetos de prueba manuales) en lugar de MockK — ver `TestExample.kt` como referencia
- La tarea de tests es `desktopTest`, no `test`: `.\gradlew.bat :composeApp:desktopTest`
- Cada test verifica una sola cosa; si el nombre del test tiene "y", probablemente son dos tests
- Nombrá los tests como: `` fun `dado X cuando Y entonces Z`() ``
- Cuando se migre `MoviesViewModel`, testearlo con fakes de `GetMoviesUseCase` y `GetMovieDetailUseCase`

### Naming
- Clases en PascalCase, funciones y variables en camelCase
- Los métodos que emiten eventos: `emitirSesionTerminada()`, no `terminar()`
- Los métodos de repositorio: `guardar()`, `cargar()`, `eliminar()` — sin prefijo `do` ni sufijo `Data`
- Evitá nombres genéricos: `Manager`, `Handler`, `Helper`, `Utils` son señales de SRP violado

