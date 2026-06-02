package com.example.samplesizecalculator.ui.navigation

import com.example.samplesizecalculator.domain.models.CalculatorId

sealed class AppScreen {
    object Splash : AppScreen()
    object MainMenu : AppScreen()
    data class Calculator(val id: CalculatorId) : AppScreen()
}
