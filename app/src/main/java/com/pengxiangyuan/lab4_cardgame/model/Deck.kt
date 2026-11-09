package com.pengxiangyuan.lab4_cardgame.model

import java.util.Random

/**
 * Model 层：一副标准扑克牌（52 张 = 4 种花色 × 13 个点数）。
 *
 * 结构与老师给的 Deck 一致：构造时按花色、点数生成整副牌，抽牌时用 Random 随机取走一张，
 * 这样既保证抽出的牌不重复，也不需要额外写洗牌方法。
 *
 * 实验五新增 [drawCardWithRank]：三张牌的游戏按"同点数三张"分组发牌，
 * 需要指定点数抽牌，才能保证每局都发得完、配得完。
 */
class Deck {

    private val cards = mutableListOf<Card>()
    private val random = Random()

    init {
        for (suit in Card.validSuits) {
            for (rank in Card.rankStrings) {
                cards.add(Card(suit = suit, rank = rank))
            }
        }
    }

    /** 牌堆里还剩多少张 */
    val size: Int
        get() = cards.size

    /** 随机抽一张牌，牌堆空了就返回 null（对应老师代码里的 var randomCard: Card?） */
    fun drawRandomCard(): Card? {
        var randomCard: Card? = null
        if (cards.size > 0) {
            randomCard = cards.removeAt(random.nextInt(cards.size))
        }
        return randomCard
    }

    /** 按点数随机抽一张牌（同点数的四张里随便取一张），没有该点数时返回 null */
    fun drawCardWithRank(rank: String): Card? {
        val candidates = cards.indices.filter { cards[it].rank == rank }
        if (candidates.isEmpty()) return null
        return cards.removeAt(candidates[random.nextInt(candidates.size)])
    }
}
