package com.pengxiangyuan.text3_manyactivity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.pengxiangyuan.text3_manyactivity.databinding.ActivitySecondBinding

class SecondActivity : AppCompatActivity() {
    // 声明ViewBinding成员变量
    private lateinit var binding: ActivitySecondBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // 加载activity_second.xml，生成binding实例
        binding = ActivitySecondBinding.inflate(layoutInflater)
        // 将根布局设置给页面
        setContentView(binding.root)

        val intent= intent
        binding.textView.text = intent.getStringExtra("FIRST_MSG")

    }
}
