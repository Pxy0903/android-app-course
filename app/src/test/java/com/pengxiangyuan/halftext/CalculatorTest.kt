package com.pengxiangyuan.halftext

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

/** 计算类不依赖 Android API，可直接在 JVM 上做单元测试。 */
class CalculatorTest {

    private val calculator = Calculator()
    private val delta = 0.000001

    @Test
    fun add_isCorrect() {
        assertEquals(7.5, calculator.add(3.0, 4.5), delta)
    }

    @Test
    fun subtract_isCorrect() {
        assertEquals(-1.5, calculator.subtract(3.0, 4.5), delta)
    }

    @Test
    fun multiply_isCorrect() {
        assertEquals(13.5, calculator.multiply(3.0, 4.5), delta)
    }

    @Test
    fun divide_isCorrect() {
        assertEquals(2.0, calculator.divide(9.0, 4.5), delta)
    }

    @Test
    fun divideByZero_throws() {
        assertThrows(IllegalArgumentException::class.java) {
            calculator.divide(9.0, 0.0)
        }
    }

    @Test
    fun calculate_dispatchesAllOperators() {
        assertEquals(7.0, calculator.calculate(3.0, 4.0, Operator.ADD), delta)
        assertEquals(-1.0, calculator.calculate(3.0, 4.0, Operator.SUBTRACT), delta)
        assertEquals(12.0, calculator.calculate(3.0, 4.0, Operator.MULTIPLY), delta)
        assertEquals(0.75, calculator.calculate(3.0, 4.0, Operator.DIVIDE), delta)
    }
}
