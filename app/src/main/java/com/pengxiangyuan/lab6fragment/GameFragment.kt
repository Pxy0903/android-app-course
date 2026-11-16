package com.pengxiangyuan.lab6fragment

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.pengxiangyuan.lab6fragment.databinding.FragmentGameBinding
import com.pengxiangyuan.lab6fragment.model.CardMatchingGame
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

/**
 * Controller 层：翻牌游戏 Fragment（由 [MainActivity] 的底部导航切换显示）。
 *
 * 同一个 Fragment 通过 newInstance 参数 [cardsPerSet] 复用两种玩法：
 *  - 传 [CardMatchingGame.TWO_CARD_GROUP] → 两张牌的翻牌游戏；
 *  - 传 [CardMatchingGame.THREE_CARD_GROUP] → 三张牌的翻牌游戏。
 *
 * 序列化存储（实验六要求）：
 *  - onViewCreated 先用 ObjectInputStream 从私有文件读回上次的牌局，读不到才发新牌；
 *  - onStop 用 ObjectOutputStream 把当前牌局写回文件，
 *    因此切换底部标签、退出程序后再进入都能恢复进度。
 *  文件名带上玩法区分：两张牌存 "game2"，三张牌存 "game3"。
 */
class GameFragment : Fragment() {

    private var _binding: FragmentGameBinding? = null
    private val binding get() = _binding!!

    /** 当前显示的是哪种玩法（每组几张牌），由 newInstance 塞进 arguments */
    private val cardsPerSet: Int by lazy {
        arguments?.getInt(ARG_CARDS_PER_SET, CardMatchingGame.THREE_CARD_GROUP)
            ?: CardMatchingGame.THREE_CARD_GROUP
    }

    /** Model 层：牌局规则与状态 */
    private lateinit var game: CardMatchingGame

    /** View 层：RecyclerView 适配器 */
    private lateinit var adapter: CardAdapter

    /** 防止全部配对后重复弹出 Toast */
    private var winToastShown = false

    /** 一组牌对不上时，停留一会儿后自动翻回 */
    private val flipBackRunnable = Runnable {
        if (::game.isInitialized && game.isWaitingForFlipBack) {
            game.flipBackMismatched()
            adapter.refresh()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 恢复上次的牌局；没有历史记录时才发新牌
        game = loadData() ?: CardMatchingGame(CARD_COUNT, cardsPerSet)

        // 2. 初始化适配器，并注册点击回调（回调里只处理"点击第几张"，规则交给 Model）
        adapter = CardAdapter(game).apply {
            onCardClickListener = ::onCardClicked
        }
        binding.rvCards.adapter = adapter
        binding.rvCards.itemAnimator = null // 关闭动画，重置/翻回时不会闪
        applyGridLayout()
        updateHeader()

        // 恢复出来时如果正停在"对不上等待翻回"的状态，补上延时翻回任务
        if (game.isWaitingForFlipBack) {
            binding.rvCards.postDelayed(flipBackRunnable, FLIP_BACK_DELAY_MS)
        }

        // 3. 重置游戏：清掉待执行的翻回任务，重新发牌
        binding.btnReset.setOnClickListener {
            binding.rvCards.removeCallbacks(flipBackRunnable)
            game.reset()
            winToastShown = false
            adapter.refresh()
            updateHeader()
        }
    }

    /** 切到别的标签或离开界面时，把牌局写进文件 */
    override fun onStop() {
        super.onStop()
        if (::game.isInitialized) saveData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvCards.removeCallbacks(flipBackRunnable)
        _binding = null
    }

    /** 按当前屏幕方向设置 GridLayoutManager 的列数：竖屏 4 列、横屏 6 列 */
    private fun applyGridLayout() {
        val columns = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            LANDSCAPE_COLUMNS
        } else {
            PORTRAIT_COLUMNS
        }
        binding.rvCards.layoutManager = GridLayoutManager(requireActivity(), columns)
    }

    /** 点击第 index 张牌：交给 Model 处理，然后刷新界面 */
    private fun onCardClicked(index: Int) {
        if (game.isWaitingForFlipBack) return // 一组不匹配的牌还没翻回，忽略点击

        game.chooseCardAtIndex(index)
        adapter.refresh()
        updateHeader()

        if (game.isWaitingForFlipBack) {
            binding.rvCards.postDelayed(flipBackRunnable, FLIP_BACK_DELAY_MS)
        }
    }

    /** 刷新得分、已配对组数与规则提示，全部配对成功时弹提示 */
    private fun updateHeader() {
        binding.tvScore.text = getString(R.string.score_format, game.score)
        binding.tvSets.text = getString(R.string.sets_format, game.matchedSets, game.setCount)
        binding.tvRule.text =
            if (cardsPerSet == CardMatchingGame.TWO_CARD_GROUP) {
                getString(R.string.rule_hint_2card)
            } else {
                getString(R.string.rule_hint_3card)
            }

        if (game.isFinished && !winToastShown) {
            winToastShown = true
            Toast.makeText(requireContext(), R.string.win_message, Toast.LENGTH_LONG).show()
        }
    }

    /**
     * 序列化保存：openFileOutput 打开私有文件，ObjectOutputStream 把整个牌局写进去。
     * 牌局对象（[CardMatchingGame] 及其中的每张 [com.pengxiangyuan.lab6fragment.model.Card]）
     * 都实现了 Serializable，退出程序后再打开可以完整恢复。
     */
    private fun saveData() {
        try {
            val output = requireActivity().openFileOutput(saveFileName, Context.MODE_PRIVATE)
            val objectOutputStream = ObjectOutputStream(output)
            objectOutputStream.writeObject(game)
            objectOutputStream.close()
            output.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /** 序列化恢复：openFileInput 读文件，ObjectInputStream 还原牌局；没有历史文件时返回 null */
    private fun loadData(): CardMatchingGame? {
        return try {
            val input = requireActivity().openFileInput(saveFileName)
            val objectInputStream = ObjectInputStream(input)
            @Suppress("UNCHECKED_CAST")
            val saved = objectInputStream.readObject() as? CardMatchingGame
            objectInputStream.close()
            input.close()
            saved
        } catch (e: Exception) {
            null // 第一次运行没有存档，属于正常情况
        }
    }

    /** 存档文件名：两张牌 "game2"、三张牌 "game3" */
    private val saveFileName: String
        get() = if (cardsPerSet == CardMatchingGame.TWO_CARD_GROUP) FILE_GAME_2 else FILE_GAME_3

    companion object {
        /** newInstance 传参用的 key */
        const val ARG_CARDS_PER_SET = "cards_per_set"

        /** 桌面上的牌数：24 张 */
        const val CARD_COUNT = CardMatchingGame.DEFAULT_CARD_COUNT

        /** 竖屏每行 4 张 */
        const val PORTRAIT_COLUMNS = 4

        /** 横屏每行 6 张 */
        const val LANDSCAPE_COLUMNS = 6

        /** 不匹配的牌停留时间（毫秒） */
        const val FLIP_BACK_DELAY_MS = 800L

        /** 两张牌玩法的存档文件名 */
        const val FILE_GAME_2 = "game2"

        /** 三张牌玩法的存档文件名 */
        const val FILE_GAME_3 = "game3"

        /** 工厂方法：用 arguments 记住玩法，Fragment 重建（旋转屏幕等）后依然保留 */
        @JvmStatic
        fun newInstance(cardsPerSet: Int): GameFragment =
            GameFragment().apply {
                arguments = Bundle().apply { putInt(ARG_CARDS_PER_SET, cardsPerSet) }
            }
    }
}
