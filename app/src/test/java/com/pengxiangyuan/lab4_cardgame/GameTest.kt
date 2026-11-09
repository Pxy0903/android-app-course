package com.pengxiangyuan.lab4_cardgame

import com.pengxiangyuan.lab4_cardgame.model.Card
import com.pengxiangyuan.lab4_cardgame.model.CardMatchingGame
import com.pengxiangyuan.lab4_cardgame.model.Deck
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 单元测试：只测试 Model 层（纯 Kotlin 逻辑，不依赖 Android 环境，直接跑在 JVM 上）。
 *
 * 覆盖实验五的三张牌规则：24 张牌 = 8 组 × 3 张、每组点数相同；
 * 翻一张 -1 分、三张点数全同 +40 分、对不上等待翻回、重复点击无效、reset 清零。
 */
class GameTest {

    @Test
    fun deck_isStandardFiftyTwoCards() {
        val deck = Deck()
        assertEquals(52, deck.size)

        val drawn = (1..24).map { deck.drawRandomCard()!! }
        assertEquals(28, deck.size)
        assertEquals(24, drawn.map { it.toString() }.toSet().size)
    }

    @Test
    fun drawCardWithRank_returnsCardOfThatRank() {
        val deck = Deck()
        val card = deck.drawCardWithRank("A")
        assertNotNull(card)
        assertEquals("A", card!!.rank)
        assertEquals(51, deck.size)
    }

    @Test
    fun match_twoCards_usesLab4Rule() {
        val aceOfSpade = Card(suit = "♠", rank = "A")
        assertEquals(Card.MATCH_BONUS, aceOfSpade.match(arrayOf(Card(suit = "♥", rank = "A"))))
        assertEquals(Card.SUIT_BONUS, aceOfSpade.match(arrayOf(Card(suit = "♠", rank = "K"))))
        assertEquals(0, aceOfSpade.match(arrayOf(Card(suit = "♥", rank = "K"))))
    }

    @Test
    fun match_threeCards_needsSameRank() {
        val ace = Card(suit = "♠", rank = "A")
        // 三张点数相同 → 40 分
        assertEquals(
            Card.THREE_MATCH_BONUS,
            ace.match(arrayOf(Card(suit = "♥", rank = "A"), Card(suit = "♦", rank = "A")))
        )
        // 只有两张点数相同 → 不算配对
        assertEquals(
            0,
            ace.match(arrayOf(Card(suit = "♥", rank = "A"), Card(suit = "♦", rank = "K")))
        )
        // 花色相同但点数不同 → 不算配对
        assertEquals(
            0,
            ace.match(arrayOf(Card(suit = "♠", rank = "K"), Card(suit = "♠", rank = "Q")))
        )
    }

    @Test
    fun gameInit_hasTwentyFourCardsInEightSets() {
        val game = CardMatchingGame(CardMatchingGame.DEFAULT_CARD_COUNT)
        assertEquals(24, game.cardCount)
        assertEquals(8, game.setCount)
        assertEquals(0, game.score)
        assertEquals(0, game.matchedSets)

        // 牌面互不重复
        val faces = (0 until game.cardCount).map { game.cardAtIndex(it).toString() }
        assertEquals(24, faces.toSet().size)

        // 每个点数正好 3 张，保证一定能配完
        val rankCounts = faces.groupBy { it.drop(1) }.mapValues { it.value.size }
        assertEquals(8, rankCounts.size)
        assertTrue(rankCounts.values.all { it == 3 })

        assertFalse(game.isFinished)
    }

    @Test
    fun flippingCard_costsOnePoint() {
        val game = CardMatchingGame()
        game.chooseCardAtIndex(0)
        assertEquals(-1, game.score)
        assertTrue(game.cardAtIndex(0).isChosen)
        // 只翻开两张时还不比较，也不扣额外的分
        game.chooseCardAtIndex(1)
        assertEquals(-2, game.score)
    }

    @Test
    fun threeSameRank_matchesAndAddsFortyPoints() {
        val game = CardMatchingGame()
        val triple = findTriple(game)

        game.chooseCardAtIndex(triple[0])
        game.chooseCardAtIndex(triple[1])
        game.chooseCardAtIndex(triple[2])

        assertEquals(Card.THREE_MATCH_BONUS - 3 * CardMatchingGame.COST_TO_CHOOSE, game.score)
        assertTrue(triple.all { game.cardAtIndex(it).isMatched })
        assertEquals(1, game.matchedSets)
        assertFalse(game.isWaitingForFlipBack)
    }

    @Test
    fun mismatchedTriple_waitsThenFlipsBack() {
        val game = CardMatchingGame()
        val first = 0
        val second = (0 until game.cardCount).first {
            it != first && game.cardAtIndex(it).rank != game.cardAtIndex(first).rank
        }
        val third = (0 until game.cardCount).first {
            it != first && it != second &&
                game.cardAtIndex(it).rank != game.cardAtIndex(first).rank &&
                game.cardAtIndex(it).rank != game.cardAtIndex(second).rank
        }

        game.chooseCardAtIndex(first)
        game.chooseCardAtIndex(second)
        game.chooseCardAtIndex(third)

        // 对不上：只承担三次翻牌的 1 分，三张先留在正面
        assertEquals(-3, game.score)
        assertEquals(0, game.matchedSets)
        assertTrue(game.isWaitingForFlipBack)
        assertTrue(game.cardAtIndex(first).isChosen)

        // 等待期间点第四张牌不处理、也不扣分
        val other = (0 until game.cardCount).first { it != first && it != second && it != third }
        game.chooseCardAtIndex(other)
        assertEquals(-3, game.score)
        assertFalse(game.cardAtIndex(other).isChosen)

        game.flipBackMismatched()
        assertFalse(game.cardAtIndex(first).isChosen)
        assertFalse(game.cardAtIndex(second).isChosen)
        assertFalse(game.cardAtIndex(third).isChosen)
        assertFalse(game.isWaitingForFlipBack)
    }

    @Test
    fun clickingSameCardTwice_isIgnored() {
        val game = CardMatchingGame()
        game.chooseCardAtIndex(0)
        game.chooseCardAtIndex(0)
        assertEquals(-1, game.score)
    }

    @Test
    fun reset_clearsBoardAndScore() {
        val game = CardMatchingGame()
        val triple = findTriple(game)
        triple.forEach { game.chooseCardAtIndex(it) }
        assertTrue(game.matchedSets > 0)

        game.reset()
        assertEquals(0, game.score)
        assertEquals(0, game.matchedSets)
        assertEquals(24, game.cardCount)
        assertFalse(game.cardAtIndex(0).isChosen)
        assertFalse(game.cardAtIndex(0).isMatched)
        assertFalse(game.isWaitingForFlipBack)
    }

    /** 找三张点数相同的牌（发牌保证每个点数正好三张） */
    private fun findTriple(game: CardMatchingGame): List<Int> {
        return (0 until game.cardCount)
            .groupBy { game.cardAtIndex(it).rank }
            .values
            .first { it.size == CardMatchingGame.CARDS_PER_SET }
    }
}
