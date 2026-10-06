package com.example.myownvocabulary

import com.example.myownvocabulary.domain.quiz.QuizAnswerMatcher
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuizAnswerMatcherTest {
    @Test
    fun ignoresCaseAndSentencePunctuation() {
        assertTrue(QuizAnswerMatcher.matches("the cat is on the mat", "The cat is on the mat."))
        assertTrue(QuizAnswerMatcher.matches("THE CAT IS ON THE MAT!", "The cat is on the mat."))
        assertTrue(QuizAnswerMatcher.matches("Hello world", "“Hello, world!”"))
    }

    @Test
    fun ignoresExtraWhitespace() {
        assertTrue(QuizAnswerMatcher.matches("  The cat  is\non the mat ", "The cat is on the mat."))
    }

    @Test
    fun rejectsDifferentWordsAndEmptyAnswers() {
        assertFalse(QuizAnswerMatcher.matches("The dog is on the mat", "The cat is on the mat."))
        assertFalse(QuizAnswerMatcher.matches("...", "!"))
        assertFalse(QuizAnswerMatcher.matches("", "The cat is on the mat."))
    }
}
