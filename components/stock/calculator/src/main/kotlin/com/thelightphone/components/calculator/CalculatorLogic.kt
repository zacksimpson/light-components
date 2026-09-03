package com.thelightphone.components.calculator

import kotlin.math.abs

// max characters (digits, sign, decimal point) the display will show before
// switching to exponent notation, so a number can never overflow or wrap.
private const val MAX_DISPLAY_LENGTH = 10

enum class Operator {
    ADD,
    SUBTRACT,
    MULTIPLY,
    DIVIDE;

    fun apply(a: Double, b: Double): Double = when (this) {
        ADD -> a + b
        SUBTRACT -> a - b
        MULTIPLY -> a * b
        DIVIDE -> a / b
    }
}

data class CalculatorState(
    val display: String = "0",
    val accumulator: Double? = null,
    val pendingOperator: Operator? = null,
    val startingNewEntry: Boolean = true,
)

/** Pure state transitions for a standard four-function calculator, one call per
 *  button press. [CalculatorState] holds everything, so undo/redo or a history
 *  list is just keeping old states around, no hidden state to reconcile. */
object CalculatorLogic {

    fun inputDigit(state: CalculatorState, digit: String): CalculatorState {
        val current = state.display
        val next = when {
            state.startingNewEntry || current == "0" -> digit
            else -> current + digit
        }
        if (next.length > MAX_DISPLAY_LENGTH) return state
        return state.copy(display = next, startingNewEntry = false)
    }

    fun inputDecimal(state: CalculatorState): CalculatorState {
        if (state.startingNewEntry) {
            return state.copy(display = "0.", startingNewEntry = false)
        }
        if (state.display.length >= MAX_DISPLAY_LENGTH) return state
        if (state.display.contains(".")) return state
        return state.copy(display = state.display + ".")
    }

    fun toggleSign(state: CalculatorState): CalculatorState {
        val current = state.display
        if (current == "0") return state
        val next = if (current.startsWith("-")) current.removePrefix("-") else "-$current"
        if (next.length > MAX_DISPLAY_LENGTH) return state
        return state.copy(display = next)
    }

    fun backspace(state: CalculatorState): CalculatorState {
        if (state.display == "Error") return clear()
        val trimmed = state.display.dropLast(1)
        val next = if (trimmed.isEmpty() || trimmed == "-") "0" else trimmed
        return state.copy(display = next, startingNewEntry = next == "0")
    }

    fun clear(): CalculatorState = CalculatorState()

    fun setOperator(state: CalculatorState, operator: Operator): CalculatorState {
        val current = state.display.toDoubleOrNull() ?: 0.0
        val accumulator = if (state.pendingOperator != null && !state.startingNewEntry) {
            state.pendingOperator.apply(state.accumulator ?: 0.0, current)
        } else {
            state.accumulator ?: current
        }
        return state.copy(
            display = formatValue(accumulator),
            accumulator = accumulator,
            pendingOperator = operator,
            startingNewEntry = true,
        )
    }

    fun equals(state: CalculatorState): CalculatorState {
        val operator = state.pendingOperator ?: return state
        val current = state.display.toDoubleOrNull() ?: 0.0
        val result = operator.apply(state.accumulator ?: 0.0, current)
        return CalculatorState(display = formatValue(result))
    }

    private fun formatValue(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "Error"
        if (value == 0.0) return "0"

        val magnitude = abs(value)
        val plain = when {
            magnitude < 1e-6 -> null
            value == value.toLong().toDouble() && magnitude < 1e15 -> value.toLong().toString()
            else -> "%.8f".format(value).trimEnd('0').trimEnd('.')
        }

        return if (plain != null && plain.length <= MAX_DISPLAY_LENGTH) {
            plain
        } else {
            "%.2e".format(value)
        }
    }
}
