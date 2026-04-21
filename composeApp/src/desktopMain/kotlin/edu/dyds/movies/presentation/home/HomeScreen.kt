@file:Suppress("FunctionName")

package edu.dyds.movies.presentation.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.foundation.Image
import coil3.compose.AsyncImage
import dydsproject.composeapp.generated.resources.Res
import dydsproject.composeapp.generated.resources.app_name
import dydsproject.composeapp.generated.resources.error
import dydsproject.composeapp.generated.resources.too_bad
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import edu.dyds.movies.domain.model.Movie
import edu.dyds.movies.domain.model.QualifiedMovie
import edu.dyds.movies.presentation.utils.LoadingIndicator
import edu.dyds.movies.presentation.utils.NoResults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onMovieClick: (Movie) -> Unit
) {

    val state by viewModel.uiState.collectAsState()

    MaterialTheme {
        Surface {
            val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
            Scaffold(
                topBar = {
                    TopAppBar(
                        { Text(stringResource(Res.string.app_name)) },
                        scrollBehavior = scrollBehavior
                    )
                },
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
            ) { padding ->

                when (state) {
                    is HomeUiState.Loading -> LoadingIndicator(true)
                    is HomeUiState.Success -> MovieGrid(padding, (state as HomeUiState.Success).movies, onMovieClick)
                    is HomeUiState.Error -> NoResults { viewModel.onEvent(HomeUiEvent.LoadMovies) }
                }
            }
        }
    }
}

@Composable
private fun MovieGrid(
    padding: PaddingValues,
    movies: List<QualifiedMovie>,
    onMovieClick: (Movie) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(120.dp),
        contentPadding = PaddingValues(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(padding)
    ) {
        items(movies, key = { it.movie.id }) { qualifiedMovie ->
            if (qualifiedMovie.qualityLabel in listOf("Top rated", "Good")) {
                GoodMovieItem(qualifiedMovie.movie) { onMovieClick(qualifiedMovie.movie) }
            } else {
                BadMovieItem(qualifiedMovie.movie)
            }
        }
    }
}

@Composable
private fun GoodMovieItem(movie: Movie, onClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable { onClick() }
    ) {
        AsyncImage(
            model = movie.poster,
            contentDescription = movie.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2 / 3f)
                .clip(MaterialTheme.shapes.small)
        )
        Text(
            text = movie.title,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable
private fun BadMovieItem(movie: Movie) {
    var dialogState by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.alpha(0.7f).clickable { dialogState = true }
    ) {
        AsyncImage(
            model = movie.poster,
            contentDescription = movie.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2 / 3f)
                .clip(MaterialTheme.shapes.small)
        )
        Text(
            text = movie.title,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            modifier = Modifier.padding(8.dp)
        )
    }

    DialogWindow(
        title = stringResource(Res.string.error),
        resizable = false,
        onCloseRequest = { dialogState = false },
        visible = dialogState
    ) {
        Image(
            painter = painterResource(Res.drawable.too_bad),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        )
    }
}
