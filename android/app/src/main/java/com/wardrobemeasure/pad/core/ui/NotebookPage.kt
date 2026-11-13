package com.wardrobemeasure.pad.core.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.wardrobemeasure.pad.R

@Composable
fun NotebookPage(title: Int, onBack: (() -> Unit)?, variant: Backdrop = Backdrop.EDITOR,
    bottom: (@Composable () -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    AppBackground(variant) {
        BoxWithConstraints(Modifier.safeDrawingPadding().imePadding().widthIn(max = 840.dp).fillMaxSize().align(Alignment.TopCenter)) {
            val compact = maxHeight < 520.dp || LocalDensity.current.fontScale > 1.5f
            if (compact) {
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    PageHeader(title, onBack)
                    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp), content = content)
                    if (bottom != null) ActionPanel(bottom)
                }
            } else Column(Modifier.fillMaxSize()) {
                PageHeader(title, onBack)
                Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp), content = content)
                if (bottom != null) ActionPanel(bottom)
            }
        }
    }
}

@Composable
private fun PageHeader(title: Int, onBack: (() -> Unit)?) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        if (onBack != null) SecondaryButton(R.string.back, onBack)
        else Image(painterResource(R.drawable.icon_home), null, Modifier.size(40.dp), contentScale = ContentScale.Fit)
        Text(stringResource(title), style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
    }
}
