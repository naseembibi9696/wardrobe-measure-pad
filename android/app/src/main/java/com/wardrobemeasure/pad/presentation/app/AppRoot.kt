package com.wardrobemeasure.pad.presentation.app

import kotlinx.coroutines.flow.collect

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wardrobemeasure.pad.core.di.ServiceLocator
import com.wardrobemeasure.pad.core.navigation.*
import com.wardrobemeasure.pad.presentation.splash.SplashRoute
import com.wardrobemeasure.pad.presentation.menu.*
import com.wardrobemeasure.pad.presentation.game.*
import com.wardrobemeasure.pad.presentation.gameover.*
import com.wardrobemeasure.pad.presentation.guide.*
import com.wardrobemeasure.pad.presentation.charts.*
import com.wardrobemeasure.pad.presentation.charteditor.*
import com.wardrobemeasure.pad.presentation.compare.*
import com.wardrobemeasure.pad.presentation.profileeditor.*
import com.wardrobemeasure.pad.presentation.settings.*
import kotlinx.coroutines.flow.Flow

@Composable
fun AppRoot() {
    val model: AppViewModel = viewModel(factory = ServiceLocator.factory)
    val screen by model.screen.collectAsStateWithLifecycle()
    val navigation by model.uiState.collectAsStateWithLifecycle()
    val haptics = LocalHapticFeedback.current
    val localView = LocalView.current
    SideEffect {
        (localView.context as? Activity)?.window?.let { window ->
            val controller = WindowCompat.getInsetsController(window, localView)
            controller.isAppearanceLightStatusBars = screen != Screen.Splash
            controller.isAppearanceLightNavigationBars = screen != Screen.Splash
        }
    }
    val onEvent: (UiEvent) -> Unit = { event ->
        when (event) {
            is UiEvent.Navigate -> model.navigate(event.screen)
            is UiEvent.Result -> {
                if (event.haptic) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                model.navigate(Screen.GameOver(event.id))
            }
            UiEvent.Back -> model.back()
            UiEvent.Menu -> model.menu()
            UiEvent.Finished -> model.finishSplash()
            UiEvent.Haptic -> haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            is UiEvent.ChartSaved -> model.chartSaved(event.chartId, event.fromResult)
            is UiEvent.UseMeasurement -> model.back()
        }
    }
    BackHandler(enabled = screen != Screen.Menu && screen != Screen.Splash) { model.back() }
    AnimatedContent(targetState = screen, transitionSpec = {
        fadeIn(tween(if (initialState == Screen.Splash) 320 else 180)) togetherWith fadeOut(tween(180))
    }, label = "navigation") { current ->
        when (current) {
            Screen.Splash -> SplashRoute(model::finishSplash)
            Screen.Menu -> MenuRoute(onEvent)
            is Screen.Game -> GameRoute(current, onEvent)
            is Screen.GameOver -> ResultRoute(current, onEvent)
            is Screen.MeasureGuide -> GuideRoute(current,
                navigation.backStack.filterIsInstance<Screen.Game>().lastOrNull { it.id == current.sourceRouteId }, onEvent)
            Screen.SizeCharts -> ChartsRoute(onEvent)
            is Screen.ChartEditor -> ChartEditorRoute(current, onEvent)
            is Screen.Compare -> CompareRoute(current, onEvent)
            Screen.Settings -> SettingsRoute(onEvent)
            is Screen.ProfileEditor -> ProfileEditorRoute(current, onEvent)
        }
    }
}

@Composable
private fun Events(events: Flow<UiEvent>, onEvent: (UiEvent) -> Unit) {
    val latest by rememberUpdatedState(onEvent)
    LaunchedEffect(Unit) { events.collect { latest(it) } }
}

@Composable
private fun MenuRoute(onEvent: (UiEvent) -> Unit) {
    val model: MenuViewModel = viewModel(factory = ServiceLocator.factory)
    val state by model.uiState.collectAsStateWithLifecycle()
    Events(model.events, onEvent)
    MenuScreen(state, model::play, model::select, model::navigate, model::retry)
}

@Composable
private fun GameRoute(route: Screen.Game, onEvent: (UiEvent) -> Unit) {
    val model: GameViewModel = viewModel(key = "game:${route.id}", factory = ServiceLocator.factory.forScreen(route))
    val state by model.uiState.collectAsStateWithLifecycle()
    DisposableEffect(Unit) { model.resume(); onDispose { model.pause() } }
    BackHandler { model.back() }
    Events(model.events, onEvent)
    GameScreen(state, GameActions(model::back, { model.bind(it) }, { model.bind(com.wardrobemeasure.pad.domain.model.MeasureMode.GARMENT, it) },
        model::step, model::openPicker, model::guide, model::clear, { model.save() }, model::compare, model::showUnits, model::setUnit,
        model::editGarment, model::askDeleteGarment, model::deleteGarment, model::pickerText, model::pickerStep, model::confirmPicker, model::closePicker,
        model::garmentName, model::garmentCategory, model::saveGarment, model::cancelGarment, model::savePending, model::discard, model::cancelPending, model::retryDraft))
}

@Composable
private fun ResultRoute(route: Screen.GameOver, onEvent: (UiEvent) -> Unit) {
    val model: GameOverViewModel = viewModel(key = "result:${route.resultId}", factory = ServiceLocator.factory.forScreen(route))
    val state by model.uiState.collectAsStateWithLifecycle()
    Events(model.events, onEvent)
    GameOverScreen(state, model::again, model::menu, model::compare, model::editChart)
}

@Composable
private fun GuideRoute(route: Screen.MeasureGuide, parent: Screen.Game?, onEvent: (UiEvent) -> Unit) {
    val model: MeasureGuideViewModel = viewModel(key = "guide:${route.sourceRouteId}:${route.field}", factory = ServiceLocator.factory.forScreen(route))
    val source: GameViewModel? = if (parent != null) viewModel(key = "game:${parent.id}", factory = ServiceLocator.factory.forScreen(parent)) else null
    val state by model.uiState.collectAsStateWithLifecycle()
    Events(model.events) { event ->
        if (event is UiEvent.UseMeasurement) { source?.openPicker(event.field); onEvent(UiEvent.Back) } else onEvent(event)
    }
    MeasureGuideScreen(state, model::back, model::step, model::use)
}

@Composable
private fun ChartsRoute(onEvent: (UiEvent) -> Unit) {
    val model: SizeChartsViewModel = viewModel(factory = ServiceLocator.factory)
    val state by model.uiState.collectAsStateWithLifecycle()
    BackHandler { model.back() }
    Events(model.events, onEvent)
    SizeChartsScreen(state, model::back, model::navigate, model::query, model::askDelete, model::delete, model::retry)
}

@Composable
private fun ChartEditorRoute(route: Screen.ChartEditor, onEvent: (UiEvent) -> Unit) {
    val model: ChartEditorViewModel = viewModel(key = "chart:${route.id}", factory = ServiceLocator.factory.forScreen(route))
    val state by model.uiState.collectAsStateWithLifecycle()
    DisposableEffect(Unit) { model.resume(); onDispose { model.pause() } }
    BackHandler { model.back() }
    Events(model.events, onEvent)
    ChartEditorScreen(state, ChartEditorActions(model::back, model::brand, model::title, model::basis, model::category, model::toggle,
        model::rowLabel, model::addRow, model::move, model::askRemove, { model.remove() }, model::cancelRemove, model::openRange,
        model::pickerText, model::pickerStep, model::confirmPicker, model::closePicker, model::save, model::discard, model::cancelBack))
}

@Composable
private fun CompareRoute(route: Screen.Compare, onEvent: (UiEvent) -> Unit) {
    val model: CompareViewModel = viewModel(key = "compare:${route.id}", factory = ServiceLocator.factory.forScreen(route))
    val state by model.uiState.collectAsStateWithLifecycle()
    DisposableEffect(route.chartId) { model.resume(); model.reselectChart(route.chartId); onDispose { model.pause() } }
    BackHandler { model.back() }
    Events(model.events, onEvent)
    CompareScreen(state, CompareActions(model::back, model::selectProfile, model::mode, model::garment, model::chart, model::navigate, model::updateMeasurements, model::compare))
}

@Composable
private fun ProfileEditorRoute(route: Screen.ProfileEditor, onEvent: (UiEvent) -> Unit) {
    val model: ProfileEditorViewModel = viewModel(key = "person:${route.id}", factory = ServiceLocator.factory.forScreen(route))
    val state by model.uiState.collectAsStateWithLifecycle()
    DisposableEffect(Unit) { model.resume(); onDispose { model.pause() } }
    BackHandler { model.back() }
    Events(model.events, onEvent)
    ProfileEditorScreen(state, model::back, model::name, model::relationship, model::avatar, model::save, model::confirmDelete, model::delete, model::discard, model::cancelBack)
}

@Composable
private fun SettingsRoute(onEvent: (UiEvent) -> Unit) {
    val model: SettingsViewModel = viewModel(factory = ServiceLocator.factory)
    val state by model.uiState.collectAsStateWithLifecycle()
    Events(model.events, onEvent)
    SettingsScreen(state, model::back, model::unit, model::haptics, model::menu, model::retry)
}
