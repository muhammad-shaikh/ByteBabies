package com.bytebabies.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val ByteBabiesColorScheme = lightColorScheme(
    primary = BB_Purple,
    onPrimary = BB_OnPrimary,
    primaryContainer = BB_Blue,
    onPrimaryContainer = BB_OnSurface,

    secondary = BB_Pink,
    onSecondary = BB_OnPrimary,
    secondaryContainer = BB_Yellow,
    onSecondaryContainer = BB_OnSurface,

    tertiary = BB_Teal,
    onTertiary = BB_OnPrimary,
    background = BB_Background,
    surface = BB_Surface,
    onSurface = BB_OnSurface,
    outline = BB_Outline
)

private val ByteBabiesShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun ByteBabiesTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ByteBabiesColorScheme,
        shapes = ByteBabiesShapes,
        typography = MaterialTheme.typography,
        content = content
    )
}
