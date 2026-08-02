package com.yiwenliu.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.yiwenliu.core.common.presentation.util.toUserMessage
import com.yiwenliu.core.model.Movie

/**
 * 電影海報網格，附帶 append（往下捲）的載入與錯誤狀態。
 *
 * refresh 的載入／錯誤／空結果**不在這裡**處理：那些狀態應該取代整個網格，而不是變成網格裡的
 * 一個項目，而且各畫面的處理方式不同（首頁有下拉重新整理、搜尋頁有空查詢與無結果）。
 * 呼叫端自己決定何時顯示這個網格。
 *
 * @param testTagPrefix 內部節點的測試標籤前綴，會產生 `"$testTagPrefix:grid"` 與
 *   `"$testTagPrefix:appendLoading"`。之所以用參數而不是讓呼叫端從 `modifier` 傳入，是因為
 *   append 的載入指示器是 `LazyVerticalGrid` 內部 `item {}` 產生的節點，呼叫端的 `modifier`
 *   到不了那裡。
 */
@Composable
fun MoviePagingGrid(
    movies: LazyPagingItems<Movie>,
    modifier: Modifier = Modifier,
    testTagPrefix: String = "movieGrid",
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 120.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("$testTagPrefix:grid"),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
    ) {
        items(
            count = movies.itemCount,
            key = movies.itemKey { it.id },
        ) { index ->
            movies[index]?.let { movie ->
                MovieItem(movie = movie, modifier = Modifier.fillMaxWidth())
            }
        }

        // 綁定 when 的 subject，避免在分支裡二次讀取 loadState 再硬轉型。
        when (val append = movies.loadState.append) {
            is LoadState.Loading -> item(span = { GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .testTag("$testTagPrefix:appendLoading"),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is LoadState.Error -> item(span = { GridItemSpan(maxLineSpan) }) {
                ErrorItem(
                    errorMessage = append.error.toUserMessage(LocalContext.current),
                    retryText = stringResource(R.string.retry),
                    onRetry = movies::retry,
                )
            }

            else -> Unit
        }
    }
}
