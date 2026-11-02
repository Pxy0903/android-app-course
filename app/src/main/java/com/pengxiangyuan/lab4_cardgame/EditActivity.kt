package com.pengxiangyuan.lab4_cardgame

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pengxiangyuan.lab4_cardgame.databinding.ActivityEditBinding

/**
 * Controller 层：修改个人信息页。
 *
 * 职责：
 *  1. 把首页通过 Intent 传来的旧信息回填到表单（两个 EditText + 一个 RadioGroup）；
 *  2. 点击"保存"时读取控件状态，组装成 Intent 用 setResult 回传给 [MainActivity]；
 *  3. 不写文件，符合"修改后的信息无需保存到文件中"的要求。
 */
class EditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = getString(R.string.edit_info)

        fillFromIntent()
        setupButtons()
    }

    /** 用首页传来的当前信息回填表单 */
    private fun fillFromIntent() {
        binding.etName.setText(intent.getStringExtra(MainActivity.KEY_NAME).orEmpty())
        binding.etPhone.setText(intent.getStringExtra(MainActivity.KEY_PHONE).orEmpty())

        when (intent.getStringExtra(MainActivity.KEY_GENDER).orEmpty()) {
            getString(R.string.male) -> binding.rbMale.isChecked = true
            getString(R.string.female) -> binding.rbFemale.isChecked = true
        }
    }

    private fun setupButtons() {
        binding.btnSave.setOnClickListener {
            // 性别：单选组里取被选中的那个
            val gender = when {
                binding.rbMale.isChecked -> getString(R.string.male)
                binding.rbFemale.isChecked -> getString(R.string.female)
                else -> getString(R.string.not_selected)
            }

            // 用 setResult 把结果回传给首页
            val data = Intent().apply {
                putExtra(MainActivity.KEY_NAME, binding.etName.text.toString().trim())
                putExtra(MainActivity.KEY_PHONE, binding.etPhone.text.toString().trim())
                putExtra(MainActivity.KEY_GENDER, gender)
            }
            setResult(RESULT_OK, data)

            Toast.makeText(this, R.string.save_success, Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.btnCancel.setOnClickListener { finish() }
    }
}
