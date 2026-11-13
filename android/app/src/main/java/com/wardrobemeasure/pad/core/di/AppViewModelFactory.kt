package com.wardrobemeasure.pad.core.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.wardrobemeasure.pad.core.navigation.Screen
import com.wardrobemeasure.pad.domain.repository.NotebookRepository
import com.wardrobemeasure.pad.presentation.app.AppViewModel
import com.wardrobemeasure.pad.presentation.splash.SplashViewModel
import com.wardrobemeasure.pad.presentation.menu.MenuViewModel
import com.wardrobemeasure.pad.presentation.game.GameViewModel
import com.wardrobemeasure.pad.presentation.gameover.GameOverViewModel
import com.wardrobemeasure.pad.presentation.guide.MeasureGuideViewModel
import com.wardrobemeasure.pad.presentation.charts.SizeChartsViewModel
import com.wardrobemeasure.pad.presentation.charteditor.ChartEditorViewModel
import com.wardrobemeasure.pad.presentation.compare.CompareViewModel
import com.wardrobemeasure.pad.presentation.profileeditor.ProfileEditorViewModel
import com.wardrobemeasure.pad.presentation.settings.SettingsViewModel

class AppViewModelFactory(private val notebook: NotebookRepository, private val screen: Screen? = null) : ViewModelProvider.Factory {
    fun forScreen(screen: Screen): AppViewModelFactory = AppViewModelFactory(notebook, screen)
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when (modelClass) {
        AppViewModel::class.java -> AppViewModel(notebook)
        SplashViewModel::class.java -> SplashViewModel()
        MenuViewModel::class.java -> MenuViewModel(notebook)
        GameViewModel::class.java -> GameViewModel(notebook, requireNotNull(screen as? Screen.Game))
        GameOverViewModel::class.java -> GameOverViewModel(notebook, requireNotNull(screen as? Screen.GameOver))
        MeasureGuideViewModel::class.java -> MeasureGuideViewModel(requireNotNull(screen as? Screen.MeasureGuide))
        SizeChartsViewModel::class.java -> SizeChartsViewModel(notebook)
        ChartEditorViewModel::class.java -> ChartEditorViewModel(notebook, requireNotNull(screen as? Screen.ChartEditor))
        CompareViewModel::class.java -> CompareViewModel(notebook, requireNotNull(screen as? Screen.Compare))
        ProfileEditorViewModel::class.java -> ProfileEditorViewModel(notebook, requireNotNull(screen as? Screen.ProfileEditor))
        SettingsViewModel::class.java -> SettingsViewModel(notebook)
        else -> error(modelClass.name)
    } as T
}
