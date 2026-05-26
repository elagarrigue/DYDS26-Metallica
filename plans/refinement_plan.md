# Plan de Refinamiento y Seguridad — Etapa 3

## Objetivo
Aplicar mejoras de seguridad, robustez y configurabilidad al sistema de integración de películas, eliminando "code smells" y endureciendo la lógica de combinación y red.

## Cambios Propuestos

### 1. Seguridad y Configuración — Credenciales y Red
- **Externalizar API Keys:** Mover `TMDB_API_KEY` y `OMDB_API_KEY` de `MoviesDependencyInjector.kt` a `gradle.properties`.
- **Hardening de Protocolo:** Cambiar de `HTTP` a `HTTPS` en todos los clientes de Ktor.
- **Configurabilidad de Timeouts:** Definir constantes claras para los timeouts de red y del broker.

### 2. Robustez en `MovieDetailBroker`
- **Validación de Fechas:** Mejorar `canCombine` para manejar fechas malformadas o cortas de forma segura (evitando errores en `take(4)`).
- **Normalización de URLs de Posters:** Asegurar que la concatenación de la base de TMDB con el path relativo no genere barras dobles (`//`) o falten barras.

### 3. Calidad de Código y Manejo de Errores
- **DetailViewModel:** Agregar logging básico para excepciones capturadas antes de mostrar el error en la UI.
- **Limpieza Genérica:** Eliminar comentarios redundantes o código residual detectado.

---

## Archivos a Modificar

### `gradle.properties`
Agregar:
```properties
tmdb_api_key=d18da1b5da16397619c688b0263cd281
omdb_api_key=f86603a1
```

### `composeApp/build.gradle.kts`
Pasar las keys como `buildConfigField` o inyectarlas mediante una tarea de generación de código si es necesario, o simplemente leerlas en el Injector si el entorno lo permite. Dado que es Desktop (JVM), usaremos una aproximación simple de lectura de propiedades en el `MoviesDependencyInjector`.

### `composeApp/src/desktopMain/kotlin/edu/dyds/movies/di/MoviesDependencyInjector.kt`
- Cambiar `protocol = URLProtocol.HTTP` a `URLProtocol.HTTPS`.
- Leer API keys desde propiedades del sistema o recursos (asumiendo que se inyectan en el build).
- Definir constantes para timeouts.

### `composeApp/src/desktopMain/kotlin/edu/dyds/movies/data/external/MovieDetailBroker.kt`
- Refactorizar `canCombine` para validar longitud de `releaseDate`.
- Refactorizar `normalizePoster` para usar `removePrefix("/")` y asegurar una sola barra.

### `composeApp/src/desktopMain/kotlin/edu/dyds/movies/presentation/detail/DetailViewModel.kt`
- Agregar `println` (como logging básico de consola para Desktop) en el bloque `catch`.

---

## Verificación
1. Ejecutar `./gradlew desktopTest` para asegurar que las nuevas validaciones no rompan la lógica de combinación.
2. Verificar manualmente (mediante inspección de logs si es posible) que se está usando HTTPS.
