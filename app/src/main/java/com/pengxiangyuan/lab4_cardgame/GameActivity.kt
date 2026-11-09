package com.pengxiangyuan.lab4_cardgame

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.pengxiangyuan.lab4_cardgame.databinding.ActivityGameBinding
import com.pengxiangyuan.lab4_cardgame.model.CardMatchingGame

/**
 * Controller 层：翻牌游戏页。
 *
 * 职责（实验要求 4-d 与 4-c）：
 *  1. 持有 Model（[CardMatchingGame]），并把 24 张牌交给 RecyclerView + [CardAdapter] 渲染（View）；
 *  2. 接收适配器回调的点击 → 调用模型 → 通知适配器刷新，实现 MVC 的单向数据流；
 *  3. 按屏幕方向切换列数：竖屏 4 列、横屏 6 列；卡片尺寸固定，放不下时只上下滚动，
 *     横屏时一行 6 张不会被截断，也不会出现左右滚动；
 *  4. 提供"重置游戏"（调用 Model 的 reset）与"退出"；
 *  5. 按"退出"即本局结束，用 setResult 把本局得分反向传回首页结算最高分。
 *
 * 界面全部通过 ViewBinding 访问（binding.rvCards / binding.tvScore ...），不再使用 findViewById。
 */
class GameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGameBinding

    /** Model 层：牌局规则与状态 */
    private lateinit var game: CardMatchingGame

    /** View 层：RecyclerView 适配器 */
    private lateinit var adapter: CardAdapter

    /** 防止全部配对后重复弹出 Toast */
    private var winToastShown = false

    /** 三张不匹配的牌停留一会儿后自动翻回 */
    private val flipBackRunnable = Runnable {
        if (game.isWaitingForFlipBack) {
            game.flipBackMismatched()
            adapter.refresh()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = getString(R.string.play_game)

        // 1. 初始化 Model：24 张牌，正好 8 组
        game = CardMatchingGame(CARD_COUNT)

        // 2. 初始化适配器，并注册点击回调（回调里只处理"点击第几张"，规则交给 Model）
        adapter = CardAdapter(game).apply {
            onCardClickListener = ::onCardClicked
        }
        binding.rvCards.adapter = adapter
        binding.rvCards.itemAnimator = null // 关闭动画，重置/翻回时不会闪
        updateHeader()

        // 3. 按屏幕方向决定每行几张牌（竖屏 4 张、横屏 6 张）；
        //    卡片尺寸固定，24 张放不下时 RecyclerView 自动上下滚动
        applyGridLayout()

        // 4. 重置游戏：清掉待执行的翻回任务，重新发牌
        binding.btnReset.setOnClickListener {
            binding.rvCards.removeCallbacks(flipBackRunnable)
            game.reset()
            winToastShown = false
            adapter.refresh()
            updateHeader()
        }

        // 5. 按下"退出"= 本局结束
        binding.btnExit.setOnClickListener { exitGame() }

        // 6. 系统返回键也当作"退出"处理，避免玩家用返回键离开导致本局成绩丢失
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = exitGame()
        })
    }

    /**
     * 按当前屏幕方向设置 GridLayoutManager 的列数：竖屏 4 列、横屏 6 列。
     * 列数决定了每张牌的宽度（屏幕宽度均分），高度在 item_layout 里固定为 100dp，
     * 所以卡片不会被拉伸；放不下时 RecyclerView 只做上下滚动。
     */
    private fun applyGridLayout() {
        val columns = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            LANDSCAPE_COLUMNS
        } else {
            PORTRAIT_COLUMNS
        }

        val rv = binding.rvCards
        val layoutManager = rv.layoutManager as? GridLayoutManager
        if (layoutManager == null || layoutManager.spanCount != columns) {
            rv.layoutManager = GridLayoutManager(this, columns)
        }
    }

    /** 点击第 index 张牌：交给 Model 处理，然后刷新界面 */
    private fun onCardClicked(index: Int) {
        if (game.isWaitingForFlipBack) return // 三张不匹配的牌还没翻回，忽略点击

        game.chooseCardAtIndex(index)
        adapter.refresh()
        updateHeader()

        if (game.isWaitingForFlipBack) {
            binding.rvCards.postDelayed(flipBackRunnable, FLIP_BACK_DELAY_MS)
        }
    }

    /** 刷新顶部的得分、已配对组数与胜利提示 */
    private fun updateHeader() {
        binding.tvScore.text = getString(R.string.score_format, game.score)
        binding.tvSets.text = getString(R.string.sets_format, game.matchedSets, game.setCount)

        if (game.isFinished && !winToastShown) {
            winToastShown = true
            Toast.makeText(this, R.string.win_message, Toast.LENGTH_LONG).show()
        }
    }

    /** 横竖屏切换：Activity 不重建，只重新排布格子，牌局进度不丢失 */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // 等新方向的布局完成后再重排，才能拿到正确的 RecyclerView 高度
        binding.rvCards.post { if (!isFinishing) applyGridLayout() }
    }

    /**
     * 结束本局：把本局得分塞进 Intent，用 setResult 反向传回首页，
     * 由 MainActivity.onActivityResult 结算最高分，然后关闭游戏页。
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
        binding.rvCards.removeCallbacks(flipBackRunnable)
    }

    companion object {
        /** 桌面上的牌数：24 张 = 8 组 × 3 张 */
        const val CARD_COUNT = CardMatchingGame.DEFAULT_CARD_COUNT

        /** 竖屏每行 4 张，共 6 行 */
        const val PORTRAIT_COLUMNS = 4

        /** 横屏每行 6 张，共 4 行 */
        const val LANDSCAPE_COLUMNS = 6

        /** 不匹配的牌停留时间（毫秒） */
        const val FLIP_BACK_DELAY_MS = 800L
    }
}
