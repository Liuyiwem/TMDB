package com.yiwenliu.core.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * @param autoFocus 是否在進入組合時自動取得焦點並拉起輸入法。預設 `false`：共用元件不該光是
 *   存在就產生副作用，是否搶焦點應該由呼叫端決定。
 */
@Composable
fun SearchTextField(
    queryString: String,
    searchIconContentDescription: String?,
    closeIconContentDescription: String?,
    onQueryStringChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    autoFocus: Boolean = false,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    TextField(
        modifier = modifier.focusRequester(focusRequester),
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
        ),
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = searchIconContentDescription,
                tint = MaterialTheme.colorScheme.onSurface,
            )
        },
        trailingIcon = {
            if (queryString.isNotEmpty()) {
                IconButton(
                    onClick = {
                        onQueryStringChanged("")
                    },
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = closeIconContentDescription,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        },
        value = queryString,
        onValueChange = { newValue ->
            // 把換行收成空白，而不是在偵測到換行時丟棄整筆編輯。
            //
            // singleLine 幫不上忙：Material3 的 String 版 TextField 走的是舊版 BasicTextField，
            // singleLine 只被轉成 softWrap = false / maxLines = 1 / imeOptions.singleLine，
            // 整條路徑沒有任何換行淨化。舊版的貼上實作會把剪貼簿原文直接送進這個 lambda，
            // 所以貼上「Fight\nClub」時換行確實會到達這裡。
            //
            // 用空白而不是空字串取代，是為了保留搜尋查詢真正在意的詞邊界。
            // 也刻意不做 trim()：在 String 版 TextField 的 onValueChange 裡 trim 會讓尾端空白
            // 打不出來，多字查詢（"star wars"）在空白處就斷了。
            onQueryStringChanged(newValue.replace(LINE_BREAKS, " "))
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        // 搜尋是隨著輸入即時進行的，所以 IME 的搜尋鍵只需要收鍵盤。
        keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
        // 不需要 maxLines = 1：Material3 的預設已經是 if (singleLine) 1，而且 KDoc 明確說明
        // singleLine = true 時 maxLines 會被忽略。
        singleLine = true,
    )

    LaunchedEffect(autoFocus) {
        if (autoFocus) focusRequester.requestFocus()
    }
}

/** 檔案層級，避免每次重組都重新編譯 Regex。`[\r\n]+` 也涵蓋桌機同步剪貼簿的 CRLF。 */
private val LINE_BREAKS = Regex("[\\r\\n]+")

@Preview
@Composable
private fun SearchTextFieldPreview() {
    MaterialTheme {
        SearchTextField(
            queryString = "Search",
            searchIconContentDescription = null,
            closeIconContentDescription = null,
            onQueryStringChanged = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        )
    }
}
