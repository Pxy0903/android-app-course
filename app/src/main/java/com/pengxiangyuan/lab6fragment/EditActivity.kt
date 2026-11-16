package com.pengxiangyuan.lab6fragment

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pengxiangyuan.lab6fragment.databinding.ActivityEditBinding
import com.pengxiangyuan.lab6fragment.model.PersonInfo
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

/**
 * Controller 层：修改个人信息页。
 *
 * 实验六的变化：个人信息必须采用序列化的方法存储历史信息，
 * 因此保存时不再用 setResult 回传，而是：
 *  1. 进入时用 ObjectInputStream 从私有文件读回 [PersonInfo]，回填表单；
 *  2. 点击"保存"时读取控件状态，重新组装 [PersonInfo]，
 *     用 ObjectOutputStream 写回文件（openFileOutput），然后关闭页面。
 * 这样退出程序后再打开，个人信息 Fragment 仍能恢复上一次填写的内容。
 */
class EditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = getString(R.string.edit_info)

        fillFromSavedInfo()
        setupButtons()
    }

    /** 从序列化文件读回旧信息并回填表单 */
    private fun fillFromSavedInfo() {
        val info = loadInfo()
        binding.etName.setText(info.name)
        binding.etPhone.setText(info.phone)

        when (info.gender) {
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

            // 组装新信息并序列化写入文件
            val info = PersonInfo(
                name = binding.etName.text.toString().trim(),
                gender = gender,
                phone = binding.etPhone.text.toString().trim()
            )
            saveInfo(info)

            Toast.makeText(this, R.string.save_success, Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.btnCancel.setOnClickListener { finish() }
    }

    /** 序列化读取：openFileInput + ObjectInputStream；第一次运行没有存档时返回空信息 */
    private fun loadInfo(): PersonInfo {
        return try {
            val input = openFileInput(PersonalInfoFragment.FILE_PERSON_INFO)
            val objectInputStream = ObjectInputStream(input)
            val saved = objectInputStream.readObject() as? PersonInfo ?: PersonInfo()
            objectInputStream.close()
            input.close()
            saved
        } catch (e: Exception) {
            PersonInfo()
        }
    }

    /** 序列化保存：openFileOutput + ObjectOutputStream，把 [PersonInfo] 对象写进私有文件 */
    private fun saveInfo(info: PersonInfo) {
        try {
            val output = openFileOutput(PersonalInfoFragment.FILE_PERSON_INFO, MODE_PRIVATE)
            val objectOutputStream = ObjectOutputStream(output)
            objectOutputStream.writeObject(info)
            objectOutputStream.close()
            output.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
