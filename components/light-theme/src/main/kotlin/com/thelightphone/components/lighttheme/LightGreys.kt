package com.thelightphone.components.lighttheme

import androidx.compose.ui.graphics.Color

/**
 * The greys, which are the whole palette.
 *
 * LightOS has three colours — background, content, and one step down from content — and no
 * accents, no dividers in any other shade, no semantic red or green. These are the values nine
 * of the apps had independently arrived at and written down identically; they are here so the
 * tenth stops guessing.
 *
 * Deliberately not a `MaterialTheme` extension or a CompositionLocal. An app that wants
 * something else should use something else, and several do — the glance widget runs a lighter
 * [Dim] because its whole surface is dimmer. Constants are easier to disagree with than a theme.
 */

/** Secondary text: labels, captions, the sub-line of a list row. */
val Dim = Color(0xFF9A9A9A)

/** Tertiary: placeholder text, disabled states, anything you should be able to ignore. */
val Faint = Color(0xFF5E5E5E)

/** Rules and separators. Nearly invisible on the panel, which is the point. */
val RuleGrey = Color(0xFF262626)
