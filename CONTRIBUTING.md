# Contributing a component

1. Pick a category: `components/stock/` if it recreates an existing LightOS screen, `components/original/` if it's your own design (see the README's [About Components](README.md#about-components) for what each means). To match the shape, copy an existing component's folder as a starting point (`components/stock/date-picker/` is a solid one). Each one is a `build.gradle.kts` (namespace under `com.thelightphone.components.<name>`), a `README.md` (what it does, `Depends on`, `Pasting this in`), and the screen itself under `src/main/kotlin/com/thelightphone/components/<name>/`.
2. Strip anything specific to your own tool, its own persistence, navigation to its other screens, etc.
3. Make sure your component builds against the official Light SDK, see the README's ["Checking a component builds first"](README.md#using-a-component) for the local wiring steps.
4. Open a ["Submit a component"](https://github.com/zacksimpson/light-components/issues/new?template=submit-component.yml) issue with a link to your code, mainly so we can talk through fit before you submit a PR.
