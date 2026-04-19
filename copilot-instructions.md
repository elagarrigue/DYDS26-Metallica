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

# Estado actual del proyecto y sus tareas pendientes
### Completado
- Paso 1: Estructura de directorios creada
- Paso 2: Capa `domain/` refactorizada
    - `domain/model/Movie.kt` y `QualifiedMovie.kt` existen y son entidades puras
    - `domain/repository/MovieRepository.kt` existe como interfaz
    - `domain/usecase/GetMoviesUseCase.kt` y `GetMovieDetailUseCase.kt` existen

### Pendiente (archivos todavía en el paquete raíz)
- Paso 3: `data/` — `MovieRepositoryImpl`, `LocalMovieDataSource`, `ExternalMovieDataSource` aún NO existen
- Paso 4: `presentation/` — `HomeViewModel`, `DetailViewModel`, `HomeScreen`, `DetailScreen` aún NO fueron movidos
- Paso 5: `di/` — `MoviesDependencyInjector` todavía está en la raíz
- Paso 6: imports y referencias sin actualizar

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
- Testeá `Sesion` con mocks de `Repositorio` y `CanalNotificacion`, nunca con las implementaciones reales
- Usá MockK para mockear en Kotlin: `val repo = mockk<Repositorio>()`
- Cada test verifica una sola cosa; si el nombre del test tiene "y", probablemente son dos tests
- Nombrá los tests como: `fun `dado X cuando Y entonces Z`()`

### Naming
- Clases en PascalCase, funciones y variables en camelCase
- Los métodos que emiten eventos: `emitirSesionTerminada()`, no `terminar()`
- Los métodos de repositorio: `guardar()`, `cargar()`, `eliminar()` — sin prefijo `do` ni sufijo `Data`
- Evitá nombres genéricos: `Manager`, `Handler`, `Helper`, `Utils` son señales de SRP violado
- ## Estado de implementación (actualizar a medida que avanza)

