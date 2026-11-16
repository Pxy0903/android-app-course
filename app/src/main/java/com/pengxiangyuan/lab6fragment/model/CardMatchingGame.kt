package com.pengxiangyuan.lab6fragment.model

import java.io.Serializable

/**
 * Model 层：翻牌游戏的规则与状态。
 *
 * 实验六在实验五的基础上把"每组几张牌"改成可配置（[groupSize]），
 * 用同一个 Model 同时支撑两个 Fragment 的两种玩法：
 *  - 两张牌玩法：groupSize = 2，随机发牌（实验四规则：点数 15 分 / 花色 4 分）；
 *  - 三张牌玩法：groupSize = 3，按"同点数三张"发牌（实验五规则：三张全同 +40 分）。
 *
 * 通用规则：
 *  1. 每翻开一张牌扣 [COST_TO_CHOOSE] 分；
 *  2. 桌面上已翻开的牌凑满一组时调用 [Card.match] 结算：配对成功加分并标记"废牌"，
 *     否则整组先留在正面，由界面延时调用 [flipBackMismatched] 翻回；
 *  3. 全部组配完即获胜（[isFinished]）。
 *
 * 实验六要求游戏状态采用序列化存储，因此本类实现 [Serializable]，
 * 由界面在 onStop 时用 ObjectOutputStream 写入私有文件、再次进入时用 ObjectInputStream 读回。
 */
class CardMatchingGame(
    private val count: Int = DEFAULT_CARD_COUNT,
    private val groupSize: Int = THREE_CARD_GROUP
) : Serializable {

    var score = 0
        private set

    /** 已经配成的组数 */
    var matchedSets = 0
        private set

    /** true 表示有一组牌对不上、正等着界面把它们翻回去 */
    var isWaitingForFlipBack = false
        private set

    private val cards: MutableList<Card> = mutableListOf()

    /** 等着翻回去的一组牌 */
    private var pendingFlipBack: Array<Card>? = null

    /** 最多能配成多少组；必须在 init 之前初始化，发牌时要用 */
    val setCount: Int = count / groupSize

    init {
        require(count % groupSize == 0) { "牌数必须是 $groupSize 的整数倍" }
        dealCards()
    }

    /** 桌面上有几张牌 */
    val cardCount: Int
        get() = cards.size

    /** 每组几张牌（2 = 两张牌玩法，3 = 三张牌玩法） */
    val cardsPerGroup: Int
        get() = groupSize

    /** 本局是否已经全部配对完成 */
    val isFinished: Boolean
        get() = matchedSets == setCount

    fun cardAtIndex(index: Int): Card {
        return cards[index]
    }

    /**
     * 发牌：
     *  - 两张牌玩法沿用实验四的方式，从整副牌里随机抽 [count] 张；
     *  - 三张牌玩法沿用实验五的方式：随机挑 [setCount] 个点数，每个点数发三张不同花色，
     *    保证同点数的三张牌一定存在，每一局都能配完。
     */
    private fun dealCards() {
        cards.clear()
        val deck = Deck()
        if (groupSize <= 1 || groupSize == TWO_CARD_GROUP) {
            repeat(count) { deck.drawRandomCard()?.let { cards.add(it) } }
        } else {
            // 先洗乱 13 个点数，取前 setCount 个作为本局的点数
            val ranks = Card.rankStrings.toMutableList()
            java.util.Collections.shuffle(ranks)
            for (rank in ranks.take(setCount)) {
                repeat(groupSize) {
                    deck.drawCardWithRank(rank)?.let { cards.add(it) }
                }
            }
        }
        java.util.Collections.shuffle(cards)
    }

    /**
     * 玩家点了第 index 张牌。
     * 已配对的牌、已翻开的牌、以及"等待翻回"期间的点击都不处理（也不扣分）。
     */
    fun chooseCardAtIndex(index: Int) {
        if (isWaitingForFlipBack) return
        if (index !in cards.indices) return

        val card = cards[index]
        if (card.isMatched || card.isChosen) return

        // 翻开就算一次操作，先扣一分
        card.isChosen = true
        score -= COST_TO_CHOOSE

        // 收集桌面上已翻开、还没配对的牌
        val openedCards = cards.filter { it.isChosen && !it.isMatched }
        if (openedCards.size < groupSize) return

        // 凑满一组：把刚翻开的这张和另外几张比较
        val otherCards = openedCards.filter { it !== card }.toTypedArray()
        val matchScore = card.match(otherCards)
        if (matchScore > 0) {
            // 配对成功：整组变成"废牌"，加分
            openedCards.forEach { it.isMatched = true }
            score += matchScore
            matchedSets++
        } else {
            // 对不上：整组先留在正面，等界面延时翻回
            pendingFlipBack = openedCards.toTypedArray()
            isWaitingForFlipBack = true
        }
    }

    /** 把刚才那组对不上的牌翻回背面（由界面延时调用） */
    fun flipBackMismatched() {
        val pending = pendingFlipBack ?: return
        for (card in pending) {
            card.isChosen = false
        }
        pendingFlipBack = null
        isWaitingForFlipBack = false
    }

    /** 重新开始一局：重新发牌，并把分数、组数、等待状态全部清零 */
    fun reset() {
        dealCards()
        score = 0
        matchedSets = 0
        pendingFlipBack = null
        isWaitingForFlipBack = false
    }

    companion object {
        /** 序列化版本号 */
        private const val serialVersionUID = 1L

        /** 每翻开一张牌扣的分 */
        const val COST_TO_CHOOSE = 1

        /** 两张牌玩法：一组两张 */
        const val TWO_CARD_GROUP = 2

        /** 三张牌玩法：一组三张 */
        const val THREE_CARD_GROUP = 3

        /** 默认桌面牌数：竖屏 6 行 × 4 列 = 24 张 */
        const val DEFAULT_CARD_COUNT = 24
    }
}
