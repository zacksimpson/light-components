package com.thelightphone.components.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.SimpleLightScreen
import com.thelightphone.sdk.ui.LightIcon
import com.thelightphone.sdk.ui.LightIconConfiguration
import com.thelightphone.sdk.ui.LightIcons
import com.thelightphone.sdk.ui.LightTheme
import com.thelightphone.sdk.ui.LightThemeController
import com.thelightphone.sdk.ui.LightThemeTokens
import com.thelightphone.sdk.ui.designVerticalPxToSp
import com.thelightphone.sdk.ui.gridUnitsAsDp
import com.thelightphone.sdk.ui.lightClickable

// measured against the stock LightOS calculator screenshot: glyph height there is
// ~1.196x LightTextVariant.Heading, and the column pitch is narrower than an even
// screen-width/4 split, hence the custom style and gutter below instead of SDK presets.
private val ButtonInset = 3.6f
private val RightGutter = 2.3f
private const val GridFontScale = 1.196f

/**
 * Standard four-function calculator grid, see [CalculatorLogic] for the state machine.
 * CLOSE returns the current display value via goBack, so a caller can grab whatever
 * result the user landed on.
 *
 * Reads [LightThemeController] for light/dark, swap for a fixed `LightThemeColors` if
 * your tool doesn't use it.
 */
class CalculatorScreen(sealedActivity: SealedLightActivity) : SimpleLightScreen<String>(sealedActivity) {

    @Composable
    override fun Content() {
        val themeColors by LightThemeController.colors.collectAsState()
        LightTheme(colors = themeColors) {
            var state by remember { mutableStateOf(CalculatorState()) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LightThemeTokens.colors.background),
            ) {
                DisplayRow(
                    value = state.display,
                    onBackspace = { state = CalculatorLogic.backspace(state) },
                    modifier = Modifier.weight(1f),
                )
                CalculatorRow(
                    modifier = Modifier.weight(1f),
                    buttons = listOf(
                        CalculatorButton.Label("C") { state = CalculatorLogic.clear() },
                        null,
                        CalculatorButton.Label("±") { state = CalculatorLogic.toggleSign(state) },
                        CalculatorButton.Label("÷") { state = CalculatorLogic.setOperator(state, Operator.DIVIDE) },
                    ),
                )
                CalculatorRow(
                    modifier = Modifier.weight(1f),
                    buttons = listOf(
                        CalculatorButton.Label("7") { state = CalculatorLogic.inputDigit(state, "7") },
                        CalculatorButton.Label("8") { state = CalculatorLogic.inputDigit(state, "8") },
                        CalculatorButton.Label("9") { state = CalculatorLogic.inputDigit(state, "9") },
                        CalculatorButton.Label("×") { state = CalculatorLogic.setOperator(state, Operator.MULTIPLY) },
                    ),
                )
                CalculatorRow(
                    modifier = Modifier.weight(1f),
                    buttons = listOf(
                        CalculatorButton.Label("4") { state = CalculatorLogic.inputDigit(state, "4") },
                        CalculatorButton.Label("5") { state = CalculatorLogic.inputDigit(state, "5") },
                        CalculatorButton.Label("6") { state = CalculatorLogic.inputDigit(state, "6") },
                        CalculatorButton.Label("-") { state = CalculatorLogic.setOperator(state, Operator.SUBTRACT) },
                    ),
                )
                CalculatorRow(
                    modifier = Modifier.weight(1f),
                    buttons = listOf(
                        CalculatorButton.Label("1") { state = CalculatorLogic.inputDigit(state, "1") },
                        CalculatorButton.Label("2") { state = CalculatorLogic.inputDigit(state, "2") },
                        CalculatorButton.Label("3") { state = CalculatorLogic.inputDigit(state, "3") },
                        CalculatorButton.Label("+") { state = CalculatorLogic.setOperator(state, Operator.ADD) },
                    ),
                )
                CalculatorRow(
                    modifier = Modifier.weight(1f),
                    buttons = listOf(
                        CalculatorButton.Icon(LightIcons.CLOSE, onClick = { goBack(state.display) }),
                        CalculatorButton.Label("0") { state = CalculatorLogic.inputDigit(state, "0") },
                        CalculatorButton.Label(".") { state = CalculatorLogic.inputDecimal(state) },
                        CalculatorButton.Label("=") { state = CalculatorLogic.equals(state) },
                    ),
                )
            }
        }
    }
}

private sealed interface CalculatorButton {
    val onClick: () -> Unit

    data class Label(val text: String, override val onClick: () -> Unit) : CalculatorButton
    data class Icon(val icon: LightIconConfiguration, override val onClick: () -> Unit) : CalculatorButton
}

@Composable
private fun gridTextStyle(): TextStyle {
    val base = LightThemeTokens.typography.heading
    return base.copy(
        fontSize = (base.fontSize.value * GridFontScale).designVerticalPxToSp(),
        lineHeight = (base.lineHeight.value * GridFontScale).designVerticalPxToSp(),
    )
}

@Composable
private fun DisplayRow(value: String, onBackspace: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(end = RightGutter.gridUnitsAsDp()),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // weighted, so the number's own box can never grow into the icon's space,
        // the icon below keeps a fixed size and position no matter what's here.
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            Text(
                text = value,
                style = gridTextStyle(),
                color = LightThemeTokens.colors.content,
                maxLines = 1,
                overflow = TextOverflow.Clip,
            )
        }
        LightIcon(
            icon = LightIcons.BACK,
            size = 1.9f,
            modifier = Modifier
                .padding(start = 0.5f.gridUnitsAsDp())
                .lightClickable(onClick = onBackspace),
        )
    }
}

@Composable
private fun CalculatorRow(buttons: List<CalculatorButton?>, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth().padding(end = RightGutter.gridUnitsAsDp())) {
        buttons.forEach { button ->
            Box(
                modifier = Modifier.weight(1f).fillMaxSize(),
                contentAlignment = Alignment.CenterStart,
            ) {
                when (button) {
                    null -> Unit
                    is CalculatorButton.Label -> Text(
                        text = button.text,
                        style = gridTextStyle(),
                        color = LightThemeTokens.colors.content,
                        modifier = Modifier
                            .padding(start = ButtonInset.gridUnitsAsDp())
                            .lightClickable(onClick = button.onClick),
                    )
                    is CalculatorButton.Icon -> LightIcon(
                        icon = button.icon,
                        size = 1.7f,
                        modifier = Modifier
                            .padding(start = ButtonInset.gridUnitsAsDp())
                            .lightClickable(onClick = button.onClick),
                    )
                }
            }
        }
    }
}
