package com.example.samplesizecalculator

import androidx.compose.runtime.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import com.example.samplesizecalculator.domain.models.CalculatorId
import com.example.samplesizecalculator.ui.navigation.AppScreen
import com.example.samplesizecalculator.ui.screens.*
import com.example.samplesizecalculator.ui.theme.SampleSizeCalculatorTheme

@Composable
fun SampleSizeApp() {
    SampleSizeCalculatorTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            // Start on Splash instead of MainMenu
            var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Splash) }

            when (val screen = currentScreen) {

                // ───── Splash screen ─────
                AppScreen.Splash -> {
                    SplashScreen(
                        onTimeout = {
                            currentScreen = AppScreen.MainMenu
                        }
                    )
                }

                // ───── Main menu ─────
                AppScreen.MainMenu -> {
                    MainMenuScreen(
                        onSelectCalculator = { id ->
                            currentScreen = AppScreen.Calculator(id)
                        }
                    )
                }

                // ───── All calculator screens ─────
                is AppScreen.Calculator -> {
                    val onBack = { currentScreen = AppScreen.MainMenu }
                    when (screen.id) {
                        CalculatorId.SINGLE_MEAN ->
                            SingleMeanScreen(onBack)

                        CalculatorId.SINGLE_MEAN_FINITE ->
                            SingleMeanFiniteScreen(onBack)

                        CalculatorId.COMPARE_TWO_MEANS_EQUAL ->
                            CompareTwoMeansEqualScreen(onBack)

                        CalculatorId.COMPARE_MORE_THAN_TWO_MEANS_EQUAL ->
                            CompareMoreThanTwoMeansEqualScreen(onBack)

                        CalculatorId.COMPARE_TWO_MEANS_UNEQUAL ->
                            CompareTwoMeansUnequalScreen(onBack)

                        CalculatorId.COMPARE_TWO_MEANS_EFFECT ->
                            CompareTwoMeansEffectSizeScreen(onBack)

                        CalculatorId.COMPARE_PAIRED_MEANS ->
                            ComparePairedMeansScreen(onBack)

                        CalculatorId.SINGLE_PROPORTION ->
                            SingleProportionScreen(onBack)

                        CalculatorId.SINGLE_PROPORTION_FINITE ->
                            SingleProportionFiniteScreen(onBack)

                        CalculatorId.COMPARE_TWO_PROPORTIONS_EQUAL ->
                            CompareTwoProportionsEqualScreen(onBack)

                        CalculatorId.COMPARE_MORE_THAN_TWO_PROPORTIONS_EQUAL ->
                            CompareMoreThanTwoProportionsEqualScreen(onBack)

                        CalculatorId.COMPARE_TWO_PROPORTIONS_UNEQUAL ->
                            CompareTwoProportionsUnequalScreen(onBack)

                        CalculatorId.COMPARE_PAIRED_PROPORTIONS ->
                            ComparePairedProportionsScreen(onBack)

                        CalculatorId.CORRELATION_COEFFICIENT ->
                            CorrelationCoefficientScreen(onBack)

                        CalculatorId.ESTIMATE_SENSITIVITY ->
                            EstimateSensitivityScreen(onBack)

                        CalculatorId.ESTIMATE_SPECIFICITY ->
                            EstimateSpecificityScreen(onBack)

                        CalculatorId.AREA_UNDER_ROC ->
                            AreaUnderRocScreen(onBack)

                        CalculatorId.CASE_CONTROL ->
                            CaseControlStudyScreen(onBack)

                        CalculatorId.COHORT ->
                            CohortStudyScreen(onBack)

                        CalculatorId.SURVIVAL ->
                            SurvivalStudyScreen(onBack)
                    }
                }
            }
        }
    }
}
