package com.thelightphone.components.calculator

import kotlin.test.Test
import kotlin.test.assertEquals

/** Covers digit entry, operator chaining, and the display formatting edge cases,
 *  fiddly enough that it's worth locking down with tests instead of trusting by
 *  inspection alone. */
class CalculatorLogicTest {

    private fun type(state: CalculatorState, digits: String): CalculatorState =
        digits.fold(state) { s, d -> CalculatorLogic.inputDigit(s, d.toString()) }

    // Digit entry

    @Test
    fun `typing digits builds up the display`() {
        val state = type(CalculatorState(), "123")
        assertEquals("123", state.display)
    }

    @Test
    fun `typing a digit after a fresh clear replaces the leading zero`() {
        val state = CalculatorLogic.inputDigit(CalculatorState(), "5")
        assertEquals("5", state.display)
    }

    @Test
    fun `display stops growing past the max length`() {
        val state = type(CalculatorState(), "12345678901234")
        assertEquals(10, state.display.length)
    }

    // Decimal point

    @Test
    fun `decimal on a fresh entry starts at 0`() {
        val state = CalculatorLogic.inputDecimal(CalculatorState())
        assertEquals("0.", state.display)
    }

    @Test
    fun `a second decimal point is ignored`() {
        val state = CalculatorLogic.inputDecimal(CalculatorLogic.inputDecimal(type(CalculatorState(), "1")))
        assertEquals("1.", state.display)
    }

    // Sign and backspace

    @Test
    fun `toggle sign prefixes and strips a minus`() {
        val positive = type(CalculatorState(), "4")
        val negative = CalculatorLogic.toggleSign(positive)
        assertEquals("-4", negative.display)
        assertEquals("4", CalculatorLogic.toggleSign(negative).display)
    }

    @Test
    fun `toggle sign on zero is a no-op`() {
        assertEquals("0", CalculatorLogic.toggleSign(CalculatorState()).display)
    }

    @Test
    fun `backspace to empty resets to 0`() {
        val state = CalculatorLogic.backspace(type(CalculatorState(), "5"))
        assertEquals("0", state.display)
        assertEquals(true, state.startingNewEntry)
    }

    @Test
    fun `backspace from an error clears fully`() {
        val error = CalculatorState(display = "Error")
        val state = CalculatorLogic.backspace(error)
        assertEquals(CalculatorState(), state)
    }

    // Operators and equals

    @Test
    fun `add then equals produces the sum`() {
        var state = type(CalculatorState(), "2")
        state = CalculatorLogic.setOperator(state, Operator.ADD)
        state = type(state, "3")
        state = CalculatorLogic.equals(state)
        assertEquals("5", state.display)
    }

    @Test
    fun `chaining operators applies the pending one first`() {
        var state = type(CalculatorState(), "2")
        state = CalculatorLogic.setOperator(state, Operator.ADD)
        state = type(state, "3")
        state = CalculatorLogic.setOperator(state, Operator.MULTIPLY)
        assertEquals("5", state.display) // 2 + 3 shown before the multiply's operand
        state = type(state, "4")
        state = CalculatorLogic.equals(state)
        assertEquals("20", state.display) // (2 + 3) * 4
    }

    @Test
    fun `divide by zero shows Error`() {
        var state = type(CalculatorState(), "1")
        state = CalculatorLogic.setOperator(state, Operator.DIVIDE)
        state = type(state, "0")
        state = CalculatorLogic.equals(state)
        assertEquals("Error", state.display)
    }

    @Test
    fun `equals with no pending operator is a no-op`() {
        val state = type(CalculatorState(), "7")
        assertEquals(state, CalculatorLogic.equals(state))
    }

    // Display formatting

    @Test
    fun `a fractional result trims trailing zeros`() {
        var state = type(CalculatorState(), "1")
        state = CalculatorLogic.setOperator(state, Operator.DIVIDE)
        state = type(state, "4")
        state = CalculatorLogic.equals(state)
        assertEquals("0.25", state.display)
    }

    @Test
    fun `a very large result switches to exponent notation`() {
        var state = type(CalculatorState(), "99999999")
        state = CalculatorLogic.setOperator(state, Operator.MULTIPLY)
        state = type(state, "99999999")
        state = CalculatorLogic.equals(state)
        assertEquals(true, state.display.contains("e"))
    }
}
