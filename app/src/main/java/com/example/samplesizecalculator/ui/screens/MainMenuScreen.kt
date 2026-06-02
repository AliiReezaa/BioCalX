package com.example.samplesizecalculator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.samplesizecalculator.domain.models.CalculatorId
import com.example.samplesizecalculator.ui.components.CalculatorScaffold

// Simple models for the menu
private data class MenuItem(
    val label: String,
    val id: CalculatorId
)

private data class MenuSection(
    val title: String,
    val items: List<MenuItem>
)

@Composable
fun MainMenuScreen(
    onSelectCalculator: (CalculatorId) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val sections = remember {
        listOf(
            MenuSection(
                title = "Means",
                items = listOf(
                    MenuItem("Single Mean", CalculatorId.SINGLE_MEAN),
                    MenuItem("Single Mean (Finite Population)", CalculatorId.SINGLE_MEAN_FINITE),
                    MenuItem("Two Means (Equal size)", CalculatorId.COMPARE_TWO_MEANS_EQUAL),
                    MenuItem(">2 Means (Equal size)", CalculatorId.COMPARE_MORE_THAN_TWO_MEANS_EQUAL),
                    MenuItem("Two Means (Unequal size)", CalculatorId.COMPARE_TWO_MEANS_UNEQUAL),
                    MenuItem("Two Means (Effect size)", CalculatorId.COMPARE_TWO_MEANS_EFFECT),
                    MenuItem("Paired Means", CalculatorId.COMPARE_PAIRED_MEANS)
                )
            ),
            MenuSection(
                title = "Proportions",
                items = listOf(
                    MenuItem("Single Proportion", CalculatorId.SINGLE_PROPORTION),
                    MenuItem("Single Proportion (Finite Population)", CalculatorId.SINGLE_PROPORTION_FINITE),
                    MenuItem("Two Proportions (Equal size)", CalculatorId.COMPARE_TWO_PROPORTIONS_EQUAL),
                    MenuItem(">2 Proportions (Equal size)", CalculatorId.COMPARE_MORE_THAN_TWO_PROPORTIONS_EQUAL),
                    MenuItem("Two Proportions (Unequal size)", CalculatorId.COMPARE_TWO_PROPORTIONS_UNEQUAL),
                    MenuItem("Paired Proportions", CalculatorId.COMPARE_PAIRED_PROPORTIONS)
                )
            ),
            MenuSection(
                title = "Correlation & Diagnostic",
                items = listOf(
                    MenuItem("Correlation Coefficient", CalculatorId.CORRELATION_COEFFICIENT),
                    MenuItem("Estimate Sensitivity", CalculatorId.ESTIMATE_SENSITIVITY),
                    MenuItem("Estimate Specificity", CalculatorId.ESTIMATE_SPECIFICITY),
                    MenuItem("Area Under ROC", CalculatorId.AREA_UNDER_ROC)
                )
            ),
            MenuSection(
                title = "Study Designs",
                items = listOf(
                    MenuItem("Case–Control Study", CalculatorId.CASE_CONTROL),
                    MenuItem("Cohort Study", CalculatorId.COHORT),
                    MenuItem("Survival Study", CalculatorId.SURVIVAL)
                )
            )
        )
    }

    // Filter sections based on search query
    val filteredSections = sections.mapNotNull { section ->
        val filteredItems =
            if (searchQuery.isBlank()) section.items
            else section.items.filter { it.label.contains(searchQuery, ignoreCase = true) }

        if (filteredItems.isEmpty()) null else section.copy(items = filteredItems)
    }

    CalculatorScaffold(
        title = "Sample Size Calculator",
        onBack = { /* no back on main menu */ }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                    )
                )
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {

                // “Search” text field (without icon to keep dependencies simple)
                item {
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        label = { Text("Search calculators") },
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                }

                // Sections + cards
                filteredSections.forEach { section ->
                    item {
                        SectionHeader(title = section.title)
                    }

                    items(section.items) { menuItem ->
                        MenuItemCard(
                            label = menuItem.label,
                            onClick = { onSelectCalculator(menuItem.id) }
                        )
                    }
                }

                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
    )
}

@Composable
private fun MenuItemCard(
    label: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left accent bar
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(20.dp)
                    .background(MaterialTheme.colorScheme.primary)
            )

            Spacer(Modifier.width(12.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.weight(1f)
            )

            // Simple subtle arrow using text to avoid icon dependencies
            Text(
                text = "›",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            )
        }
    }
}
