package com.pengxiangyuan.lab4_cardgame.model

/**
 * Model 层：一张扑克牌。
 *
 * 写法完全沿用老师给的 model 代码：花色、点数、是否已翻开（isChosen）、是否已配对（isMatched）
 * 都放在构造参数里，外部可以直接读写这几个状态，Model 之间传参最省事。
 * 与老师原版相比只改了两处：
 *  1. match() 直接返回本次得分（点数 15 分、花色 4 分），不再返回 4/1 再乘 MATCH_BONUS；
 *  2. 增加 isRed，供界面把红桃、方块显示成红色（只是显示用，不影响配对规则）。
 */
class Card(
    private val suit: String,
    private val rank: String,
    var isChosen: Boolean = false,
    var isMatched: Boolean = false
) {
    companion object {
        val rankStrings = arrayOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")
        val validSuits = arrayOf("♥", "♦", "♠", "♣")
        const val MATCH_BONUS = 15
        const val SUIT_BONUS = 4
    }

    override fun toString(): String {
        return suit + rank
    }

    val isRed: Boolean
        get() = suit == "♥" || suit == "♦"

    fun match(otherCards: Array<Card>): Int {
        var score = 0
        if (otherCards.size == 1) {
            val otherCard = otherCards[0]
            score = when {
                otherCard.rank == rank -> MATCH_BONUS
                otherCard.suit == suit -> SUIT_BONUS
                else -> 0
            }
        }
        return score
    }
}
