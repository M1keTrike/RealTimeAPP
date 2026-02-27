package com.duelmath.features.questions.data.datasources.remote.mapper

import com.duelmath.features.questions.data.datasources.remote.model.QuestionOptionResponse
import com.duelmath.features.questions.data.datasources.remote.model.QuestionResponse
import com.duelmath.features.questions.domain.entities.Question
import com.duelmath.features.questions.domain.entities.QuestionOption

fun QuestionResponse.toDomain(): Question {
    return Question(
        id = id,
        statement = statement,
        difficulty = difficulty,
        options = options.map { it.toDomain() },
        correctOptionId = correctOptionId,
    )
}

fun QuestionOptionResponse.toDomain(): QuestionOption {
    return QuestionOption(
        id = id,
        text = text,
    )
}