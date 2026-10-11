package com.example.myownvocabulary.model.fsrs

import kotlin.math.exp
import kotlin.math.expm1
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round

/**
 * FSRS-6 equations and parameter bounds, verified against py-fsrs v6.3.2.
 * https://github.com/open-spaced-repetition/py-fsrs/blob/v6.3.2/fsrs/scheduler.py
 * Elapsed days are completed 24-hour periods in UTC, including overdue reviews.
 * Learning steps and deterministic fuzz are scheduler policies, not memory-model parameters.
 */
class FSRS6(
    private val requestRetention: Double = 0.90,
    private val maximumInterval: Int = 36500,
    private val enableFuzz: Boolean = true,
    private val seed: Long = 0,
    parameters: DoubleArray = DEFAULT_PARAMETERS,
    learningSteps: List<Long> = listOf(60_000L, 600_000L),
    relearningSteps: List<Long> = listOf(600_000L)
) {
    private val w = parameters.copyOf()
    private val learningSteps = learningSteps.toList()
    private val relearningSteps = relearningSteps.toList()
    private val decay: Double
    private val factor: Double

    init {
        require(requestRetention.isFinite() && requestRetention > 0 && requestRetention < 1)
        require(maximumInterval in 1..36500) { "Maximum interval must be between 1 and 36500 days" }
        require(w.size == 21) { "FSRS-6 requires exactly 21 parameters" }
        w.forEachIndexed { index, value ->
            require(value.isFinite() && value in LOWER_BOUNDS[index]..UPPER_BOUNDS[index]) {
                "FSRS parameter $index is out of bounds"
            }
        }
        validateSteps(this.learningSteps)
        validateSteps(this.relearningSteps)
        decay = -w[20]
        factor = expm1(ln(0.9) / decay)
    }

    companion object {
        const val ALGORITHM_VERSION = 6
        const val DAY_MILLIS = 86_400_000L

        // Return a fresh array; callers cannot alter defaults used by another scheduler.
        val DEFAULT_PARAMETERS: DoubleArray
            get() = doubleArrayOf(
                0.212, 1.2931, 2.3065, 8.2956, 6.4133, 0.8334, 3.0194,
                0.001, 1.8722, 0.1666, 0.796, 1.4835, 0.0614, 0.2629,
                1.6483, 0.6014, 1.8729, 0.5425, 0.0912, 0.0658, 0.1542
            )

        private val LOWER_BOUNDS = doubleArrayOf(
            0.001, 0.001, 0.001, 0.001, 1.0, 0.001, 0.001, 0.001,
            0.0, 0.0, 0.001, 0.001, 0.001, 0.001, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.1
        )
        private val UPPER_BOUNDS = doubleArrayOf(
            100.0, 100.0, 100.0, 100.0, 10.0, 4.0, 4.0, 0.75,
            4.5, 0.8, 3.5, 5.0, 0.25, 0.9, 4.0, 1.0, 6.0, 2.0, 2.0, 0.8, 0.8
        )

        private fun validateSteps(steps: List<Long>) {
            require(steps.all { it in 1 until DAY_MILLIS }) { "Steps must be positive and shorter than a day" }
            require(steps.zipWithNext().all { (a, b) -> a < b }) { "Steps must increase" }
        }
    }

    fun retrievability(card: FlashCard, now: Long = System.currentTimeMillis()): Double {
        val elapsed = elapsedDays(card, now)
        return card.memory?.let { forgettingCurve(elapsed.toDouble(), it.stability) } ?: 0.0
    }

    /** Unrounded mathematical interval; rounding, day limits and fuzz are separate policies. */
    fun intervalDays(stability: Double): Double {
        require(stability.isFinite() && stability >= 0.001)
        val result = stability / factor * expm1(ln(requestRetention) / decay)
        require(result.isFinite()) { "Interval overflow" }
        return result
    }

    fun forgettingCurve(elapsedDays: Double, stability: Double): Double {
        require(elapsedDays.isFinite() && elapsedDays >= 0)
        require(stability.isFinite() && stability >= 0.001)
        return (1.0 + factor * elapsedDays / stability).pow(decay)
    }

    /** Pure preview: identical card, time, configuration and seed yield identical proposals. */
    fun calculate(card: FlashCard, reviewedAt: Long = System.currentTimeMillis()): List<Grade> {
        val elapsed = elapsedDays(card, reviewedAt)
        val quantile = fuzzQuantile(card)
        var previousReviewInterval = 1
        return Rating.entries.map { rating ->
            val memory = nextMemory(card.memory, elapsed, rating)
            val schedule = schedule(card, rating)
            val interval = if (schedule.phase == CardPhase.Review) {
                fuzzInterval(intervalDays(memory.stability), quantile)
                    .coerceAtLeast(if (rating == Rating.Again) 1 else previousReviewInterval)
                    .also { previousReviewInterval = it }
            } else {
                0
            }
            val duration = if (interval > 0) interval * DAY_MILLIS else schedule.durationMillis
            val next = card.copy(
                memory = memory,
                dueAt = Math.addExact(reviewedAt, duration),
                lastReviewAt = reviewedAt,
                reviewCount = Math.incrementExact(card.reviewCount),
                lapseCount = if (card.phase == CardPhase.Review && rating == Rating.Again) {
                    Math.incrementExact(card.lapseCount)
                } else {
                    card.lapseCount
                },
                phase = schedule.phase,
                learningStep = schedule.step,
                interval = interval
            )
            Grade(rating, card, next, ReviewLog(card.id, rating, reviewedAt, elapsed), duration)
        }
    }

    fun review(card: FlashCard, rating: Rating, reviewedAt: Long = System.currentTimeMillis()): Grade =
        calculate(card, reviewedAt).single { it.choice == rating }

    /** Rebuild from actual ratings/times when switching parameters; never reinterpret v7 state as v6. */
    fun replay(cardId: String, history: List<ReviewLog>): FlashCard {
        require(history.all { it.cardId == cardId }) { "History belongs to another card" }
        return history.sortedBy { it.reviewedAt }.fold(FlashCard(cardId)) { card, log ->
            review(card, log.rating, log.reviewedAt).card
        }
    }

    private fun elapsedDays(card: FlashCard, now: Long): Long {
        require(now >= 0) { "Invalid review timestamp" }
        val last = card.lastReviewAt ?: return 0
        require(now >= last) { "Review precedes the previous review" }
        return (now - last) / DAY_MILLIS
    }

    private fun initialDifficulty(rating: Rating): Double = w[4] - exp(w[5] * (rating.value - 1)) + 1

    private fun nextMemory(previous: MemoryState?, elapsed: Long, rating: Rating): MemoryState {
        if (previous == null) {
            return MemoryState(w[rating.value - 1], initialDifficulty(rating).coerceIn(1.0, 10.0))
        }
        val s = previous.stability
        val d = previous.difficulty
        val stability = if (elapsed == 0L) {
            val growth = exp(w[17] * (rating.value - 3 + w[18])) * s.pow(-w[19])
            s * if (rating == Rating.Again) growth else max(1.0, growth)
        } else {
            val r = forgettingCurve(elapsed.toDouble(), s)
            if (rating == Rating.Again) {
                min(
                    w[11] * d.pow(-w[12]) * ((s + 1).pow(w[13]) - 1) * exp(w[14] * (1 - r)),
                    s / exp(w[17] * w[18])
                )
            } else {
                val hardPenalty = if (rating == Rating.Hard) w[15] else 1.0
                val easyBonus = if (rating == Rating.Easy) w[16] else 1.0
                s * (
                    1 + exp(w[8]) * (11 - d) * s.pow(-w[9]) * expm1(w[10] * (1 - r)) *
                        hardPenalty * easyBonus
                    )
            }
        }
        val delta = -w[6] * (rating.value - 3)
        val difficulty = w[7] * initialDifficulty(Rating.Easy) +
            (1 - w[7]) * (d + delta * (10 - d) / 9)
        return MemoryState(max(0.001, stability), difficulty.coerceIn(1.0, 10.0))
    }

    private data class Schedule(val phase: CardPhase, val step: Int? = null, val durationMillis: Long = 0)

    private fun schedule(card: FlashCard, rating: Rating): Schedule {
        if (card.phase == CardPhase.Review) {
            return if (rating == Rating.Again && relearningSteps.isNotEmpty()) {
                Schedule(CardPhase.ReLearning, 0, relearningSteps.first())
            } else {
                Schedule(CardPhase.Review)
            }
        }
        val phase = if (card.phase == CardPhase.ReLearning) CardPhase.ReLearning else CardPhase.Learning
        val steps = if (phase == CardPhase.ReLearning) relearningSteps else learningSteps
        val step = card.learningStep ?: 0
        // A configuration change may leave the old step beyond the new list.
        if (steps.isEmpty() || rating == Rating.Easy || (step >= steps.size && rating != Rating.Again)) {
            return Schedule(CardPhase.Review)
        }
        return when (rating) {
            Rating.Again -> Schedule(phase, 0, steps.first())
            Rating.Hard -> {
                val duration = when {
                    step > 0 -> steps[step]
                    steps.size == 1 -> steps.first() * 3 / 2
                    else -> (steps[0] + steps[1]) / 2
                }
                Schedule(phase, step, duration)
            }
            Rating.Good -> if (step + 1 >= steps.size) {
                Schedule(CardPhase.Review)
            } else {
                Schedule(phase, step + 1, steps[step + 1])
            }
            Rating.Easy -> Schedule(CardPhase.Review)
        }
    }

    private fun fuzzQuantile(card: FlashCard): Double {
        // FNV-1a over stable card identity/history. One quantile is shared by the four choices.
        var hash = seed xor -3750763034362895579L
        val key = "${card.id}:${card.reviewCount}:${card.lastReviewAt}"
        key.forEach { hash = (hash xor it.code.toLong()) * 1099511628211L }
        return java.util.Random(hash).nextDouble()
    }

    private fun fuzzInterval(raw: Double, quantile: Double): Int {
        val interval = round(raw.coerceIn(1.0, maximumInterval.toDouble())).toInt()
        if (!enableFuzz || interval < 3) return interval
        val days = interval.toDouble()
        val delta = 1 + 0.15 * max(0.0, min(days, 7.0) - 2.5) +
            0.10 * max(0.0, min(days, 20.0) - 7.0) + 0.05 * max(0.0, days - 20.0)
        val upper = round(days + delta).toInt().coerceAtMost(maximumInterval)
        val lower = round(days - delta).toInt().coerceIn(min(2, upper), upper)
        return lower + floor(quantile * (upper - lower + 1)).toInt()
    }
}
