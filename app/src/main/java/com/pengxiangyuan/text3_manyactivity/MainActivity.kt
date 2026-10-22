package com.pengxiangyuan.text3_manyactivity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import com.pengxiangyuan.text3_manyactivity.databinding.ActivityMainBinding
import androidx.activity.result.contract.ActivityResultContracts

const val FIRST_MSG="FIRST_MSG"
const val SECOND_MSG="SECOND_MSG"
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding// 声明binding对象
    private val thirdLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        // 这里就是拿到第三页面回传的数据，替代原来onActivityResult
        if(result.resultCode == RESULT_OK){
            binding.textView.text = result.data?.getStringExtra("SECOND_MSG")// 拿到backData，更新UI（binding.xxx.text = backData.toString()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)// 初始化ViewBinding，代替setContentView
        setContentView(binding.root)

        binding.second.setOnClickListener {
            val intent = Intent(this, SecondActivity::class.java)
            intent.putExtra(FIRST_MSG,binding.edInput.text.toString())
            startActivity(intent)
        }

        binding.third.setOnClickListener {
            val intent = Intent(this, ThirdActivity::class.java)
            thirdLauncher.launch(intent)
        }

        binding.four.setOnClickListener {
            val intent = Intent(this, FourActivity::class.java)
            startActivity(intent)
        }

        binding.five.setOnClickListener {
            val intent = Intent(this, FifthActivity::class.java)
            startActivity(intent)
        }

    }
}