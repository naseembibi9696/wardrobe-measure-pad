package com.wardrobemeasure.pad.presentation.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.wardrobemeasure.pad.R
import com.wardrobemeasure.pad.core.ui.SecondaryButton

@Composable
fun ConfirmDeleteDialog(name: String, detail: Int, onConfirm: () -> Unit, onCancel: () -> Unit) {
    AlertDialog(onDismissRequest = onCancel, title = { Text(stringResource(R.string.delete_named, name)) },
        text = { Text(stringResource(detail)) }, confirmButton = { SecondaryButton(R.string.delete, onConfirm) },
        dismissButton = { SecondaryButton(R.string.cancel, onCancel) })
}

@Composable
fun UnsavedChangesDialog(saveLabel: Int, onSave: () -> Unit, onDiscard: () -> Unit, onCancel: () -> Unit) {
    AlertDialog(onDismissRequest = onCancel, title = { Text(stringResource(R.string.unsaved_changes)) },
        text = { Text(stringResource(R.string.unsaved_hint)) }, confirmButton = {
            Column { SecondaryButton(saveLabel, onSave); SecondaryButton(R.string.discard_changes, onDiscard) }
        }, dismissButton = { SecondaryButton(R.string.cancel, onCancel) })
}
