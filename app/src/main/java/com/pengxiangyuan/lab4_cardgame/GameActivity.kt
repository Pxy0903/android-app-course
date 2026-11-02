package com.pengxiangyuan.lab4_cardgame

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.widget.GridLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.pengxiangyuan.lab4_cardgame.databinding.ActivityGameBinding
import com.pengxiangyuan.lab4_cardgame.model.CardMatchingGame

/**
 * Controller 层：翻牌游戏页。
 *
 * 职责：
 *  1. 持有 Model（[CardMatchingGame]），并把 16 张牌动态生成为 GridLayout 里的按钮（View）；
 *  2. 接收点击 → 调用模型 → 按模型状态刷新界面，实现"MVC 的 Controller"；
 *  3. 提供"重置游戏"（重新发牌、清零得分）与"退出"；
 *  4. 按下"退出"即视为本局结束，用 setResult 把本局得分反向传回首页，
 *     由首页负责与当前最高分比较并决定是否刷新纪录（本页不直接读写最高分）。
 *
 * 界面全部通过 ViewBinding 访问（binding.gridLayout / binding.tvScore ...），代码里不再出现 findViewById。
 */
class GameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGameBinding

    /** Model 层：牌局规则与状态（对应老师给的 CardMatchingGame） */
    private lateinit var game: CardMatchingGame

    /** 16 个牌面按钮，index 与 Model 中牌的下标一一对应 */
    private val buttons = mutableListOf<AppCompatButton>()

    /** 牌面文字大小（px），在 createCards 里按卡片实际宽度换算 */
    private var cardTextSizePx = 0f

    /** 防止全部配对后重复弹出 Toast */
    private var winToastShown = false

    /** 两张不匹配的牌停留一会儿后自动翻回 */
    private val flipBackRunnable = Runnable {
        if (game.isWaitingForFlipBack) {
            game.flipBackMismatched()
            updateView()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = getString(R.string.play_game)

        // 1. 初始化模型：从 52 张标准牌里随机抽 16 张摆成 4×4
        game = CardMatchingGame(CARD_COUNT)

        // 2. 等 GridLayout 完成测量后再动态生成卡片，这样能拿到真实可用宽高，适配不同屏幕
        binding.gridLayout.post {
            if (buttons.isEmpty() && !isFinishing) {
                createCards()
                updateView()
            }
        }

        // 3. 重置游戏：清掉待执行的翻回任务，重新发牌
        binding.btnReset.setOnClickListener {
            binding.gridLayout.removeCallbacks(flipBackRunnable)
            game.restart()
            winToastShown = false
            updateView()
        }

        // 4. 按下"退出"= 本局结束：把本局得分反向传回首页，再关闭本页
        binding.btnExit.setOnClickListener { exitGame() }

        // 5. 系统返回键也当作"退出"处理，避免玩家用返回键离开导致本局成绩丢失
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = exitGame()
        })
    }

    /** 动态生成 16 张牌放进 GridLayout（4 列），尺寸按容器实际大小均分 */
    private fun createCards() {
        val grid = binding.gridLayout
        grid.removeAllViews()
        buttons.clear()

        val rows = (game.cardCount + COLUMN_COUNT - 1) / COLUMN_COUNT
        val gap = dpToPx(GAP_DP)
        val availWidth = grid.width - grid.paddingLeft - grid.paddingRight
        val availHeight = grid.height - grid.paddingTop - grid.paddingBottom
        val cardWidth = (availWidth - gap * COLUMN_COUNT) / COLUMN_COUNT
        val cardHeight = (availHeight - gap * rows) / rows

        // 牌面字号随卡片大小缩放，小屏也不会溢出
        cardTextSizePx = (cardWidth * 0.30f).coerceAtLeast(dpToPx(12).toFloat())

        for (i in 0 until game.cardCount) {
            val btn = AppCompatButton(this).apply {
                setPadding(0, 0, 0, 0)
                minimumWidth = 0
                minimumHeight = 0
                isAllCaps = false
                includeFontPadding = false
                contentDescription = getString(R.string.card_desc, i + 1)
            }

            // 用 GridLayout 的行列坐标摆放，让 4×4 严格对齐
            val params = GridLayout.LayoutParams(
                GridLayout.spec(i / COLUMN_COUNT),
                GridLayout.spec(i % COLUMN_COUNT)
            ).apply {
                width = cardWidth
                height = cardHeight
                setMargins(gap / 2, gap / 2, gap / 2, gap / 2)
            }
            btn.layoutParams = params

            // 初始状态：背面朝上，不显示文字
            btn.setBackgroundResource(R.drawable.card_back)
            btn.textSize = 0f
            btn.text = ""

            btn.setOnClickListener { onCardClicked(i) }

            grid.addView(btn)
            buttons.add(btn)
        }
    }

    /** 点击第 index 张牌：交给模型处理，然后刷新界面 */
    private fun onCardClicked(index: Int) {
        if (game.isWaitingForFlipBack) return // 两张不匹配的牌还没翻回，忽略点击
        game.chooseCardAtIndex(index)
        updateView()
        if (game.isWaitingForFlipBack) {
            binding.gridLayout.postDelayed(flipBackRunnable, FLIP_BACK_DELAY_MS)
        }
    }

    /** 根据模型状态刷新整个棋盘（Model → View 的单向同步） */
    private fun updateView() {
        binding.tvScore.text = getString(R.string.score_format, game.score)
        binding.tvPairs.text = getString(R.string.pairs_format, game.matchedPairs)

        for (i in 0 until buttons.size) {
            val btn = buttons[i]
            val card = game.cardAtIndex(i)

            btn.alpha = 1f // 先重置透明度，防止重置游戏后界面仍然发灰
            when {
                card.isMatched -> {
                    // 已配对：灰底半透明的"废牌"
                    btn.setBackgroundResource(R.drawable.card_face_matched)
                    btn.text = card.toString()
                    btn.setTextSize(TypedValue.COMPLEX_UNIT_PX, cardTextSizePx)
                    btn.setTextColor(ContextCompat.getColor(this, R.color.card_matched_text))
                    btn.alpha = 0.55f
                }

                card.isChosen -> {
                    // 已翻开：白底显示牌面，红色花色用红字
                    btn.setBackgroundResource(R.drawable.card_face)
                    btn.text = card.toString()
                    btn.setTextSize(TypedValue.COMPLEX_UNIT_PX, cardTextSizePx)
                    val colorRes = if (card.isRed) R.color.score_red else R.color.text_primary
                    btn.setTextColor(ContextCompat.getColor(this, colorRes))
                }

                else -> {
                    // 未翻开：牌背，隐藏文字
                    btn.setBackgroundResource(R.drawable.card_back)
                    btn.text = ""
                    btn.setTextSize(TypedValue.COMPLEX_UNIT_PX, 0f)
                    btn.setTextColor(Color.TRANSPARENT)
                }
            }
        }

        // 全部配对完成
        if (game.isFinished && !winToastShown) {
            winToastShown = true
            Toast.makeText(this, R.string.win_message, Toast.LENGTH_LONG).show()
        }
    }

    /**
     * 结束本局：把本局得分塞进 Intent，用 setResult 反向传回首页，
     * 由 MainActivity.onActivityResult 结算最高分，然后关闭游戏页。
     * （最高分不在本页比较，保证"分数的裁决权"只在首页一处，逻辑不重复）
     */
    private fun exitGame() {
        val data = Intent().apply {
            putExtra(MainActivity.KEY_SCORE, game.score)
        }
        setResult(RESULT_OK, data)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        // 移除未执行的延迟任务，避免 Activity 销毁后仍然回调
        binding.gridLayout.removeCallbacks(flipBackRunnable)
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density + 0.5f).toInt()

    companion object {
        /** 桌面上的牌数（从 52 张里随机抽 16 张） */
        const val CARD_COUNT = 16

        /** 每行 4 张 */
        const val COLUMN_COUNT = 4

        /** 卡片间距 */
        const val GAP_DP = 8

        /** 不匹配的牌停留时间（毫秒） */
        const val FLIP_BACK_DELAY_MS = 800L
    }
}
