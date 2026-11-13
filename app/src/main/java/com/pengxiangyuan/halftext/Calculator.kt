package com.pengxiangyuan.halftext

/** 四则运算符，symbol 用于界面展示与历史记录。 */
enum class Operator(val symbol: String) {
    ADD("+"),
    SUBTRACT("-"),
    MULTIPLY("*"),
    DIVIDE("/")
}

/**
 * 计算类：实现两个数的加、减、乘、除。
 *
 * 不依赖任何 Android API，保持纯 Kotlin，方便单独编写单元测试。
 */
class Calculator {

    fun add(left: Double, right: Double): Double = left + right

    fun subtract(left: Double, right: Double): Double = left - right

    fun multiply(left: Double, right: Double): Double = left * right

    /** 除数为 0 时抛出 [IllegalArgumentException]，由界面层给出提示。 */
    fun divide(left: Double, right: Double): Double {
        require(right != 0.0) { "除数不能为 0" }
        return left / right
    }

    /** 按运算符统一调度以上四个方法。 */
    fun calculate(left: Double, right: Double, operator: Operator): Double = when (operator) {
        Operator.ADD -> add(left, right)
        Operator.SUBTRACT -> subtract(left, right)
        Operator.MULTIPLY -> multiply(left, right)
        Operator.DIVIDE -> divide(left, right)
    }
}
