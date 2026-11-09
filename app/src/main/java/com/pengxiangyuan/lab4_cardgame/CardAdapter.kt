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
 * 格子尺寸固定写在 item_layout（高 100dp、宽按列数均分），适配器不再按屏幕高度调整，
 * 一屏放不下时由 RecyclerView 上下滚动。
 */
class CardAdapter(private val game: CardMatchingGame) :
    RecyclerView.Adapter<CardAdapter.CardViewHolder>() {

    /** 点击回调：参数是被点击牌的下标，由 Activity 设置 */
    var onCardClickListener: ((Int) -> Unit)? = null

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

    /** 重置或旋转屏幕后让 RecyclerView 重新绑定全部格子 */
    fun refresh() {
        notifyDataSetChanged()
    }
}
