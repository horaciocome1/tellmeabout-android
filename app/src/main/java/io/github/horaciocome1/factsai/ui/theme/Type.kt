package io.github.horaciocome1.factsai.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.github.horaciocome1.factsai.R

// val openSansFamily = FontFamily(
//     Font(R.font.open_sans_light, FontWeight.Light),
//     Font(R.font.open_sans, FontWeight.Normal),
//     Font(R.font.open_sans_semibold, FontWeight.Medium),
//     Font(R.font.open_sans_semibold, FontWeight.SemiBold),
//     Font(R.font.open_sans_bold, FontWeight.Bold),
// )

val montserratFamily = FontFamily(
    Font(R.font.montserrat_light, FontWeight.Light),
    Font(R.font.montserrat, FontWeight.Normal),
    Font(R.font.montserrat_medium, FontWeight.Medium),
    Font(R.font.montserrat_semibold, FontWeight.SemiBold),
    Font(R.font.montserrat_bold, FontWeight.Bold),
)

// Set of Material typography styles to start with
val Typography = Typography(
    labelLarge = TextStyle(
        fontFamily = montserratFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = montserratFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = montserratFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.7.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = montserratFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 40.sp, // 36.sp
        letterSpacing = 0.6.sp,
    ),
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)
