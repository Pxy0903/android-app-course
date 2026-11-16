package com.pengxiangyuan.lab6fragment.model

import java.io.Serializable

/**
 * Model 层：一张扑克牌。
 *
 * 实验六要求翻牌游戏的状态用"对象序列化 + 文件存储"保存，
 * 因此 [Card] 实现 [Serializable]，随 [CardMatchingGame] 一起写入私有文件，
 * 退出程序后再打开即可恢复牌局。
 *
 * 配牌规则兼容两种玩法：
 *  - 两张牌玩法：传 1 张参照牌，点数 15 分 / 花色 4 分（实验四规则）；
 *  - 三张牌玩法：传 2 张参照牌，三张点数全部相同才配对成功，得 40 分（实验五规则）。
 */
class Card(
    val suit: String,
    val rank: String,
    var isChosen: Boolean = false,
    var isMatched: Boolean = false
) : Serializable {

    companion object {
        /** 序列化版本号：类结构变化时保持旧文件可读 */
        private const val serialVersionUID = 1L

        val rankStrings = arrayOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")
        val validSuits = arrayOf("♥", "♦", "♠", "♣")

        /** 两张牌点数相同时的得分（两张牌玩法） */
        const val MATCH_BONUS = 15

        /** 两张牌花色相同时的得分（两张牌玩法） */
        const val SUIT_BONUS = 4

        /** 三张牌点数全部相同时的得分（三张牌玩法） */
        const val THREE_MATCH_BONUS = 40
    }

    override fun toString(): String {
        return suit + rank
    }

    /** 红桃、方块显示成红色（只影响显示，不影响配对规则） */
    val isRed: Boolean
        get() = suit == "♥" || suit == "♦"

    /**
     * 判断本牌与参照牌是否匹配，返回本次得分，0 表示不匹配。
     *
     * @param otherCards 参照牌：长度 1 表示两张牌比较；长度 2 表示三张牌比较。
     */
    fun match(otherCards: Array<Card>): Int {
        return when (otherCards.size) {
            // 两张牌：点数相同给 15 分，否则花色相同给 4 分
            1 -> {
                val otherCard = otherCards[0]
                when {
                    otherCard.rank == rank -> MATCH_BONUS
                    otherCard.suit == suit -> SUIT_BONUS
                    else -> 0
                }
            }

            // 三张牌：必须三张点数全部相同才配对成功，给 40 分
            2 -> {
                val allSameRank = otherCards[0].rank == rank && otherCards[1].rank == rank
                if (allSameRank) THREE_MATCH_BONUS else 0
            }

            else -> 0
        }
    }
}
