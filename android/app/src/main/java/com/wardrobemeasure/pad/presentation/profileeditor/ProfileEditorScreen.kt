package com.wardrobemeasure.pad.presentation.profileeditor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.wardrobemeasure.pad.R
import com.wardrobemeasure.pad.core.ui.*
import com.wardrobemeasure.pad.presentation.dialog.*

@Composable
fun ProfileEditorScreen(state: ProfileEditorUiState, onBack: () -> Unit, onName: (String) -> Unit, onRelationship: (String) -> Unit,
    onAvatar: (Int) -> Unit, onSave: () -> Unit, onAskDelete: (Boolean) -> Unit, onDelete: () -> Unit, onDiscard: () -> Unit, onCancelBack: () -> Unit) {
    val focus = remember { FocusRequester() }
    LaunchedEffect(state.invalid) { if (state.invalid) focus.requestFocus() }
    NotebookPage(if (state.editing) R.string.edit_person_title else R.string.add_person_title, onBack, bottom = {
        Text(stringResource(R.string.profile_hint), style = MaterialTheme.typography.bodyMedium)
        PrimaryButton(if (state.error) R.string.retry_save else R.string.save_person, onSave, enabled = !state.busy && !state.loading)
    }) {
        if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
        NotebookCard {
            OutlinedTextField(state.name, onName, label = { Text(stringResource(R.string.person_name)) }, isError = state.invalid,
                enabled = !state.busy, modifier = Modifier.fillMaxWidth().focusRequester(focus), singleLine = true)
            OutlinedTextField(state.relationship, onRelationship, label = { Text(stringResource(R.string.relationship)) },
                enabled = !state.busy, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Text(stringResource(R.string.avatar_style), style = MaterialTheme.typography.titleMedium)
            listOf(R.string.avatar_blue, R.string.avatar_yellow, R.string.avatar_linen, R.string.avatar_ink).forEachIndexed { index, label ->
                val status = stringResource(if (state.avatar == index) R.string.selected else R.string.not_selected)
                SecondaryButton(label, { onAvatar(index) }, Modifier.fillMaxWidth().semantics { stateDescription = status }, !state.busy)
            }
        }
        if (state.invalid) StatusMessage(R.string.profile_validation)
        if (state.error) StatusMessage(R.string.storage_error)
        if (state.busy) { LinearProgressIndicator(Modifier.fillMaxWidth()); StatusMessage(R.string.saving) }
        if (state.editing) SecondaryButton(R.string.delete_person, { onAskDelete(true) }, Modifier.fillMaxWidth(), !state.busy)
    }
    if (state.confirmDelete) ConfirmDeleteDialog(state.name, R.string.delete_person_detail, onDelete, { onAskDelete(false) })
    if (state.confirmBack) UnsavedChangesDialog(R.string.save_person, onSave, onDiscard, onCancelBack)
}
