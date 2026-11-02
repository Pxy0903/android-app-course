package com.pengxiangyuan.lab4_cardgame.model

import java.util.Random

/**
 * Model 层：一副标准扑克牌（52 张 = 4 种花色 × 13 个点数）。
 *
 * 结构与老师给的 Deck 一致：构造时按花色、点数生成整副牌，抽牌时用 Random 随机取走一张，
 * 这样既保证抽出的牌不重复，也不需要额外写洗牌方法。
 * 与老师原版的区别：多保留了 size（测试里要验证"一副牌是 52 张"），其余写法保持原样。
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
}
