package com.wardrobemeasure.pad.presentation.splash

import kotlinx.coroutines.flow.collect

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wardrobemeasure.pad.R
import com.wardrobemeasure.pad.core.di.ServiceLocator
import com.wardrobemeasure.pad.core.navigation.UiEvent
import com.wardrobemeasure.pad.core.theme.*
import com.wardrobemeasure.pad.core.ui.*

@Composable
fun SplashRoute(onFinished: () -> Unit) {
    val model: SplashViewModel = viewModel(factory = ServiceLocator.factory)
    val state by model.uiState.collectAsStateWithLifecycle()
    val finished by model.finished.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { model.events.collect { if (it == UiEvent.Finished && model.finished.value) onFinished() } }
    SplashScreen(state.copy(finished = finished))
}

@Composable
fun SplashScreen(state: SplashUiState) {
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }
    val entry by animateFloatAsState(if (entered) 1f else 0f, tween(360), label = "entry")
    val transition = rememberInfiniteTransition(label = "fabric")
    val float by transition.animateFloat(-4f, 4f, infiniteRepeatable(tween(2400), RepeatMode.Reverse), label = "float")
    val rotation by transition.animateFloat(-3f, 3f, infiniteRepeatable(tween(3200), RepeatMode.Reverse), label = "stitch")
    val status = stringResource(R.string.opening)
    AppBackground(Backdrop.SPLASH) {
        BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding()) {
            val emblemSize = if (maxHeight < 520.dp) 112.dp else 184.dp
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text(stringResource(R.string.splash_eyebrow), style = MaterialTheme.typography.labelSmall, color = Background.copy(alpha = 0.78f), textAlign = TextAlign.Center)
                Spacer(Modifier.height(40.dp))
                Box(Modifier.size(emblemSize + 28.dp).graphicsLayer { translationY = float.dp.toPx(); alpha = entry; scaleX = 0.94f + entry * 0.06f; scaleY = scaleX }.rotate(rotation)
                    .border(1.dp, Secondary.copy(alpha = 0.3f), CircleShape), contentAlignment = Alignment.Center) {
                    Image(painterResource(R.drawable.logo), stringResource(R.string.logo_description), Modifier.size(emblemSize), contentScale = ContentScale.Fit)
                }
                Spacer(Modifier.height(24.dp))
                Text(stringResource(R.string.app_name), style = MaterialTheme.typography.displaySmall, color = Background, textAlign = TextAlign.Center,
                    modifier = Modifier.graphicsLayer { alpha = entry })
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.splash_subtitle), style = MaterialTheme.typography.bodyLarge, color = Background.copy(alpha = 0.86f), textAlign = TextAlign.Center)
                Spacer(Modifier.height(28.dp))
                LinearProgressIndicator(modifier = Modifier.width(144.dp).semantics { stateDescription = status; liveRegion = LiveRegionMode.Polite },
                    color = Accent, trackColor = Secondary.copy(alpha = 0.28f))
                Spacer(Modifier.height(16.dp))
                Text(stringResource(if (state.finished) R.string.splash_subtitle else R.string.opening), color = Background.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
            }
        }
    }
}
