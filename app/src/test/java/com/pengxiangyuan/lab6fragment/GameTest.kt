package com.pengxiangyuan.lab6fragment

import com.pengxiangyuan.lab6fragment.model.Card
import com.pengxiangyuan.lab6fragment.model.CardMatchingGame
import com.pengxiangyuan.lab6fragment.model.Deck
import com.pengxiangyuan.lab6fragment.model.PersonInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

/**
 * 单元测试：只测试 Model 层（纯 Kotlin 逻辑，不依赖 Android 环境，直接跑在 JVM 上）。
 *
 * 覆盖实验六的两种玩法：
 *  - 两张牌玩法：24 张随机牌、翻一张 -1 分、点数相同 +15 / 花色相同 +4、对不上等待翻回；
 *  - 三张牌玩法：24 张 = 8 组 × 3 张同点数、三张点数全同 +40 分；
 *  - 以及实验六的核心要求：牌局与个人信息支持 Java 序列化（写出去再读回来内容一致）。
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

    // ---------- 两张牌玩法（第 2 个 Fragment） ----------

    @Test
    fun game2_init_hasTwentyFourRandomCards() {
        val game = CardMatchingGame(CardMatchingGame.DEFAULT_CARD_COUNT, CardMatchingGame.TWO_CARD_GROUP)
        assertEquals(24, game.cardCount)
        assertEquals(12, game.setCount)
        assertEquals(2, game.cardsPerGroup)
        assertEquals(0, game.score)
        assertFalse(game.isFinished)
    }

    @Test
    fun game2_pairSameRank_addsFifteenPoints() {
        val game = CardMatchingGame(CardMatchingGame.DEFAULT_CARD_COUNT, CardMatchingGame.TWO_CARD_GROUP)
        val pair = findSameRankPair(game)
        require(pair != null) { "随机 24 张里应存在同点数的一对" }

        game.chooseCardAtIndex(pair[0])
        game.chooseCardAtIndex(pair[1])

        assertEquals(Card.MATCH_BONUS - 2 * CardMatchingGame.COST_TO_CHOOSE, game.score)
        assertTrue(pair.all { game.cardAtIndex(it).isMatched })
        assertEquals(1, game.matchedSets)
        assertFalse(game.isWaitingForFlipBack)
    }

    @Test
    fun game2_mismatchedPair_waitsThenFlipsBack() {
        val game = CardMatchingGame(CardMatchingGame.DEFAULT_CARD_COUNT, CardMatchingGame.TWO_CARD_GROUP)
        // 找一对点数、花色都不同的牌
        val pair = (0 until game.cardCount).flatMap { i ->
            ((i + 1) until game.cardCount).map { j -> listOf(i, j) }
        }.first { (i, j) ->
            val a = game.cardAtIndex(i); val b = game.cardAtIndex(j)
            a.rank != b.rank && a.suit != b.suit
        }

        game.chooseCardAtIndex(pair[0])
        game.chooseCardAtIndex(pair[1])

        assertEquals(-2, game.score)
        assertEquals(0, game.matchedSets)
        assertTrue(game.isWaitingForFlipBack)

        // 等待期间点第三张牌不处理、也不扣分
        val other = (0 until game.cardCount).first { it != pair[0] && it != pair[1] }
        game.chooseCardAtIndex(other)
        assertEquals(-2, game.score)
        assertFalse(game.cardAtIndex(other).isChosen)

        game.flipBackMismatched()
        assertFalse(game.cardAtIndex(pair[0]).isChosen)
        assertFalse(game.cardAtIndex(pair[1]).isChosen)
        assertFalse(game.isWaitingForFlipBack)
    }

    // ---------- 三张牌玩法（第 3 个 Fragment） ----------

    @Test
    fun game3_init_hasTwentyFourCardsInEightSets() {
        val game = CardMatchingGame(CardMatchingGame.DEFAULT_CARD_COUNT, CardMatchingGame.THREE_CARD_GROUP)
        assertEquals(24, game.cardCount)
        assertEquals(8, game.setCount)
        assertEquals(3, game.cardsPerGroup)

        // 牌面互不重复
        val faces = (0 until game.cardCount).map { game.cardAtIndex(it).toString() }
        assertEquals(24, faces.toSet().size)

        // 每个点数正好 3 张，保证一定能配完
        val rankCounts = faces.groupBy { it.drop(1) }.mapValues { it.value.size }
        assertEquals(8, rankCounts.size)
        assertTrue(rankCounts.values.all { it == 3 })
    }

    @Test
    fun game3_threeSameRank_matchesAndAddsFortyPoints() {
        val game = CardMatchingGame(CardMatchingGame.DEFAULT_CARD_COUNT, CardMatchingGame.THREE_CARD_GROUP)
        val triple = findGroup(game)

        game.chooseCardAtIndex(triple[0])
        game.chooseCardAtIndex(triple[1])
        game.chooseCardAtIndex(triple[2])

        assertEquals(Card.THREE_MATCH_BONUS - 3 * CardMatchingGame.COST_TO_CHOOSE, game.score)
        assertTrue(triple.all { game.cardAtIndex(it).isMatched })
        assertEquals(1, game.matchedSets)
        assertFalse(game.isWaitingForFlipBack)
    }

    @Test
    fun game3_mismatchedTriple_waitsThenFlipsBack() {
        val game = CardMatchingGame(CardMatchingGame.DEFAULT_CARD_COUNT, CardMatchingGame.THREE_CARD_GROUP)
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

    // ---------- 通用规则 ----------

    @Test
    fun clickingSameCardTwice_isIgnored() {
        val game = CardMatchingGame(CardMatchingGame.DEFAULT_CARD_COUNT, CardMatchingGame.THREE_CARD_GROUP)
        game.chooseCardAtIndex(0)
        game.chooseCardAtIndex(0)
        assertEquals(-1, game.score)
    }

    @Test
    fun reset_clearsBoardAndScore() {
        val game = CardMatchingGame(CardMatchingGame.DEFAULT_CARD_COUNT, CardMatchingGame.THREE_CARD_GROUP)
        val triple = findGroup(game)
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

    // ---------- 实验六核心：对象序列化 ----------

    @Test
    fun serialization_game_roundTripKeepsState() {
        val game = CardMatchingGame(CardMatchingGame.DEFAULT_CARD_COUNT, CardMatchingGame.THREE_CARD_GROUP)
        val triple = findGroup(game)
        triple.forEach { game.chooseCardAtIndex(it) }

        val saved = writeAndRead(game)
        assertNotNull(saved)
        assertEquals(game.score, saved!!.score)
        assertEquals(game.matchedSets, saved.matchedSets)
        assertEquals(game.cardCount, saved.cardCount)
        assertEquals(game.setCount, saved.setCount)
        // 每张牌的翻开 / 配对状态都一致
        for (i in 0 until game.cardCount) {
            assertEquals(game.cardAtIndex(i).toString(), saved.cardAtIndex(i).toString())
            assertEquals(game.cardAtIndex(i).isChosen, saved.cardAtIndex(i).isChosen)
            assertEquals(game.cardAtIndex(i).isMatched, saved.cardAtIndex(i).isMatched)
        }
    }

    @Test
    fun serialization_personInfo_roundTripKeepsFields() {
        val info = PersonInfo(name = "彭湘媛", gender = "女", phone = "2024110126")
        val saved = writeAndRead(info)

        assertNotNull(saved)
        assertEquals(info.name, saved!!.name)
        assertEquals(info.gender, saved.gender)
        assertEquals(info.phone, saved.phone)
    }

    /** 用 ObjectInputStream / ObjectOutputStream 做一次完整的写出、读回 */
    private inline fun <reified T : java.io.Serializable> writeAndRead(obj: T): T? {
        val bytes = ByteArrayOutputStream()
        val out = ObjectOutputStream(bytes)
        out.writeObject(obj)
        out.close()

        val input = ObjectInputStream(ByteArrayInputStream(bytes.toByteArray()))
        val result = input.readObject() as? T
        input.close()
        return result
    }

    /** 三张牌玩法：找三张点数相同的牌（发牌保证每个点数正好三张） */
    private fun findGroup(game: CardMatchingGame): List<Int> {
        return (0 until game.cardCount)
            .groupBy { game.cardAtIndex(it).rank }
            .values
            .first { it.size == game.cardsPerGroup }
    }

    /** 两张牌玩法：找一对点数相同的牌；找不到返回 null */
    private fun findSameRankPair(game: CardMatchingGame): List<Int>? {
        return (0 until game.cardCount).flatMap { i ->
            ((i + 1) until game.cardCount).map { j -> listOf(i, j) }
        }.firstOrNull { (i, j) -> game.cardAtIndex(i).rank == game.cardAtIndex(j).rank }
    }
}
