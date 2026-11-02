package com.pengxiangyuan.lab4_cardgame

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pengxiangyuan.lab4_cardgame.databinding.ActivityMainBinding

/**
 * Controller 层：首页。
 *
 * 职责：
 *  1. 用 ViewBinding 绑定 activity_main.xml，展示玩家信息（姓名、性别、电话）与当前最高分；
 *  2. 点击"修改个人信息"用 startActivityForResult 打开 [EditActivity]，把当前信息带过去回填，
 *     再通过 onActivityResult 接收回传的新信息；
 *  3. 点击"翻牌游戏"同样用 startActivityForResult 打开 [GameActivity]；
 *     玩家在游戏页按"退出"结束一局时，游戏页用 setResult 把本局得分反向传回来，
 *     这里比较"本局得分"与"当前最高分"：本局更高就刷新最高分并写入 SharedPreferences；
 *  4. 最高分只在"一局结束（按下退出）"时结算，中途返回首页不会产生新纪录。
 */
class MainActivity : AppCompatActivity() {

    /** ViewBinding：编译期生成的布局绑定类，替代 findViewById */
    private lateinit var binding: ActivityMainBinding
    private lateinit var sp: SharedPreferences

    /** 玩家信息只保存在内存中（实验要求：无需写入文件） */
    private var playerName: String = ""
    private var playerGender: String = ""
    private var playerPhone: String = ""

    /** 当前最高分，从 SharedPreferences 读出后缓存在内存里 */
    private var highScore: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = getString(R.string.info_title)

        sp = getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
        highScore = sp.getInt(KEY_HIGH_SCORE, 0)
        initViews()

        // 打开修改信息页，把当前信息通过 Intent 传过去，便于回填
        binding.btnEdit.setOnClickListener {
            val intent = Intent(this, EditActivity::class.java).apply {
                putExtra(KEY_NAME, playerName)
                putExtra(KEY_GENDER, playerGender)
                putExtra(KEY_PHONE, playerPhone)
            }
            startActivityForResult(intent, REQUEST_EDIT)
        }

        // 打开翻牌游戏页：用 startActivityForResult，等它把本局得分传回来
        binding.btnPlay.setOnClickListener {
            startActivityForResult(Intent(this, GameActivity::class.java), REQUEST_GAME)
        }
    }

    /** 把信息区的 TextView 刷新成当前玩家信息与最高分 */
    private fun initViews() {
        val notFilled = getString(R.string.not_filled)
        binding.tvName.text = getString(R.string.name_format, playerName.ifBlank { notFilled })
        binding.tvGender.text = getString(R.string.gender_format, playerGender.ifBlank { notFilled })
        binding.tvPhone.text = getString(R.string.phone_format, playerPhone.ifBlank { notFilled })
        refreshHighScore()
    }

    /** 接收二级页面 setResult 回来的数据（反向传参），并同步界面 */
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK || data == null) return

        when (requestCode) {
            // 修改信息页：回传姓名、性别、电话、爱好
            REQUEST_EDIT -> {
                playerName = data.getStringExtra(KEY_NAME).orEmpty().trim()
                playerGender = data.getStringExtra(KEY_GENDER).orEmpty()
                playerPhone = data.getStringExtra(KEY_PHONE).orEmpty().trim()
                initViews()
            }

            // 游戏页：回传本局得分，在一局结束时结算最高分
            REQUEST_GAME -> {
                val score = data.getIntExtra(KEY_SCORE, 0)
                settleHighScore(score)
            }
        }
    }

    /**
     * 一局结束后的结算：本局得分高于当前最高分就更新，
     * 同时写入 SharedPreferences，保证退出应用后纪录仍然保留。
     */
    private fun settleHighScore(score: Int) {
        if (score > highScore) {
            highScore = score
            sp.edit().putInt(KEY_HIGH_SCORE, highScore).apply()
            refreshHighScore()
            Toast.makeText(this, getString(R.string.new_record_format, highScore), Toast.LENGTH_LONG)
                .show()
        } else {
            Toast.makeText(
                this,
                getString(R.string.game_over_format, score, highScore),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun refreshHighScore() {
        binding.tvScore.text = getString(R.string.high_score_format, highScore)
    }

    companion object {
        const val SP_NAME = "GameData"
        const val KEY_HIGH_SCORE = "high_score"
        const val KEY_NAME = "name"
        const val KEY_GENDER = "gender"
        const val KEY_PHONE = "phone"

        /** 游戏页回传本局得分用的 key */
        const val KEY_SCORE = "score"

        const val REQUEST_EDIT = 1
        const val REQUEST_GAME = 2
    }
}
