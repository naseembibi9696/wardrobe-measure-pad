package com.wardrobemeasure.pad.presentation.menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.wardrobemeasure.pad.R
import com.wardrobemeasure.pad.core.navigation.Screen
import com.wardrobemeasure.pad.core.theme.*
import com.wardrobemeasure.pad.core.ui.*
import com.wardrobemeasure.pad.presentation.common.dateText

@Composable
fun MenuScreen(state: MenuUiState, onPlay: (String?) -> Unit, onSelect: (String) -> Unit, onNavigate: (Screen) -> Unit, onRetry: () -> Unit) {
    val selected = state.profiles.find { it.id == state.selectedId }
    NotebookPage(R.string.people, null, Backdrop.PEOPLE, bottom = {
        if (!state.loading && !state.error && selected != null) {
            Text(stringResource(if (state.drafts.any { it.sourceId == selected.id }) R.string.draft_hint else R.string.selected_hint, selected.name), style = MaterialTheme.typography.bodyMedium)
            PrimaryButton(R.string.play, { onPlay(selected.id) })
        } else if (!state.loading && !state.error) PrimaryButton(R.string.create_person, { onNavigate(Screen.ProfileEditor()) })
    }) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { SecondaryButton(R.string.settings_action, { onNavigate(Screen.Settings) }) }
        NotebookCard {
            Text(stringResource(R.string.notebook_eyebrow), style = MaterialTheme.typography.labelSmall)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.hero_title), style = MaterialTheme.typography.headlineMedium)
                    Text(stringResource(R.string.hero_hint), style = MaterialTheme.typography.bodyMedium)
                }
                Image(painterResource(R.drawable.logo), null, Modifier.size(80.dp), contentScale = ContentScale.Fit)
            }
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                listOf(R.drawable.sprite_1, R.drawable.sprite_2, R.drawable.sprite_3, R.drawable.sprite_4, R.drawable.sprite_5).forEach { drawable ->
                    Image(painterResource(drawable), null, Modifier.size(36.dp), contentScale = ContentScale.Fit)
                }
            }
        }
        when {
            state.loading -> { LinearProgressIndicator(Modifier.fillMaxWidth()); StatusMessage(R.string.loading) }
            state.error -> { StatusMessage(R.string.load_error); PrimaryButton(R.string.retry, onRetry) }
            else -> {
                if (state.recovered) StatusMessage(R.string.recovered)
                if (state.selectionError) StatusMessage(R.string.storage_error)
                if (state.profiles.isNotEmpty() && state.charts > 0) Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(R.string.people_count, state.profiles.size, Modifier.weight(1f))
                    StatCard(R.string.charts_count, state.charts, Modifier.weight(1f))
                }
                if (state.profiles.isEmpty()) NotebookCard {
                    Text(stringResource(R.string.empty_people), style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(R.string.empty_people_detail))
                }
                state.profiles.forEach { profile ->
                    val chosen = profile.id == state.selectedId
                    val selection = stringResource(if (chosen) R.string.selected_person else R.string.not_selected)
                    NotebookCard(selected = chosen, modifier = Modifier.semantics { stateDescription = selection }) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Surface(shape = CircleShape, color = if (profile.avatar % 2 == 0) Primary else Accent, modifier = Modifier.size(48.dp)) {
                                Box(contentAlignment = Alignment.Center) { Text(stringResource(R.string.data_text, profile.name.take(1).uppercase()), color = if (profile.avatar % 2 == 0) Background else Ink, style = MaterialTheme.typography.titleLarge) }
                            }
                            Column(Modifier.weight(1f)) {
                                Text(stringResource(R.string.data_text, profile.name), style = MaterialTheme.typography.titleLarge)
                                if (profile.relationship.isNotBlank()) Text(stringResource(R.string.data_text, profile.relationship), style = MaterialTheme.typography.bodyMedium)
                            }
                            val description = stringResource(R.string.profile_selection, profile.name)
                            RadioButton(chosen, { onSelect(profile.id) }, Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp).semantics { contentDescription = description })
                        }
                        if (profile.isSample) Text(stringResource(R.string.sample_badge), style = MaterialTheme.typography.labelSmall)
                        if (chosen) Text(stringResource(R.string.selected), style = MaterialTheme.typography.labelSmall)
                        val record = state.records.find { it.sourceId == profile.id }
                        if (record != null && record.values.isNotEmpty()) {
                            Text(stringResource(R.string.saved_fields, record.values.size), style = MaterialTheme.typography.bodyMedium)
                            Text(stringResource(R.string.updated_date, dateText(record.updatedAt)), style = MaterialTheme.typography.labelSmall)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val openDescription = stringResource(R.string.profile_action, stringResource(R.string.open), profile.name)
                            val editDescription = stringResource(R.string.profile_action, stringResource(R.string.edit), profile.name)
                            SecondaryButton(R.string.open, { onPlay(profile.id) }, Modifier.weight(1f).semantics { contentDescription = openDescription })
                            SecondaryButton(R.string.edit, { onNavigate(Screen.ProfileEditor(profile.id)) }, Modifier.weight(1f).semantics { contentDescription = editDescription })
                        }
                    }
                }
                SecondaryButton(R.string.add_person, { onNavigate(Screen.ProfileEditor()) }, Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SecondaryButton(R.string.size_charts_action, { onNavigate(Screen.SizeCharts) }, Modifier.weight(1f))
                    SecondaryButton(R.string.compare_action, { onNavigate(Screen.Compare()) }, Modifier.weight(1f))
                }
            }
        }
    }
}
