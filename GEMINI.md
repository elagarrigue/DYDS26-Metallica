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
- **Paso 4**: Capa `presentation/` refactorizada
    - `MoviesViewModel.kt` dividido en `HomeViewModel.kt` y `DetailViewModel.kt`.
    - `HomeScreen.kt` y `DetailScreen.kt` movidos a sus respectivos paquetes.
    - `CommonComposables.kt` movido a `presentation/utils/`.
    - `HomeScreen.kt` actualizado para usar `qualityLabel` y migrado a Compose resources library (`Res.drawable.too_bad`).
- **Paso 5**: Capa `di/` implementada
    - `MoviesDependencyInjector.kt` movido a `di/` y adaptado para inyectar los nuevos casos de uso y ViewModels.
- **Paso 6**: Clases duplicadas del paquete raíz eliminadas (`Movie.kt`, `MoviesViewModel.kt`, etc.).

### Pendiente
- **Paso 7**: Verificación final (compilación y pruebas).

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