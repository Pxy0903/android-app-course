package com.pengxiangyuan.lab4_cardgame.model

/**
 * Model 层：翻牌游戏的规则与状态。
 *
 * 结构与老师给的 CardMatchingGame 一致：
 *  - 构造时新建一副 Deck，用 drawRandomCard() 连抽 count 张摆上桌面；
 *  - score 对外只读（private set），只能由 chooseCardAtIndex() 改；
 *  - chooseCardAtIndex() 也是"遍历桌面找另一张已翻开的牌 → 调 match() → 结算"的写法。
 *
 * 与老师原版相比改了三处（都是为了满足本次实验的规则和界面效果）：
 *  1. 得分：每翻一张扣 COST_TO_CHOOSE(1) 分，配对成功加上 match() 返回的分数（点数 15 / 花色 4），
 *     对不上不额外扣分（老师原版的 MISMATCH_PENALTY 那 2 分去掉了），得分允许为负；
 *  2. 翻回时机：对不上的两张牌不立刻翻回去，而是把牌记在 pendingFlipBack 里，
 *     由界面停 0.8 秒后调用 flipBackMismatched() 再翻，玩家才看得清翻到了什么；
 *  3. 增加 restart()：实验要求"可以重新开始一局游戏"，重新发牌并把分数、对数清零。
 *
 * 老师的 cards.get(index) 这种老写法已经换成 Kotlin 的下标写法 cards[ ]，
 * 功能完全一样，只是现在更常用。
 */
class CardMatchingGame(private val count: Int) {
    var score = 0
        private set
    private val cards: MutableList<Card> = mutableListOf()
    /** 已经配成的对数 */
    var matchedPairs = 0
        private set

    /** true 表示有两张牌对不上、正等着界面把它们翻回去 */
    var isWaitingForFlipBack = false
        private set

    /** 等着翻回去的两张牌 */
    private var pendingFlipBack: Array<Card>? = null

    init {
        dealCards()
    }

    /** 桌面上有几张牌 */
    val cardCount: Int
        get() = cards.size

    /** 最多能配成多少对（16 张即 8 对） */
    val pairCount: Int
        get() = cards.size / 2

    /** 本局是否已经全部配对完成 */
    val isFinished: Boolean
        get() = matchedPairs == pairCount

    fun cardAtIndex(index: Int): Card {
        return cards[index]
    }

    /** 发牌：新建一副 52 张的牌，随机抽 count 张摆上桌面 */
    private fun dealCards() {
        cards.clear()
        val deck = Deck()
        for (i in 1..count) {
            val card: Card? = deck.drawRandomCard()
            if (card != null) {
                cards.add(card)
            }
        }
    }

    /**
     * 玩家点了第 index 张牌。
     * 已配对的牌、已翻开的牌、以及"等待翻回"期间的点击都不处理（也不扣分）。
     */
    fun chooseCardAtIndex(index: Int) {
        if (isWaitingForFlipBack) return

        val card = cardAtIndex(index)
        if (card.isMatched) return
        if (card.isChosen) return

        // 翻开就算一次操作，先扣一分
        card.isChosen = true
        score -= COST_TO_CHOOSE

        // 在桌面上找另一张已翻开、还没配对的牌来比较
        for (otherCard in cards) {
            if (otherCard !== card && otherCard.isChosen && !otherCard.isMatched) {
                val matchScore = card.match(arrayOf(otherCard))
                if (matchScore > 0) {
                    // 点数或花色相同：两张都变成"废牌"，加分
                    score += matchScore
                    otherCard.isMatched = true
                    card.isMatched = true
                    matchedPairs++
                } else {
                    // 对不上：两张先留在正面，等界面延时翻回
                    pendingFlipBack = arrayOf(otherCard, card)
                    isWaitingForFlipBack = true
                }
                return
            }
        }
    }

    /** 把刚才那两张对不上的牌翻回背面（由界面延时调用） */
    fun flipBackMismatched() {
        val pending = pendingFlipBack ?: return
        for (card in pending) {
            card.isChosen = false
        }
        pendingFlipBack = null
        isWaitingForFlipBack = false
    }

    /** 重新开始一局：重新发牌并把分数、对数、等待状态全部清零 */
    fun restart() {
        dealCards()
        score = 0
        matchedPairs = 0
        pendingFlipBack = null
        isWaitingForFlipBack = false
    }

    companion object {
        /** 每翻开一张牌扣的分 */
        const val COST_TO_CHOOSE = 1
    }
}
