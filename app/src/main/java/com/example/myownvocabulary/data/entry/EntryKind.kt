package com.example.myownvocabulary.data.entry

enum class EntryKind(val singularLabel: String, val pluralLabel: String, val icon: String) {
    Word(
        "Słowo",
        "Słowa",
        "\uD83D\uDCDD" // 📝
    ),
    Expression(
        "Wyrażenie",
        "Wyrażenia",
        "\uD83D\uDCAC" // 💬
    ),
    Idiom(
        "Idiom",
        "Idiomy",
        "\uD83E\uDDE9" // 🧩
    ),
    Sentence(
        "Zdanie",
        "Zdania",
        "\uD83D\uDCD6 " // 📖
    ),
    Numeral(
        "Liczebnik",
        "Liczebniki",
        "\uD83D\uDD22" // \uD83E\uDDEE - 🧮 or 🔢
    )
}
