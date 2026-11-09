package com.pengxiangyuan.lab4_cardgame.model

/**
 * Model 层：一张扑克牌。
 *
 * 实验五在实验四的基础上把游戏改成"三张牌一组"：
 *  - [match] 一次可能收到 1 张或 2 张参照牌；
 *  - 传 1 张（两张牌比较）时沿用实验四的规则：点数 15 分 / 花色 4 分；
 *  - 传 2 张（三张牌比较）时采用本次设计的规则：三张点数全部相同才算配对成功，得 40 分。
 *
 * 花色、点数改成对外只读（val），方便 Model 层按点数发牌、测试按点数分组。
 */
class Card(
    val suit: String,
    val rank: String,
    var isChosen: Boolean = false,
    var isMatched: Boolean = false
) {
    companion object {
        val rankStrings = arrayOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")
        val validSuits = arrayOf("♥", "♦", "♠", "♣")

        /** 两张牌点数相同时的得分（实验四规则，保留） */
        const val MATCH_BONUS = 15

        /** 两张牌花色相同时的得分（实验四规则，保留） */
        const val SUIT_BONUS = 4

        /** 三张牌点数全部相同时的得分（实验五规则） */
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
