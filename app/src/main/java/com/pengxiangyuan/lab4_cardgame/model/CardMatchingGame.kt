package com.pengxiangyuan.lab4_cardgame.model

/**
 * Model 层：翻牌游戏的规则与状态。
 *
 * 实验五把"两张配对"改成"三张配对"，整体结构不变：
 *  - 构造时新建一副 Deck 发牌，牌数由 [count] 决定（默认 24 张，正好 8 组）；
 *  - score 对外只读（private set），只能由 [chooseCardAtIndex] 改；
 *  - chooseCardAtIndex() 收集桌面上已翻开的牌，凑满三张就调用 [Card.match] 结算。
 *
 * 本次的三张牌规则（自己设计）：
 *  1. 每翻开一张牌扣 [COST_TO_CHOOSE] 分；
 *  2. 桌面上同时有 3 张已翻开的牌时进行比较：三张点数全部相同 → 配对成功，+40 分；
 *     否则三张一起翻回背面（停 0.8 秒，由界面调用 [flipBackMismatched]）；
 *  3. 8 组全部配完即获胜。
 *
 * 发牌方式也跟着改：不再是随机抽 24 张（随机抽几乎凑不出同点数的三张），
 * 而是随机挑 8 个点数，每个点数发 3 张不同花色，保证每局都能全部配完。
 *
 * 实验要求新增的 [reset] 方法：重新发牌，并把分数、组数、等待状态清零。
 */
class CardMatchingGame(private val count: Int = DEFAULT_CARD_COUNT) {
    var score = 0
        private set

    /** 已经配成的组数（三张一组） */
    var matchedSets = 0
        private set

    /** true 表示有三张牌对不上、正等着界面把它们翻回去 */
    var isWaitingForFlipBack = false
        private set

    private val cards: MutableList<Card> = mutableListOf()

    /** 等着翻回去的三张牌 */
    private var pendingFlipBack: Array<Card>? = null

    /** 最多能配成多少组（24 张即 8 组）；必须在 init 之前初始化，发牌时要用 */
    val setCount: Int = count / CARDS_PER_SET

    init {
        require(count % CARDS_PER_SET == 0) { "牌数必须是 $CARDS_PER_SET 的整数倍" }
        dealCards()
    }

    /** 桌面上有几张牌 */
    val cardCount: Int
        get() = cards.size

    /** 本局是否已经全部配对完成 */
    val isFinished: Boolean
        get() = matchedSets == setCount

    fun cardAtIndex(index: Int): Card {
        return cards[index]
    }

    /**
     * 发牌：随机挑 [setCount] 个点数，每个点数发三张不同花色的牌，
     * 打乱后摆上桌面，保证同点数的三张牌一定存在，每一局都能配完。
     */
    private fun dealCards() {
        cards.clear()
        val deck = Deck()
        // 先洗乱 13 个点数，取前 setCount 个作为本局的点数
        val ranks = Card.rankStrings.toMutableList()
        java.util.Collections.shuffle(ranks)
        for (rank in ranks.take(setCount)) {
            repeat(CARDS_PER_SET) {
                deck.drawCardWithRank(rank)?.let { cards.add(it) }
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
        if (openedCards.size < CARDS_PER_SET) return

        // 凑满三张：把刚翻开的这张和另外两张比较
        val otherCards = openedCards.filter { it !== card }.toTypedArray()
        val matchScore = card.match(otherCards)
        if (matchScore > 0) {
            // 三张点数相同：三张一起变成"废牌"，加分
            openedCards.forEach { it.isMatched = true }
            score += matchScore
            matchedSets++
        } else {
            // 对不上：三张先留在正面，等界面延时翻回
            pendingFlipBack = openedCards.toTypedArray()
            isWaitingForFlipBack = true
        }
    }

    /** 把刚才那三张对不上的牌翻回背面（由界面延时调用） */
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
        /** 每翻开一张牌扣的分 */
        const val COST_TO_CHOOSE = 1

        /** 一组几张牌（本次为三张） */
        const val CARDS_PER_SET = 3

        /** 默认桌面牌数：竖屏 6 行 × 4 列 = 24 张 */
        const val DEFAULT_CARD_COUNT = 24
    }
}
