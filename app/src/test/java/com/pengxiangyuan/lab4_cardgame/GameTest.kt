package com.pengxiangyuan.lab4_cardgame

import com.pengxiangyuan.lab4_cardgame.model.Card
import com.pengxiangyuan.lab4_cardgame.model.CardMatchingGame
import com.pengxiangyuan.lab4_cardgame.model.Deck
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 单元测试：只测试 Model 层（纯 Kotlin 逻辑，不依赖 Android 环境，直接跑在 JVM 上）。
 *
 * 覆盖点：52 张牌堆、开局 16 张互不重复、match() 的三种结果、翻牌扣 1 分、
 * 点数相同 +15、花色相同 +4、对不上不加分且等待翻回、重复点击、重置游戏。
 */
class GameTest {

    /** Card 的花色、点数是 private 的，测试里借助 match() 的返回值判断两张牌的关系 */
    private fun Card.isSameRank(other: Card) = match(arrayOf(other)) == Card.MATCH_BONUS

    private fun Card.isSameSuit(other: Card) = match(arrayOf(other)) == Card.SUIT_BONUS

    /** 找两张点数相同的牌 —— 16 张牌只分 13 个点数，必然存在 */
    private fun findSameRank(game: CardMatchingGame): Pair<Int, Int> {
        for (i in 0 until game.cardCount) {
            for (j in i + 1 until game.cardCount) {
                if (game.cardAtIndex(i).isSameRank(game.cardAtIndex(j))) return i to j
            }
        }
        throw IllegalStateException("没有找到点数相同的两张牌")
    }

    /** 找两张花色相同、点数不同的牌 —— 16 张牌只分 4 个花色，必然存在 */
    private fun findSameSuitDiffRank(game: CardMatchingGame): Pair<Int, Int> {
        for (i in 0 until game.cardCount) {
            for (j in i + 1 until game.cardCount) {
                if (game.cardAtIndex(i).isSameSuit(game.cardAtIndex(j))) return i to j
            }
        }
        throw IllegalStateException("没有找到花色相同的两张牌")
    }

    /** 找两张点数、花色都不相同的牌 */
    private fun findNothingInCommon(game: CardMatchingGame): Pair<Int, Int> {
        for (i in 0 until game.cardCount) {
            for (j in i + 1 until game.cardCount) {
                val a = game.cardAtIndex(i)
                val b = game.cardAtIndex(j)
                if (!a.isSameRank(b) && !a.isSameSuit(b)) return i to j
            }
        }
        throw IllegalStateException("没有找到毫无共同点的两张牌")
    }

    @Test
    fun deck_isStandardFiftyTwoCards() {
        val deck = Deck()
        assertEquals(52, deck.size)

        // 抽 16 张后还剩 36 张，且抽出来的牌互不重复
        val drawn = (1..16).map { deck.drawRandomCard()!! }
        assertEquals(36, deck.size)
        assertEquals(16, drawn.map { it.toString() }.toSet().size)
    }

    @Test
    fun match_pointsByRule() {
        val aceOfSpade = Card(suit = "♠", rank = "A")
        assertEquals(Card.MATCH_BONUS, aceOfSpade.match(arrayOf(Card(suit = "♥", rank = "A"))))
        assertEquals(Card.SUIT_BONUS, aceOfSpade.match(arrayOf(Card(suit = "♠", rank = "K"))))
        assertEquals(0, aceOfSpade.match(arrayOf(Card(suit = "♥", rank = "K"))))
    }

    @Test
    fun gameInit_hasSixteenUniqueCards() {
        val game = CardMatchingGame(16)
        assertEquals(16, game.cardCount)
        assertEquals(8, game.pairCount)
        assertEquals(0, game.score)

        // 牌面互不重复
        val faces = (0 until game.cardCount).map { game.cardAtIndex(it).toString() }
        assertEquals(16, faces.toSet().size)
        assertFalse(game.isFinished)
    }

    @Test
    fun flippingCard_costsOnePoint() {
        val game = CardMatchingGame(16)
        game.chooseCardAtIndex(0)
        assertEquals(-1, game.score) // 翻开第一张就扣 1 分
        assertTrue(game.cardAtIndex(0).isChosen)
    }

    @Test
    fun sameRank_addsFifteenPoints() {
        val game = CardMatchingGame(16)
        val (i, j) = findSameRank(game)

        game.chooseCardAtIndex(i) // 翻第一张：-1
        game.chooseCardAtIndex(j) // 翻第二张：-1，点数相同 +15

        assertEquals(Card.MATCH_BONUS - 2 * CardMatchingGame.COST_TO_CHOOSE, game.score)
        assertTrue(game.cardAtIndex(i).isMatched)
        assertTrue(game.cardAtIndex(j).isMatched)
        assertEquals(1, game.matchedPairs)
    }

    @Test
    fun sameSuit_addsFourPoints() {
        val game = CardMatchingGame(16)
        val (i, j) = findSameSuitDiffRank(game)

        game.chooseCardAtIndex(i) // -1
        game.chooseCardAtIndex(j) // -1，花色相同 +4

        assertEquals(Card.SUIT_BONUS - 2 * CardMatchingGame.COST_TO_CHOOSE, game.score)
        assertTrue(game.cardAtIndex(i).isMatched)
        assertTrue(game.cardAtIndex(j).isMatched)
        assertEquals(1, game.matchedPairs)
    }

    @Test
    fun nothingInCommon_waitsThenFlipsBack() {
        val game = CardMatchingGame(16)
        val (i, j) = findNothingInCommon(game)

        game.chooseCardAtIndex(i)
        game.chooseCardAtIndex(j)

        // 对不上只承担两次翻牌的 1 分，不加分也不额外扣分
        assertEquals(-2, game.score)
        assertFalse(game.cardAtIndex(i).isMatched)
        assertTrue(game.cardAtIndex(i).isChosen) // 等待翻回期间保持正面朝上
        assertTrue(game.isWaitingForFlipBack)

        // 等待期间点第三张牌不处理、也不扣分
        val other = (0 until game.cardCount).first { it != i && it != j }
        game.chooseCardAtIndex(other)
        assertEquals(-2, game.score)
        assertFalse(game.cardAtIndex(other).isChosen)

        game.flipBackMismatched()
        assertFalse(game.cardAtIndex(i).isChosen)
        assertFalse(game.cardAtIndex(j).isChosen)
        assertFalse(game.isWaitingForFlipBack)
    }

    @Test
    fun clickingSameCardTwice_isIgnored() {
        val game = CardMatchingGame(16)
        game.chooseCardAtIndex(0)
        game.chooseCardAtIndex(0) // 同一张牌重复点应无效，也不再扣分

        assertEquals(-1, game.score)
        assertTrue(game.cardAtIndex(0).isChosen)
    }

    @Test
    fun restart_clearsBoardAndScore() {
        val game = CardMatchingGame(16)
        val (i, j) = findSameRank(game)
        game.chooseCardAtIndex(i)
        game.chooseCardAtIndex(j)
        assertTrue(game.matchedPairs > 0)

        game.restart()
        assertEquals(0, game.score)
        assertEquals(0, game.matchedPairs)
        assertEquals(16, game.cardCount)
        assertFalse(game.cardAtIndex(0).isChosen)
        assertFalse(game.cardAtIndex(0).isMatched)
        assertFalse(game.isWaitingForFlipBack)
    }
}
