# Light Components

A set of building blocks for tools built with the [Light SDK](https://github.com/lightphone/light-sdk). Designed to be as faithful to the original LightOS screens as possible, so they drop straight into your own tool's dev environment.

## The goal

New tools built on the Light SDK are showing up almost daily. They are all tremendously inspiring, and its community has been unlike any other experience I've had with software! A lot of that work often involves rebuilding the same handful of screens and logic that already exist in LightOS. This can be very general (eg. a settings screen) or category-specific (eg. media players or calendars).

That made me wonder: instead of everyone building their own slightly-off version of the same components (myself included), what if we just built these together, once?

**The goal here is community-built components that hit visual and functional parity with LightOS as closely as possible, so anyone building on the official SDK can skip the step of building them from scratch.**

## Benefits
* Time saved for every type of developer
* SDK-built tools that feel as close to native as possible
* Lower barrier to entry 
* Developing by hand? Hopefully this means less manual work to replicate pieces that aren't currently provided in the SDK
* Using LLMs to code? Cuts down on design drift (eg. unnecessary dividers, gray text, Android-like buttons, etc)


Either way, the hope is a community-backed design system that takes that burden off the official SDK to provide these while it's still growing.

## A disclaimer

This isn't meant to discourage anyone from designing something new. Really, it's the opposite. I hope creating a shared set of community-made "components" is something everyone can benefit from!

I'm also not an authority on what "LightOS-faithful" means beyond my own best guesses. (Honestly I'm just screenshotting LightOS and refining from there most of the time!) If you have ideas on how this project as a whole could improve, I'd _seriously_ appreciate the help. Open an issue or a PR anytime!

## Contributing a component

Already built something? Open a ["Submit a component"](https://github.com/zacksimpson/light-components/issues/new?template=submit-component.yml) issue with a link to your code, mainly so we can talk through fit before you put in the work to match the shape below. Not required, a PR works too if you're already there.

To match the shape, copy an existing component's folder as a starting point, `components/date-picker/` is a solid one. Each one is a `build.gradle.kts` (namespace under `com.thelightphone.components.<name>`), a `README.md` (what it does, `Depends on`, `Pasting this in`), and the screen itself under `src/main/kotlin/com/thelightphone/components/<name>/`. Strip anything specific to your own tool, its own persistence, navigation to its other screens, that kind of coupling shouldn't leak in. Add a line to "What's here" below, then open the PR (referencing the issue number if there is one).

---

> [!NOTE]
> **These components are simply copy-paste for now, rather than a dependency or something else.** Grab the component you need from `components/`, paste it into your own tool, and rename the package. Nothing here is versioned or published, so once you copy something, updates here don't reach you automatically. (Always open to changing this if you have a suggestion!)

### What's here / in progress

* Date picker: full-screen calendar month picker 
* Time picker: numpad time entry, 12h and 24h
* Settings screen: scaffold plus link, toggle, and value rows (to be built out with each "style" of setting we see in LightOS)

### Planned
* Conversations thread screen (messaging tools)
* Now Playing screen (audio players)

---

## Using a component

1. Read that component's own README for what it depends on and what to rename.
2. Copy its `src` folder into your tool.
3. Update the `package` declaration in each copied file.

<details>
<summary><strong>Checking a component builds first</strong></summary>

Each component is a real Gradle module, but it only compiles alongside an actual `light-sdk` checkout. Clone this repo next to your `light-sdk` clone, then wire the component into your local, uncommitted `light-sdk/settings.gradle.kts`:

```kotlin
include(":date-picker")
project(":date-picker").projectDir = file("../light-components/components/date-picker")
```

</details>

---

## Support

If any of my tools have been useful to you, I'd love to hear from you! Feel free to [email me](mailto:zacksimpson24@gmail.com), or open an issue with feedback.

---

## Credits

* [The Light Phone](https://www.thelightphone.com) – for building a phone worth building tools for
