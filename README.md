# Light Components

A set of building blocks for tools built with the [Light SDK](https://github.com/lightphone/light-sdk). Designed to be as faithful to the original LightOS screens as possible, so they drop straight into your own tool's dev environment.

## The goal

New tools built on the Light SDK are showing up almost daily. They are all tremendously inspiring, and its community has been unlike any other experience I've had with software! A lot of that work often involves rebuilding the same handful of screens and logic that already exist in LightOS. This can be very general (eg. a settings screen) or category-specific (eg. media players or calendars).

That made me wonder: _instead of everyone building their own slightly-off version of the same components (myself included), what if we just built these together, once?_

**The goal here is community-built components that hit visual and functional parity with LightOS as closely as possible, so anyone building on the official SDK can skip the step of building them from scratch.**

---

### Benefits
* Time saved for every type of developer
* SDK-built tools that feel as close to native as possible
* Developing by hand? Less manual work to replicate pieces that aren't currently provided by the SDK
* Using LLMs to code? Cuts down on design drift (eg. unnecessary dividers, gray text, Android-like buttons, etc)


Either way, the hope is a community-backed design system that takes that burden off the official SDK to provide these while it's still growing.

---

## About Components

All components belong in one of two categories: 
* `components/stock/` contains best-effort attempts at replicating **existing aspects of LightOS** as closely as possible.
* `components/original/` contains **new ideas from the community** that don't exist in LightOS. 

Both are welcome! See [Contributing a component](#contributing-a-component) below.

**Stock**

| Component | Description |
| :--- | :--- |
| Date picker | Full-screen calendar month picker |
| Time picker | Numpad time entry, 12h and 24h |
| Settings screen | Scaffold plus link, toggle, and value rows (to be built out with each "style" of setting we see in LightOS) |
| Calculator | Numpad and function buttons for performing basic calculations |

**Original**

(Nothing here yet, be the first!)

### Planned

| Component | Description |
| :--- | :--- |
| Conversations thread screen | For messaging tools, with sent / received, contact details, message composer |
| Now Playing screen | Audio players, with play / pause / skip / scrubbing |

---

## Contributing a component

1. Pick a category: `components/stock/` if it recreates an existing LightOS screen, `components/original/` if it's your own design. To match the shape, copy an existing component's folder as a starting point (`components/stock/date-picker/` is a solid one). Each one is a `build.gradle.kts` (namespace under `com.thelightphone.components.<name>`), a `README.md` (what it does, `Depends on`, `Pasting this in`), and the screen itself under `src/main/kotlin/com/thelightphone/components/<name>/`.
2. Strip anything specific to your own tool, its own persistence, navigation to its other screens, etc.
3. Make sure your component builds against the official Light SDK (see below for instructions). 
4. Open a ["Submit a component"](https://github.com/zacksimpson/light-components/issues/new?template=submit-component.yml) issue with a link to your code, mainly so we can talk through fit before you submit a PR. 

---

## Using a component
> [!NOTE]
> **These components are simply copy-paste for now, rather than a dependency or something else.** Grab the component you need from `components/`, paste it into your own tool, and rename the package.
<details>
<summary><strong>Checking a component builds first</strong></summary>

1. Read that component's own README for what it depends on and what to rename.
2. Copy its `src` folder into your tool.
3. Update the `package` declaration in each copied file.

Each component is a real Gradle module, but it only compiles alongside an actual `light-sdk` checkout. Clone this repo next to your `light-sdk` clone, then wire the component into your local, uncommitted `light-sdk/settings.gradle.kts`:

```kotlin
include(":date-picker")
project(":date-picker").projectDir = file("../light-components/components/stock/date-picker")
```

</details>

---

### A disclaimer

I'm not an authority on what "LightOS-faithful" means beyond my own best guesses. (Honestly I'm just screenshotting LightOS and refining from there most of the time!)  I hope creating a shared set of community-made "components" is something that can benefit everyone, and provide a shared set of useful tools that have visual consistency with LightOS. 

---

### Support & feedback

If you have ideas on how this project as a whole could improve, I'd _seriously_ appreciate the help. Feel free to [email me](mailto:zacksimpson24@gmail.com) or open an issue with feedback anytime. And if any of my tools have been useful to you, I'd love to hear from you! 

---

### Credits

* [The Light Phone](https://www.thelightphone.com) – for building a phone worth building tools for
