package com.theinkreaper.aceofdecks.domain

import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.roundToInt

enum class UserGrade(val value: Int) {
    AGAIN(1),
    HARD(2),
    GOOD(3),
    EASY(4)
}

data class SrsResult(
    val nextReview: Long,
    val interval: Int,
    val easeFactor: Float
)

class SrsAlgorithm {

    fun calculateNextReview(
        currentInterval: Int,
        currentEaseFactor: Float,
        grade: UserGrade
    ): SrsResult {

        var newInterval: Int
        var newEaseFactor = currentEaseFactor

        if (grade == UserGrade.AGAIN) {
            newInterval = 0

            newEaseFactor = max(1.3f, currentEaseFactor - 0.2f)
        } else {
            if (currentInterval == 0) {
                newInterval = 1
            } else if (currentInterval == 1) {
                newInterval = 6
            } else {
                newInterval = (currentInterval * currentEaseFactor).roundToInt()
            }

            val adjustedGrade = grade.value + 1
            newEaseFactor = currentEaseFactor + (0.1f - (5 - adjustedGrade) * (0.08f + (5 - adjustedGrade) * 0.02f))
        }

        newEaseFactor = max(1.3f, newEaseFactor)

        val nextReviewDate = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(newInterval.toLong())

        return SrsResult(
            nextReview = nextReviewDate,
            interval = newInterval,
            easeFactor = newEaseFactor
        )
    }
}