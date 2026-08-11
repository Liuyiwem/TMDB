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
fun TmdbConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.testTag(TmdbTestTags.CONFIRM_DIALOG),
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                modifier = Modifier.testTag(TmdbTestTags.CONFIRM_DIALOG_CONFIRM),
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag(TmdbTestTags.CONFIRM_DIALOG_DISMISS),
            ) {
                Text(dismissText)
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun TmdbConfirmDialogPreview() {
    MaterialTheme {
        TmdbConfirmDialog(
            title = "Remove favorite",
            message = "Remove \"Deadpool & Wolverine\" from your favorites?",
            confirmText = "Remove",
            dismissText = "Cancel",
            onConfirm = {},
            onDismiss = {},
        )
    }
}
