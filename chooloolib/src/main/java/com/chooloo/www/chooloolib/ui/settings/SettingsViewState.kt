package com.chooloo.www.chooloolib.ui.settings

import androidx.lifecycle.MutableLiveData
import com.chooloo.www.chooloolib.R
import com.chooloo.www.chooloolib.interactor.color.ColorsInteractor
import com.chooloo.www.chooloolib.interactor.navigation.NavigationsInteractor
import com.chooloo.www.chooloolib.interactor.preferences.PreferencesInteractor
import com.chooloo.www.chooloolib.interactor.preferences.PreferencesInteractor.Companion.AccentTheme.*
import com.chooloo.www.chooloolib.interactor.string.StringsInteractor
import com.chooloo.www.chooloolib.interactor.theme.ThemesInteractor
import com.chooloo.www.chooloolib.interactor.theme.ThemesInteractor.ThemeMode
import com.chooloo.www.chooloolib.ui.base.menu.BaseMenuViewState
import com.chooloo.www.chooloolib.util.DataLiveEvent
import com.chooloo.www.chooloolib.util.LiveEvent
import com.chooloo.www.chooloolib.util.MutableDataLiveEvent
import com.chooloo.www.chooloolib.util.MutableLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
open class SettingsViewState @Inject constructor(
    protected val themes: ThemesInteractor,
    protected val colors: ColorsInteractor,
    protected val strings: StringsInteractor,
    protected val navigations: NavigationsInteractor,
    protected val preferences: PreferencesInteractor
) :
    BaseMenuViewState() {
    override val menuResList = listOf(R.menu.menu_chooloo)
    override val _title = MutableLiveData(strings.getString(R.string.settings))

    private val _askForThemeModeEvent = MutableLiveEvent()
    private val _askForAnimationsEvent = MutableLiveEvent()
    private val _askForColorEvent = MutableDataLiveEvent<Int>()

    val askForThemeModeEvent = _askForThemeModeEvent as LiveEvent
    val askForAnimationsEvent = _askForAnimationsEvent as LiveEvent
    val askForColorEvent = _askForColorEvent as DataLiveEvent<Int>


    override fun onMenuItemClick(itemId: Int) {
        when (itemId) {
            R.id.menu_chooloo_rate -> navigations.rateApp()
            R.id.menu_chooloo_email -> navigations.sendEmail()
            R.id.menu_chooloo_report_bugs -> navigations.reportBug()
            R.id.menu_chooloo_theme_mode -> _askForThemeModeEvent.call()
            R.id.menu_chooloo_animations -> _askForAnimationsEvent.call()
            R.id.menu_chooloo_accent_color -> _askForColorEvent.call(R.array.accent_colors)
            else -> super.onMenuItemClick(itemId)
        }
    }

    fun onColorResponse(color: Int) {
        preferences.accentTheme = when (color) {
            colors.getColor(R.color.red_primary) -> RED
            colors.getColor(R.color.blue_primary) -> BLUE
            colors.getColor(R.color.green_primary) -> GREEN
            colors.getColor(R.color.purple_primary) -> PURPLE
            else -> DEFAULT
        }
        navigations.goToLauncherActivity()
    }

    fun onAnimationsResponse(response: Boolean) {
        preferences.isAnimations = response
    }

    fun onThemeModeResponse(response: ThemeMode) {
        preferences.themeMode = response
        themes.applyThemeMode(response)
        navigations.goToLauncherActivity()
    }
}