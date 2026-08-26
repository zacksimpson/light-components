# Light Components

A set of shared LightOS-styled screens for other [Light SDK](https://github.com/lightphone/light-sdk) tools to borrow: a date picker, a time picker, a settings screen, more on the way.

---

> [!NOTE]
> **These components are simply copy-paste for now, rather than a dependency or something else.** Grab the component you need from `components/`, paste it into your own tool, and rename the package. Nothing here is versioned or published, so once you copy something, updates here don't reach you automatically. (Always open to changing this if you have a suggestion!)

## What's here

* Date picker: full-screen calendar month picker
* Time picker: numpad time entry, 12h and 24h
* Settings screen: scaffold plus link, toggle, and value rows

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
