package io.github.horaciocome1.factsai.util

enum class AnalyticsEvent {
    AppOpened,
    AppClosed,
    AppBackgrounded,
    AppForegrounded,

    UserSignInAttempted,
    UserSignInSucceeded,
    UserSignInFailed,

    GenerateFactsAttempted,
    GenerateFactsAttemptedWithNewTopic,
    GenerateFactsSucceeded,
    GenerateFactsFailed,

    FactRead,
}
