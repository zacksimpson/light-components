package com.thelightphone.components.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.thelightphone.sdk.ui.LightBarButton
import com.thelightphone.sdk.ui.LightIcons
import com.thelightphone.sdk.ui.LightScrollView
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.LightThemeTokens
import com.thelightphone.sdk.ui.LightTopBar
import com.thelightphone.sdk.ui.LightTopBarCenter
import com.thelightphone.sdk.ui.gridUnitsAsDp
import com.thelightphone.sdk.ui.lightClickable

/**
 * Settings screen scaffold: top bar with centered title and optional back button,
 * scrollable body. Doesn't wrap LightTheme itself, wrap your screen's Content() in that first.
 * Pass onBack = null for a top-level tab (no back button).
 */
@Composable
fun LightSettingsScaffold(
    title: String,
    onBack: (() -> Unit)?,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightThemeTokens.colors.background),
    ) {
        LightTopBar(
            leftButton = onBack?.let { LightBarButton.LightIcon(LightIcons.BACK, onClick = it) },
            center = LightTopBarCenter.Text(title),
            rightButton = null,
            modifier = Modifier.padding(bottom = 1f.gridUnitsAsDp()),
        )

        // Horizontal padding goes on the content column, not on LightScrollView's own
        // modifier, padding the scroll view itself pulls its scrollbar in with no gutter.
        LightScrollView(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.padding(horizontal = 1f.gridUnitsAsDp())) {
                content()
            }
        }
    }
}

@Composable
fun SettingsLinkRow(label: String, onClick: () -> Unit, description: String? = null) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .lightClickable(onClick = onClick)
            .padding(vertical = 0.75f.gridUnitsAsDp()),
    ) {
        LightText(text = label, variant = LightTextVariant.Heading)
        if (description != null) {
            LightText(
                text = description,
                variant = LightTextVariant.Detail,
                modifier = Modifier.padding(top = 0.15f.gridUnitsAsDp()),
            )
        }
    }
}

/** Label on top in Detail, current value below in Heading. For "tap to change this
 *  setting" rows, e.g. label "Units", value "Fahrenheit". Opposite size pairing from
 *  SettingsLinkRow on purpose, that one shows a destination, this one shows a live value. */
@Composable
fun SettingsValueRow(label: String, value: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .lightClickable(onClick = onClick)
            .padding(vertical = 0.75f.gridUnitsAsDp()),
    ) {
        LightText(text = label, variant = LightTextVariant.Detail)
        LightText(text = value, variant = LightTextVariant.Heading)
    }
}
