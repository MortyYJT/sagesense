package com.mortyyjt.sagesense.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.unit.sp
import org.junit.Assert.assertTrue
import org.junit.Test

class TypographyAccessibilityTest {
    @Test
    fun everyMaterialTextStyleIsAtLeastTwentyTwoSp() {
        val typography = SageTypography
        val sizes = listOf(
            typography.displayLarge,
            typography.displayMedium,
            typography.displaySmall,
            typography.headlineLarge,
            typography.headlineMedium,
            typography.headlineSmall,
            typography.titleLarge,
            typography.titleMedium,
            typography.titleSmall,
            typography.bodyLarge,
            typography.bodyMedium,
            typography.bodySmall,
            typography.labelLarge,
            typography.labelMedium,
            typography.labelSmall,
        ).map { it.fontSize }
        assertTrue("All text styles should be at least 22sp: $sizes", sizes.all { it >= 22.sp })
    }
}
