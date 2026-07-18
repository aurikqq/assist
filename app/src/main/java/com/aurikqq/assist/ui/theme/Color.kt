package com.aurikqq.assist.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)


data class Colors(
    val primary: Color,
    val secondary: Color,
    val background: Color,
    val surfaceLow: Color,
    val surfaceHigh: Color,
    val red: Color,
    val secondaryRed: Color,
)

val NothingLightScheme = Colors(
    background = Color(0xFFf2f2f2),
    primary = Color(0xFF1c1c1c),
    secondary = Color(0xFF777777),
    surfaceHigh = Color(0xFFf2f2f2),
    surfaceLow = Color(0xFFe1e1e1),
    red = Color(0xFFfe223f),
    secondaryRed = Color(0xFFec232b)
)

val NothingDarkScheme = Colors(
    background = Color(0xFF000000),
    primary = Color(0xFFF2F2F2),
    secondary = Color(0xFF929292),
    surfaceHigh = Color(0xFF292929),
    surfaceLow = Color(0xFF181818),
    red = Color(0xFFfe223f),
    secondaryRed = Color(0xFFec232b)
)

val NothingColors = staticCompositionLocalOf { NothingLightScheme }