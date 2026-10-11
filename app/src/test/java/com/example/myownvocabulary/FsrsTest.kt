package com.example.myownvocabulary

import com.example.myownvocabulary.data.learning.FsrsCardEntity
import com.example.myownvocabulary.data.learning.FsrsReviewEntity
import com.example.myownvocabulary.model.fsrs.CardPhase
import com.example.myownvocabulary.model.fsrs.FSRS6
import com.example.myownvocabulary.model.fsrs.FlashCard
import com.example.myownvocabulary.model.fsrs.MemoryState
import com.example.myownvocabulary.model.fsrs.Rating
import com.example.myownvocabulary.model.fsrs.ReviewLog
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class FsrsTest {
    private val day = FSRS6.DAY_MILLIS
    private val start = 1_767_225_600_000L

    private fun reviewed(stability: Double = 10.0, difficulty: Double = 5.0): FlashCard = FlashCard(
        id = "card",
        memory = MemoryState(stability, difficulty),
        dueAt = start + 7 * day,
        lastReviewAt = start,
        reviewCount = 1,
        phase = CardPhase.Review,
        interval = 7
    )

    @Test
    fun matchesIndependentPythonReference() {
        val resource = requireNotNull(javaClass.getResourceAsStream("/fsrs6-reference.csv"))
        val lines = resource.bufferedReader().use { it.readLines().drop(1) }
        assertTrue(lines.size >= 700)
        lines.forEachIndexed { index, line ->
            val c = line.split(',')
            val phase = CardPhase.valueOf(c[1])
            val last = c[5].toLong()
            val card = if (phase == CardPhase.Added) {
                FlashCard("reference")
            } else {
                FlashCard(
                    "reference", MemoryState(c[3].toDouble(), c[4].toDouble()), last, last,
                    1, 0, phase, c[2].toIntOrNull(), if (phase == CardPhase.Review) 1 else 0
                )
            }
            val steps = when (c[0]) {
                "empty" -> emptyList()
                "single" -> listOf(60_000L)
                else -> listOf(60_000L, 600_000L)
            }
            val scheduler = FSRS6(
                requestRetention = c[8].toDouble(),
                maximumInterval = c[9].toInt(),
                enableFuzz = false,
                learningSteps = steps,
                relearningSteps = if (c[0] == "empty") emptyList() else listOf(600_000L)
            )
            val rating = Rating.entries.single { it.value == c[7].toInt() }
            val proposal = scheduler.review(card, rating, last + c[6].toLong())
            val memory = requireNotNull(proposal.card.memory)
            val label = "reference case ${index + 1}: $line"
            assertEquals(label, c[10].toDouble(), memory.stability, maxOf(1e-10, abs(c[10].toDouble()) * 1e-12))
            assertEquals(label, c[11].toDouble(), memory.difficulty, 1e-12)
            assertEquals(label, c[12], proposal.card.phase.name)
            assertEquals(label, c[13].toIntOrNull(), proposal.card.learningStep)
            assertEquals(label, c[14].toLong(), proposal.durationMillis)
            assertEquals(label, last + c[6].toLong() + proposal.durationMillis, proposal.card.dueAt)
        }
    }

    @Test
    fun forgettingCurveAndIntervalAreInverses() {
        for (retention in listOf(0.7, 0.8, 0.9, 0.95, 0.99)) {
            val fsrs = FSRS6(requestRetention = retention)
            for (stability in listOf(0.001, 0.212, 2.3065, 100.0, 100000.0)) {
                assertEquals(retention, fsrs.forgettingCurve(fsrs.intervalDays(stability), stability), 1e-12)
                assertEquals(0.9, fsrs.forgettingCurve(stability, stability), 1e-12)
            }
        }
    }

    @Test
    fun actualElapsedTimeOverridesScheduledInterval() {
        val fsrs = FSRS6(enableFuzz = false)
        val card = reviewed()
        val onTime = fsrs.review(card, Rating.Good, start + 7 * day)
        val late = fsrs.review(card, Rating.Good, start + 30 * day)
        assertEquals(30L, late.log.elapsedDays)
        assertTrue(requireNotNull(late.card.memory).stability > requireNotNull(onTime.card.memory).stability)
        assertEquals(
            late,
            fsrs.review(card.copy(interval = 999), Rating.Good, start + 30 * day).let {
                it.copy(before = card)
            }
        )
    }

    @Test
    fun completeLearningAndRelearningCycles() {
        val fsrs = FSRS6(enableFuzz = false)
        val first = fsrs.review(FlashCard("card"), Rating.Good, start)
        assertEquals(CardPhase.Learning, first.card.phase)
        assertEquals(1, first.card.learningStep)
        assertEquals(600_000L, first.durationMillis)
        val graduated = fsrs.review(first.card, Rating.Good, first.card.dueAt)
        assertEquals(CardPhase.Review, graduated.card.phase)
        val forgotten = fsrs.review(graduated.card, Rating.Again, graduated.card.dueAt)
        assertEquals(CardPhase.ReLearning, forgotten.card.phase)
        assertEquals(1, forgotten.card.lapseCount)
        val repeatedFailure = fsrs.review(forgotten.card, Rating.Again, forgotten.card.dueAt)
        assertEquals(1, repeatedFailure.card.lapseCount)
        val recovered = fsrs.review(repeatedFailure.card, Rating.Good, repeatedFailure.card.dueAt)
        assertEquals(CardPhase.Review, recovered.card.phase)
        assertEquals(5, recovered.card.reviewCount)
    }

    @Test
    fun sameDaySuccessfulRatingsNeverReduceStability() {
        val fsrs = FSRS6(enableFuzz = false)
        for (s in listOf(0.001, 1.0, 100000.0)) {
            for (rating in listOf(Rating.Hard, Rating.Good, Rating.Easy)) {
                val next = fsrs.review(reviewed(s), rating, start + day - 1)
                assertTrue(requireNotNull(next.card.memory).stability >= s)
                assertEquals(0L, next.log.elapsedDays)
            }
        }
    }

    @Test
    fun fuzzIsStableAndOrderedWithinLimits() {
        for (limit in listOf(1, 2, 3, 10, 36500)) {
            for (seed in 0L..50L) {
                val fsrs = FSRS6(seed = seed, maximumInterval = limit)
                val card = reviewed().copy(id = "card-$seed")
                val choices = fsrs.calculate(card, start + 30 * day)
                assertEquals(choices, fsrs.calculate(card, start + 30 * day))
                assertEquals(choices, FSRS6(seed = seed, maximumInterval = limit).calculate(card, start + 30 * day))
                val intervals = choices.filter { it.card.phase == CardPhase.Review }.map { it.interval }
                assertTrue(intervals.all { it in 1..limit })
                assertTrue(intervals.zipWithNext().all { (a, b) -> a <= b })
                choices.forEach { assertEquals(it, fsrs.review(card, it.choice, start + 30 * day)) }
            }
        }
    }

    @Test
    fun overdueFuzzDoesNotForceAnIntervalLongerThanMemorySupports() {
        val fsrs = FSRS6(learningSteps = emptyList(), relearningSteps = emptyList())
        val proposal = fsrs.review(reviewed(0.001), Rating.Hard, start + 1000 * day)
        assertTrue(proposal.interval < 1000)
    }

    @Test
    fun parameterAndStepInputsAreCopied() {
        val weights = FSRS6.DEFAULT_PARAMETERS
        val steps = mutableListOf(60_000L, 600_000L)
        val fsrs = FSRS6(parameters = weights, learningSteps = steps, enableFuzz = false)
        val before = fsrs.calculate(FlashCard("card"), start)
        weights[0] = Double.NaN
        steps.clear()
        FSRS6.DEFAULT_PARAMETERS[0] = 99.0
        assertEquals(before, fsrs.calculate(FlashCard("card"), start))
        assertEquals(0.212, FSRS6.DEFAULT_PARAMETERS[0], 0.0)
    }

    @Test
    fun rejectsInvalidConfigurationAndPartialState() {
        for (retention in listOf(0.0, 1.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY)) {
            assertThrows(IllegalArgumentException::class.java) { FSRS6(requestRetention = retention) }
        }
        for (limit in listOf(0, -1, 36501)) {
            assertThrows(IllegalArgumentException::class.java) { FSRS6(maximumInterval = limit) }
        }
        assertThrows(IllegalArgumentException::class.java) { FSRS6(parameters = DoubleArray(34)) }
        for (value in listOf(Double.NaN, Double.POSITIVE_INFINITY, -1.0)) {
            val weights = FSRS6.DEFAULT_PARAMETERS.also { it[20] = value }
            assertThrows(IllegalArgumentException::class.java) { FSRS6(parameters = weights) }
            assertThrows(IllegalArgumentException::class.java) { MemoryState(value, 5.0) }
        }
        assertThrows(IllegalArgumentException::class.java) { FSRS6(learningSteps = listOf(0)) }
        assertThrows(IllegalArgumentException::class.java) { FSRS6(learningSteps = listOf(day)) }
        assertThrows(IllegalArgumentException::class.java) { FSRS6(learningSteps = listOf(600000, 60000)) }
        assertThrows(IllegalArgumentException::class.java) { FlashCard("card", memory = MemoryState(1.0, 5.0)) }
        assertThrows(IllegalArgumentException::class.java) { reviewed().copy(phase = CardPhase.Learning) }
        assertThrows(IllegalArgumentException::class.java) { MemoryState(1.0, Double.NaN) }
    }

    @Test
    fun rejectsBackwardsClockAndTimestampOverflow() {
        val fsrs = FSRS6()
        assertThrows(IllegalArgumentException::class.java) { fsrs.review(reviewed(), Rating.Good, start - 1) }
        assertThrows(ArithmeticException::class.java) { fsrs.review(FlashCard("card"), Rating.Easy, Long.MAX_VALUE) }
        assertThrows(IllegalArgumentException::class.java) { fsrs.calculate(FlashCard("card"), -1) }
    }

    @Test
    fun replayRebuildsFromRatingsAndTimes() {
        val fsrs = FSRS6()
        var card = FlashCard("card")
        val logs = mutableListOf<ReviewLog>()
        for ((time, rating) in listOf(
            start to Rating.Good,
            start + 600000 to Rating.Good,
            start + 7 * day to Rating.Again,
            start + 7 * day + 600000 to Rating.Good,
            start + 30 * day to Rating.Easy
        )) {
            val proposal = fsrs.review(card, rating, time)
            logs += proposal.log
            card = proposal.card
        }
        assertEquals(card, fsrs.replay("card", logs.reversed()))
        assertEquals(FlashCard("new"), fsrs.replay("new", emptyList()))
        assertThrows(IllegalArgumentException::class.java) { fsrs.replay("other", logs) }
        val changed = FSRS6(
            parameters = FSRS6.DEFAULT_PARAMETERS.also {
                it[0] = 0.5
                it[2] = 3.0
            }
        )
        assertNotEquals(card.memory, changed.replay("card", logs).memory)
    }

    @Test
    fun storageMappingRoundTripsAndRejectsOtherVersions() {
        val proposal = FSRS6().review(FlashCard("entry"), Rating.Good, start)
        val entity = FsrsCardEntity.fromCard(proposal.card)
        assertEquals(proposal.card, entity.toCard())
        assertEquals(proposal.log, FsrsReviewEntity.fromLog(proposal.log).toLog())
        assertThrows(IllegalArgumentException::class.java) { entity.copy(algorithmVersion = 7).toCard() }
    }
}
