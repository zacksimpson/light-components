package com.thelightphone.components.lighttheme

import android.graphics.fonts.SystemFonts
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

/** LightOS phones ship with Akkurat; use it so the app matches the system UI. */
fun akkuratFamilyOrDefault(): FontFamily {
    return runCatching {
        val fonts = SystemFonts.getAvailableFonts()
            .filter { it.file?.name?.startsWith("Akkurat", ignoreCase = true) == true }
            .mapNotNull { f ->
                val file = f.file ?: return@mapNotNull null
                val style = if (f.style.slant != 0) FontStyle.Italic else FontStyle.Normal
                Font(file = file, weight = FontWeight(f.style.weight), style = style)
            }
        if (fonts.isNotEmpty()) FontFamily(fonts) else FontFamily.Default
    }.getOrDefault(FontFamily.Default)
}
