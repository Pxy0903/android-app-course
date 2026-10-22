package com.pengxiangyuan.text3_manyactivity

import android.os.Bundle
import android.os.Looper
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.pengxiangyuan.text3_manyactivity.databinding.ActivityFifthBinding

class FifthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFifthBinding
    // ✅ 变量移到类里面，变成成员变量，不再是全局
    private var seconds = 0
    private var running = false
    private var wasRunning = false

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFifthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d("life cycle","onCreate")

        if(savedInstanceState != null){
            seconds = savedInstanceState.getInt("seconds")
            running = savedInstanceState.getBoolean("running")
            wasRunning = savedInstanceState.getBoolean("wasRunning")
        }

        // ✅ 只初始化一次runnable，不要每次runTimer都新建
        runnable = object : Runnable {
            override fun run() {
                val hours = seconds / 3600
                val minutes = (seconds % 3600) / 60
                val secs = seconds % 60
                binding.timerTv.text = String.format("%02d:%02d:%02d", hours, minutes, secs)
                if (running) {
                    seconds++
                }
                handler.postDelayed(this, 1000)
            }
        }
        // 启动一次计时循环
        handler.post(runnable)

        binding.startBut.setOnClickListener {
            running = true
        }
        binding.stopBut.setOnClickListener {
            running = false
        }
        binding.restartBut.setOnClickListener {
            running = true
            seconds = 0
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.d("life cycle","onSaveInstanceState")
        super.onSaveInstanceState(outState)
        outState.putInt("seconds", seconds)
        outState.putBoolean("running", running)
        outState.putBoolean("wasRunning", wasRunning)
    }

    override fun onStart() {
        super.onStart()
        Log.d("life cycle","onStart")
    }

    override fun onStop() {
        super.onStop()
        Log.d("life cycle","onStop")
    }

    // 页面暂停，保存wasRunning，停止running
    override fun onPause() {
        super.onPause()
        Log.d("life cycle","onPause")
        wasRunning = running
        running = false
    }

    // 页面恢复，还原running状态
    override fun onResume() {
        super.onResume()
        Log.d("life cycle","onResume")
        if (wasRunning){
            running = true
        }
    }

    // Activity销毁的时候移除消息，防止继续回调、内存泄漏
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }
}
