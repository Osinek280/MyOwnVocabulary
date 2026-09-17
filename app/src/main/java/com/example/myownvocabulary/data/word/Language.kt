package com.example.myownvocabulary.data.word

enum class Language(val code: String, val displayName: String, val flagEmoji: String) {
    English("en", "Angielski", "🇬🇧"),
    German("de", "Niemiecki", "🇩🇪"),
    Spanish("es", "Hiszpański", "🇪🇸"),
    French("fr", "Francuski", "🇫🇷"),
    Italian("it", "Włoski", "🇮🇹"),
    Ukrainian("uk", "Ukraiński", "🇺🇦"),
    Polish("pl", "Polski", "🇵🇱"),
    Portuguese("pt", "Portugalski", "🇵🇹"),
    Dutch("nl", "Niderlandzki", "🇳🇱"),
    Swedish("sv", "Szwedzki", "🇸🇪"),
    Norwegian("no", "Norweski", "🇳🇴"),
    Danish("da", "Duński", "🇩🇰"),
    Finnish("fi", "Fiński", "🇫🇮"),
    Czech("cs", "Czeski", "🇨🇿"),
    Slovak("sk", "Słowacki", "🇸🇰"),
    Hungarian("hu", "Węgierski", "🇭🇺"),
    Romanian("ro", "Rumuński", "🇷🇴"),
    Bulgarian("bg", "Bułgarski", "🇧🇬"),
    Croatian("hr", "Chorwacki", "🇭🇷"),
    Serbian("sr", "Serbski", "🇷🇸"),
    Slovenian("sl", "Słoweński", "🇸🇮"),
    Greek("el", "Grecki", "🇬🇷"),
    Turkish("tr", "Turecki", "🇹🇷"),
    Russian("ru", "Rosyjski", "🇷🇺"),
    Japanese("ja", "Japoński", "🇯🇵"),
    Korean("ko", "Koreański", "🇰🇷"),
    Chinese("zh", "Chiński", "🇨🇳"),
    Arabic("ar", "Arabski", "🇸🇦"),
    Hebrew("he", "Hebrajski", "🇮🇱"),
    Hindi("hi", "Hindi", "🇮🇳"),
    Vietnamese("vi", "Wietnamski", "🇻🇳"),
    Thai("th", "Tajski", "🇹🇭"),
    Indonesian("id", "Indonezyjski", "🇮🇩"),
    Malay("ms", "Malajski", "🇲🇾"),
    Latin("la", "Łacina", "🏛");

    companion object {
        fun fromCode(code: String): Language = entries.find { it.code.equals(code, ignoreCase = true) } ?: English
    }
}
