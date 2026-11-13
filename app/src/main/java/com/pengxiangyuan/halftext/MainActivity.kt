package com.pengxiangyuan.halftext

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.pengxiangyuan.halftext.databinding.ActivityMainBinding
import kotlin.math.abs

/**
 * 第 1 个 Activity：输入两个数与运算符，调用 [Calculator] 计算并显示结果。
 *
 * 页面统一通过 [ActivityMainBinding]（ViewBinding）访问控件，不使用 findViewById。
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val calculator = Calculator()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.app_name)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        binding.btnCalculate.setOnClickListener { calculate() }

        // 有 id 的输入框和 RadioGroup 系统会保存状态；结果 TextView 默认不保存文本，
        // 因此显式恢复，保证横竖屏切换后计算结果不会丢失。
        savedInstanceState?.getString(KEY_RESULT)?.let { binding.tvResult.text = it }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_RESULT, binding.tvResult.text.toString())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_history -> {
            startActivity(Intent(this, HistoryActivity::class.java))
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    /** 读取输入、调用计算类、展示结果并写入历史记录。 */
    private fun calculate() {
        val left = readNumber(binding.etFirstNumber) ?: return
        val right = readNumber(binding.etSecondNumber) ?: return
        val operator = selectedOperator() ?: return

        val value = try {
            calculator.calculate(left, right, operator)
        } catch (e: IllegalArgumentException) {
            Toast.makeText(this, R.string.error_divide_by_zero, Toast.LENGTH_SHORT).show()
            return
        }

        val expression = getString(
            R.string.history_expression_format,
            formatNumber(left),
            operator.symbol,
            formatNumber(right)
        )
        val result = formatNumber(value)

        binding.tvResult.text = result
        HistoryRepository.add(
            this,
            HistoryRecord(
                expression = expression,
                result = result,
                timeMillis = System.currentTimeMillis()
            )
        )
    }

    /** 读取并校验一个输入值，非法时用 setError 提示，不让程序崩溃。 */
    private fun readNumber(field: EditText): Double? {
        val input = field.text.toString().trim()
        if (input.isEmpty()) {
            field.error = getString(R.string.error_empty_number)
            field.requestFocus()
            return null
        }
        val value = input.toDoubleOrNull()
        if (value == null || value.isNaN() || value.isInfinite()) {
            field.error = getString(R.string.error_invalid_number)
            field.requestFocus()
            return null
        }
        return value
    }

    private fun selectedOperator(): Operator? = when (binding.rgOperator.checkedRadioButtonId) {
        R.id.rbAdd -> Operator.ADD
        R.id.rbSubtract -> Operator.SUBTRACT
        R.id.rbMultiply -> Operator.MULTIPLY
        R.id.rbDivide -> Operator.DIVIDE
        else -> {
            Toast.makeText(this, R.string.error_no_operator, Toast.LENGTH_SHORT).show()
            null
        }
    }

    /** 整数结果不显示多余的 ".0"，其余保留原始小数。 */
    private fun formatNumber(value: Double): String =
        if (abs(value) < MAX_EXACT_INTEGER && value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            value.toString()
        }

    private companion object {
        const val KEY_RESULT = "key_result"
        const val MAX_EXACT_INTEGER = 1e15
    }
}
