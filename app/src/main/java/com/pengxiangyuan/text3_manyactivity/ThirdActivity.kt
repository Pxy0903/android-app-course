package com.pengxiangyuan.text3_manyactivity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.pengxiangyuan.text3_manyactivity.databinding.ActivityThirdBinding

class ThirdActivity : AppCompatActivity() {
    // 声明binding对象
    private lateinit var binding: ActivityThirdBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityThirdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.confirmBut.setOnClickListener {
            val intent = Intent()
            intent.putExtra(SECOND_MSG,binding.editTextText2.text.toString())
            setResult(RESULT_OK,intent)
            finish()
        }

    }
}