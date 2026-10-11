package com.pengxiangyuan.text2_mvc

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.pengxiangyuan.text2_mvc.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val expert = ProgramExpert() // 实例化ProgramExpert对象，放在Activity成员位置

    // 区1计数变量，记录TextView数量
    private var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // ===== ViewBinding初始化核心代码 =====
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //===================================

        // 改用binding.main，去掉findViewById
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ==========区1：button1 点击，动态添加TextView ==========
        binding.button1.setOnClickListener {
            count++
            val prefix = getString(R.string.dynamic_tv_prefix)
            val newText = "$prefix$count"
            val newTv = TextView(this)
            newTv.text = newText
            newTv.textSize = 24f
            // 添加到容器
            binding.llContainer.addView(newTv)

            // =====【修复】改成ScrollView滚动到底部 =====
            binding.scrollView.post {
                binding.scrollView.fullScroll(android.view.View.FOCUS_DOWN)
            }
        }


        //区二

        // 按钮点击事件：获取Spinner选中值，显示到TextView
        binding.button2.setOnClickListener {
            //拿到选择特征
            val selectedFeature = binding.spinner2.selectedItem.toString()
            //调用ProgramExpert方法，根据特征获取编程语言
            val resultLang = expert.getLanguage(selectedFeature)
            //将得到的编程语言名称显示到TextView
            binding.textView2.text = resultLang
        }
    }

}

