package com.yiwenliu.core.ui.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import com.yiwenliu.core.ui.TmdbTestTags

@Composable
fun TmdbMessageDialog(message: String, confirmText: String, onConfirm: () -> Unit, modifier: Modifier = Modifier) {
    AlertDialog(
        onDismissRequest = onConfirm,
        modifier = modifier.testTag(TmdbTestTags.MESSAGE_DIALOG),
        text = { Text(message) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                modifier = Modifier.testTag(TmdbTestTags.MESSAGE_DIALOG_CONFIRM),
            ) {
                Text(confirmText)
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun TmdbMessageDialogPreview() {
    MaterialTheme {
        TmdbMessageDialog(
            message = "No internet connection",
            confirmText = "OK",
            onConfirm = {},
        )
    }
}
