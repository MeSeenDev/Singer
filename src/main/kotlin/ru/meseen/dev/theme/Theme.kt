package ru.meseen.dev.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

val DarkColorPalette = darkColors(
    primary = greyFull,
    primaryVariant = greyMid,
    secondary = greyLight,
    background = greyDark
)
val LightColorPalette = lightColors(
    primary = greyFull,
    primaryVariant = greyMid,
    secondary = greyLight,
    background = greyDark
)

@Composable
fun CompostTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable() () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        content = content
    )
}