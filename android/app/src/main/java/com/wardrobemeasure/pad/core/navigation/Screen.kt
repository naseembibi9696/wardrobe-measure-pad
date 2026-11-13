package com.wardrobemeasure.pad.core.navigation

import com.wardrobemeasure.pad.domain.model.*

sealed interface Screen {
    data object Splash : Screen
    data object Menu : Screen
    data class Game(val profileId: String, val mode: MeasureMode = MeasureMode.BODY, val garmentId: String? = null, val id: String = newId()) : Screen
    data class GameOver(val resultId: String) : Screen
    data class MeasureGuide(val sourceRouteId: String, val field: FieldKey, val mode: MeasureMode) : Screen
    data object SizeCharts : Screen
    data class ChartEditor(val chartId: String? = null, val basis: ChartBasis = ChartBasis.BODY, val fromResult: Boolean = false, val id: String = newId()) : Screen
    data class Compare(val profileId: String? = null, val garmentId: String? = null, val chartId: String? = null, val id: String = newId()) : Screen
    data object Settings : Screen
    data class ProfileEditor(val profileId: String? = null, val id: String = newId()) : Screen
}

sealed interface UiEvent {
    data class Navigate(val screen: Screen) : UiEvent
    data class Result(val id: String, val haptic: Boolean) : UiEvent
    data object Back : UiEvent
    data object Menu : UiEvent
    data object Finished : UiEvent
    data object Haptic : UiEvent
    data class UseMeasurement(val field: FieldKey) : UiEvent
    data class ChartSaved(val chartId: String, val fromResult: Boolean) : UiEvent
}
