package com.yiwenliu.feature.favorite.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yiwenliu.core.model.FavoriteMovie
import com.yiwenliu.core.ui.LocalSnackbarHostState
import com.yiwenliu.core.ui.component.TmdbConfirmDialog
import com.yiwenliu.core.ui.component.TmdbMessageDialog
import com.yiwenliu.core.ui.preview.FavoriteMoviePreviewParameterProvider
import com.yiwenliu.core.ui.util.ObserveAsEvents

@Composable
internal fun FavoriteRoot(
    onMovieClick: (Int, String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FavoriteViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = LocalSnackbarHostState.current
    val context = LocalContext.current

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is FavoriteEvent.ShowError -> snackbarHostState.showSnackbar(event.message.asString(context))
        }
    }

    FavoriteScreen(
        state = state,
        modifier = modifier,
        onAction = viewModel::onAction,
        onMovieClick = onMovieClick,
    )
}

@Composable
internal fun FavoriteScreen(
    state: FavoriteUiState,
    onAction: (FavoriteAction) -> Unit,
    onMovieClick: (Int, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when {
            state.isLoading -> CircularProgressIndicator(
                modifier = Modifier.testTag(FavoriteTestTags.LOADING),
            )

            state.favorites.isEmpty() -> Text(
                text = stringResource(R.string.favorite_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .testTag(FavoriteTestTags.EMPTY),
            )

            else -> LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 120.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .testTag(FavoriteTestTags.GRID),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
            ) {
                items(items = state.favorites, key = FavoriteMovie::id) { movie ->
                    FavoriteMovieItem(
                        movie = movie,
                        onClick = { onMovieClick(movie.id, movie.title) },
                        onRemoveClick = { onAction(FavoriteAction.OnRemoveClick(movie)) },
                    )
                }
            }
        }
    }

    state.pendingRemoval?.let { movie ->
        TmdbConfirmDialog(
            title = stringResource(R.string.remove_favorite_title),
            message = stringResource(R.string.remove_favorite_message, movie.title),
            confirmText = stringResource(R.string.favorite_remove),
            dismissText = stringResource(R.string.favorite_cancel),
            onConfirm = { onAction(FavoriteAction.OnRemoveConfirm) },
            onDismiss = { onAction(FavoriteAction.OnRemoveDismiss) },
        )
    }

    state.error?.let { error ->
        TmdbMessageDialog(
            message = error.asString(),
            confirmText = stringResource(com.yiwenliu.core.ui.R.string.ok),
            onConfirm = { onAction(FavoriteAction.OnErrorDismiss) },
        )
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun FavoriteScreenPreview() {
    val favorites = FavoriteMoviePreviewParameterProvider().values.first()
    MaterialTheme {
        FavoriteScreen(
            state = FavoriteUiState(isLoading = false, favorites = favorites),
            onAction = {},
            onMovieClick = { _, _ -> },
        )
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun FavoriteScreenEmptyPreview() {
    MaterialTheme {
        FavoriteScreen(
            state = FavoriteUiState(isLoading = false),
            onAction = {},
            onMovieClick = { _, _ -> },
        )
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun FavoriteScreenErrorPreview() {
    val favorites = FavoriteMoviePreviewParameterProvider().values.first()
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            FavoriteScreen(
                state = FavoriteUiState(isLoading = false, favorites = favorites),
                onAction = {},
                onMovieClick = { _, _ -> },
            )
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
            ) {
                Text(stringResource(com.yiwenliu.core.ui.R.string.error_disk_full))
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun FavoriteScreenRemoveDialogPreview() {
    val favorites = FavoriteMoviePreviewParameterProvider().values.first()
    MaterialTheme {
        FavoriteScreen(
            state = FavoriteUiState(
                isLoading = false,
                favorites = favorites,
                pendingRemoval = favorites.first(),
            ),
            onAction = {},
            onMovieClick = { _, _ -> },
        )
    }
}
