package com.example.ringrush.data

enum class ThemeMode(val title: String) {
    SYSTEM("System"),
    DARK("Dark"),
    LIGHT("Light");

    companion object {
        fun fromOrdinal(ordinal: Int): ThemeMode =
            values().getOrElse(ordinal) { DARK }

        fun fromName(name: String): ThemeMode =
            try {
                valueOf(name.uppercase())
            } catch (e: Exception) {
                DARK
            }
    }
}
