package com.pengxiangyuan.lab4_cardgame

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.pengxiangyuan.lab4_cardgame.databinding.ItemLayoutBinding
import com.pengxiangyuan.lab4_cardgame.model.CardMatchingGame

/**
 * View 层：RecyclerView 的适配器。
 *
 * 职责（实验要求 4-b）：
 *  1. 从 [RecyclerView.Adapter] 派生，重载 [onCreateViewHolder] / [getItemCount] / [onBindViewHolder]；
 *  2. 只负责"把 Model 里第 position 张牌画到格子里"，不写任何游戏规则；
 *  3. 点击事件通过 [onCardClickListener] 回调交给 Controller（GameActivity），
 *     适配器自己不修改 Model，保证 MVC 的职责边界。
 *
 * 格子高度不写死：绑定时取 RecyclerView 当前可用高度除以 [rows]，
 * 再扣掉 item_layout 里 3dp 的上下外边距，竖屏 6 行、横屏 4 行都能刚好铺满一屏。
 */
class CardAdapter(private val game: CardMatchingGame) :
    RecyclerView.Adapter<CardAdapter.CardViewHolder>() {

    /** 点击回调：参数是被点击牌的下标，由 Activity 设置 */
    var onCardClickListener: ((Int) -> Unit)? = null

    /** 当前方向下的行数：竖屏 6 行、横屏 4 行，由 Activity 设置 */
    var rows: Int = PORTRAIT_ROWS

    /** 棋盘可用高度（px），由 Activity 在布局完成后写入，适配器据此均分每行高度 */
    var boardHeightPx: Int = 0

    /** 每张卡片单边的外边距（px），需与 item_layout.xml 的 3dp 一致 */
    var itemMarginPx: Int = 0

    class CardViewHolder(val binding: ItemLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val binding = ItemLayoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CardViewHolder(binding)
    }

    override fun getItemCount(): Int = game.cardCount

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val binding = holder.binding
        applyItemHeight(holder)

        val card = game.cardAtIndex(position)
        val context = binding.root.context

        when {
            // 已配对：灰色牌面，文字变浅，作为"废牌"
            card.isMatched -> {
                binding.ivCardBack.visibility = View.GONE
                binding.tvCardFace.visibility = View.VISIBLE
                binding.tvCardFace.setBackgroundResource(R.drawable.card_face_matched)
                binding.tvCardFace.text = card.toString()
                binding.tvCardFace.setTextColor(
                    ContextCompat.getColor(context, R.color.card_matched_text)
                )
                binding.root.alpha = 0.55f
            }

            // 已翻开：白底牌面，红花色用红字
            card.isChosen -> {
                binding.ivCardBack.visibility = View.GONE
                binding.tvCardFace.visibility = View.VISIBLE
                binding.tvCardFace.setBackgroundResource(R.drawable.card_face)
                binding.tvCardFace.text = card.toString()
                val colorRes = if (card.isRed) R.color.score_red else R.color.text_primary
                binding.tvCardFace.setTextColor(ContextCompat.getColor(context, colorRes))
                binding.root.alpha = 1f
            }

            // 未翻开：显示牌背
            else -> {
                binding.tvCardFace.text = ""
                binding.tvCardFace.visibility = View.GONE
                binding.ivCardBack.visibility = View.VISIBLE
                binding.root.alpha = 1f
            }
        }

        // 点击回调：交给 Activity 处理规则；已配对的牌不需要再响应
        binding.root.setOnClickListener {
            if (!card.isMatched && !card.isChosen) {
                onCardClickListener?.invoke(position)
            }
        }
        binding.root.isClickable = !card.isMatched && !card.isChosen
    }

    /**
     * 按棋盘可用高度把格子高度均分给 [rows] 行。
     * 这里不用 itemView.parent 取高度：onBindViewHolder 执行时格子还没有挂到 RecyclerView 上，
     * parent 为 null，所以高度由 Activity 在布局完成后通过 [boardHeightPx] 传进来。
     */
    private fun applyItemHeight(holder: CardViewHolder) {
        if (boardHeightPx <= 0 || rows <= 0) return
        val height = boardHeightPx / rows - itemMarginPx * 2
        if (height <= 0) return
        val params = holder.binding.root.layoutParams
        if (params.height != height) {
            params.height = height
            holder.binding.root.layoutParams = params
        }
    }

    /** 重置或旋转屏幕后让 RecyclerView 重新绑定全部格子 */
    fun refresh() {
        notifyDataSetChanged()
    }

    companion object {
        /** 竖屏行数（4 列 × 6 行 = 24 张） */
        const val PORTRAIT_ROWS = 6
    }
}
