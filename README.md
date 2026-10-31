<p align="center">
  <img src="Imagez/Banner.png" alt="banner">
</p>

# Plugin Categories 👌👌👌👌👌👌👌

i Just Waked up And Have No idea Why I Am Making This

A Just A Weird Weird Open Source Project

But Hey Look At The Good Parts You Might Liked it

...and it actually turned into a fully working plugin. **Plugin Categories** replaces the plain `/pl` output with a clickable, categorized chat overview of every plugin on your server — with real load status colors, automatic category detection from folders, and full Paper / Spigot / Bukkit / **Folia** support.

- **Plugin Name:** Plugin Categories
- **Author:** The Names Ali
- **Version:** 1.0.0 Beta
- **Main Command:** `/pl`
- **Default Permission:** `plugincategories.use` (configurable)

---

## What It Does

Normally `/pl` dumps one long line of plugin names. Plugin Categories turns it into a small interactive menu:

```
Plugins (12) (2)
- Administration
- Economy
- Minigames
- Utilities
```

- `12` (green) = plugins that are loaded and enabled
- `2` (red) = plugins that failed to load, are disabled, or are invalid/not recognized

Clicking a category opens its contents:

```
Economy
EconomyPlugin
ShopPlugin
OldEconomyPlugin
                        [Back]
```

- Green entries = loaded and enabled
- Red entries = failed / disabled / not loaded / invalid
- `[Back]` (bold red, centered) returns to the overview

If **no** valid category folders exist, the plugin simply lists all plugin names directly (green/red) instead of showing an empty category list.

## Requirements

| Requirement | Details |
| --- | --- |
| Server software | Bukkit, Spigot, Paper, Purpur and other forks, and Folia |
| Minecraft version | 1.8 through the latest release (compiled against the 1.8.8 API, no NMS code, no version-specific internals) |
| Java | Java 8 or newer (on Java 17+ servers everything works the same) |
| Folia | Folia 1.19.4 / 1.20+ supported through the real Folia scheduler API (`folia-supported: true`) |

The plugin detects at runtime what the server offers and adapts:

- **Chat rendering:** Adventure API components when the server ships them (Paper 1.16.5+, Folia), otherwise the BungeeCord chat API (Spigot/Bukkit 1.8+), otherwise plain legacy text. Click events and hover text work on every path that supports them.
- **Scheduling:** the Folia `GlobalRegionScheduler` / `AsyncScheduler` when present (Folia, Paper 1.20.1+), otherwise the classic Bukkit scheduler. All file scanning happens off the main/region threads.
- **Startup log** tells you exactly what was detected and chosen, e.g. `Server detected: Folia 1.20.4 ...`, `Chat renderer: Adventure API components`, `Scheduler: Folia compatible region schedulers`.

## Installation

1. Download `PluginCategories-1.0.0-Beta.jar` from the [Releases page](https://github.com/seyedaliahmadirouein/Plugin-Categories/releases).
2. Drop it into your server's `plugins` folder.
3. Restart the server.
4. Run `/pl` (as a player with the permission, by default an operator).

That's it. Category detection works out of the box with zero configuration.

## The Automatic Category Folder System

Every folder **directly inside** your `plugins` directory becomes a category, using the folder's name. No configuration needed:

```
plugins/
├── PluginCategories.jar
├── EssentialsX.jar
├── LuckPerms.jar
├── Administration/
│   ├── SomeAdminPlugin.jar
│   └── AnotherAdminPlugin.jar
├── Economy/
│   ├── EconomyPlugin.jar
│   └── ShopPlugin.jar
├── Minigames/
│   ├── BedWars.jar
│   └── SkyWars.jar
└── Utilities/
    └── UtilityPlugin.jar
```

Rules the plugin follows:

1. Every folder directly inside `plugins` is a potential category.
2. The folder name becomes the category name (original casing is preserved; names are matched case-insensitively when clicked).
3. A category appears only if it contains at least one `.jar` file.
4. Empty folders never appear.
5. Folders without JAR files never appear.
6. The `plugins` directory itself is never a category.
7. The plugin's own data folder (`plugins/PluginCategories`) and the Bukkit `update` folder are excluded automatically (more folders can be excluded via config).
8. Multiple categories are supported and sorted alphabetically.
9. A plugin is never displayed twice inside a category.
10. Categories are detected automatically on startup and rescanned in the background (configurable interval), plus on `/plcat reload`.

### Important: how green and red are decided

The plugin never assumes a JAR file is a working plugin. It matches every JAR against what the server actually loaded:

| State | Meaning | Color |
| --- | --- | --- |
| Loaded and enabled | The server loaded the plugin and it is running | Green |
| Loaded but disabled | The plugin is known but disabled (e.g. it errored while enabling) | Red |
| Not loaded | A valid plugin JAR (readable `plugin.yml`/`paper-plugin.yml`) that the server did not load | Red |
| Invalid | Not a readable JAR / no plugin metadata — shown with its file name | Red |

Matching is done by file path first, then by plugin name, so a copied JAR inside a category folder still shows green if that plugin is actually running from another location.

**Note about vanilla servers:** Bukkit, Spigot, Paper and Folia only *load* plugin JARs that sit directly in the `plugins` root — JARs inside subfolders are not started by the server. Those JARs will therefore correctly show up red ("not loaded"). To have a plugin actually run *and* appear in a category, keep it loaded from the root (or via a symlink/copy) — the plugin's name-matching will then show it green inside the category. Plugin Categories deliberately never loads, unloads or moves JARs itself.

## Commands

| Command | Description |
| --- | --- |
| `/pl` or `/plugins` | Shows the categorized overview (replaces the vanilla output **only** for players with the permission; everyone else gets normal vanilla behavior) |
| `/pl view <category>` | Opens a category (this is what the clickable names run) |
| `/pl back` | Returns to the overview (this is what `[Back]` runs) |
| `/pl reload` | Reloads the config and rescans (needs the admin permission) |
| `/plugincategories` (`/plcat`) | Same overview, also works from console |
| `/plugincategories view <category>` | Opens a category from console |
| `/plugincategories reload` | Reloads the config and rescans |

Tab completion is provided for `/plugincategories`.

## Permissions

| Permission | Default | Description |
| --- | --- | --- |
| `plugincategories.use` | op | Access to the categorized `/pl` interface. Fully configurable in `config.yml` |
| `plugincategories.admin` | op | Reload the configuration (`/pl reload`, `/plcat reload`) |

Security details:

- The permission is re-checked on **every** interaction, including every chat click (clicks run `/pl view ...` / `/pl back`, which are permission-checked again).
- Clickable elements can only ever run these two internal subcommands — they cannot execute arbitrary commands.
- Players without the permission keep the vanilla `/pl` behavior (or whatever another plugin such as a plugin hider makes of it).

## Configuration (`config.yml`)

```yaml
permission: plugincategories.use
admin-permission: plugincategories.admin

category-detection: true
hover-messages: true
plugin-hover: false

commands:
- pl
- plugins

navigation-command: pl

excluded-folders:
- update

rescan-interval-seconds: 30
debug: false

colors:
  success: "&a"
  failed: "&c"
  category: "&f"
  header: "&f"
  back: "&c"
  back-bold: true

count-format: "Plugins (%success%) (%failed%)"
category-prefix: "- "
category-hover: "&7Click to view &f%category%"
back-hover: "&7Click to go back"
back-button-padding: 24

messages:
  scanning: "&7Plugin data is still loading, please try again in a moment."
  unknown-category: "&cUnknown category: &f%category%"
  no-permission: "&cYou do not have permission to use this command."
  reloaded: "&aPlugin Categories configuration reloaded and a rescan was started."
```

| Option | Meaning |
| --- | --- |
| `permission` | Permission required to use the interface (default `plugincategories.use`) |
| `admin-permission` | Permission required for reload |
| `category-detection` | Enables/disables the folder category system (when off, `/pl` lists all plugins directly) |
| `hover-messages` | Enables/disables hover text on clickable elements |
| `plugin-hover` | When true, plugin entries show their state and file path on hover |
| `commands` | Which command roots the plugin intercepts (default `pl`, `plugins`) |
| `navigation-command` | Which command the clickable elements use (`pl` keeps everything on one command; switch to `plugincategories` if your setup prefers that) |
| `excluded-folders` | Folders inside `plugins` that are never treated as categories |
| `rescan-interval-seconds` | Minimum age before a background rescan is triggered again |
| `colors.*` | Colors for success, failed, category, header and back button. Accepts `&x` codes, color names like `GREEN`, or hex like `#55FF55` (hex uses true colors where supported, nearest legacy color otherwise) |
| `count-format` | Format of the count header; `%success%` and `%failed%` placeholders are colored automatically. May contain legacy color codes |
| `category-prefix` | Prefix in front of category names |
| `category-hover` / `back-hover` | Hover texts (`%category%` placeholder supported) |
| `back-button-padding` | Leading spaces used to center the `[Back]` button |
| `messages.*` | All plugin messages |

## Compatibility

### Server software

- **Paper / Purpur / Pufferfish and other Paper forks:** full support, Adventure components used automatically.
- **Spigot:** full support via the BungeeCord chat API (bundled in Spigot since 1.8).
- **Bukkit:** full support, same as Spigot.
- **Folia:** full support. `folia-supported: true` is declared, and all scheduling goes through Folia's `GlobalRegionScheduler` / `AsyncScheduler` (never through the Bukkit scheduler, which Folia removed). File scanning runs async, plugin state snapshots are taken on the global region thread, and chat responses are sent from the calling thread without touching shared server state.

### Minecraft versions

The plugin is compiled against the **1.8.8 Bukkit API** with Java 8 bytecode and uses no NMS or version-specific code, so the same JAR runs from 1.8 to the latest version. The only class compiled for Java 17 is the Folia scheduler adapter, which is loaded only after the runtime confirms the Folia scheduler API exists (it always does on any server new enough to have it). Older servers simply use the classic Bukkit scheduler instead.

### Plugin Hider (PL-Hider)

- Plugin Categories does **not** register `/pl` or `/plugins` itself — there are no command registration conflicts.
- Interception happens at `HIGHEST` priority with `ignoreCancelled = true`: if PL-Hider (or any similar plugin) already cancelled `/pl` for a player, Plugin Categories stays out of the way completely.
- Players **without** `plugincategories.use` are never touched — PL-Hider keeps hiding your plugins from them exactly as before.
- Players **with** `plugincategories.use` see the categorized overview. If PL-Hider is set up to block `/pl` for everyone without a bypass group, grant those staff members the bypass in PL-Hider too so both plugins can cooperate.

### CommandWhitelist (YouHaveTrouble)

- All interactive navigation runs through the same root command (`/pl view ...`, `/pl back`) that opened the overview — whitelisting `pl` is enough for the whole GUI to work, including clicks.
- The optional `/plugincategories` command is only needed for console/admin use or as a fallback; whitelist it too if your staff should use it from chat.
- Cancellations made by CommandWhitelist are respected (the listener ignores already-cancelled command events).

## Building From Source

```bash
git clone https://github.com/seyedaliahmadirouein/Plugin-Categories.git
cd Plugin-Categories
mvn clean package
```

The distributable JAR is created at `dist/target/PluginCategories-<version>.jar`.

Project layout (Maven multi-module):

| Module | Purpose |
| --- | --- |
| `core` | Plugin logic, compiled for Java 8 against the Spigot 1.8.8 API + Adventure API (both `provided`, nothing is shaded) |
| `folia-support` | The Folia scheduler adapter, compiled for Java 17 against the Paper 1.20.4 API |
| `dist` | Shades `core` + `folia-support` into the single release JAR |

There are no runtime dependencies — everything the plugin needs is provided by the server itself.

## Limitations

- **Console rendering:** the console receives plain colored text; click events and hover text only exist for players (they still get full components on Paper/Folia consoles that support them).
- **Bukkit vs. Spigot sections:** vanilla `/pl` cannot technically distinguish "Bukkit plugins" from "Spigot plugins" — from the API's perspective every installed plugin is a Bukkit plugin. Plugin Categories therefore shows one unified, truthful list instead of two artificial sections.
- **Subfolder JARs are not loaded by vanilla servers** (see the folder system section above) — they will show red until the server actually loads them from the root (or via symlink/copy, which name-matching handles).
- **Centered `[Back]`:** Minecraft chat has no true centering; the button is centered using configurable space padding, which can look slightly off depending on chat width/scale.
- **Legacy hover on 1.8–1.15:** hover text is sent through the BungeeCord API; very old clients render multi-line hovers differently.
- **`/bukkit:pl` and `/bukkit:plugins`:** namespaced variants are intentionally left untouched as a vanilla escape hatch for administrators.
- **Interaction with aggressive hiders:** if another plugin cancels `/pl` for *everyone* regardless of permissions, staff need that plugin's bypass permission to see the categorized overview (nothing Plugin Categories can override safely, by design).

## Tested Environments

- Paper 1.20.4 (build 499) — Java 21: enable, scan, console views, simulated player `/pl` interception, category clicks, `[Back]`, unknown category, reload, permission fallbacks — all verified.
- Folia 1.20.4 (build 31) — Java 21: enable with `folia-supported`, Folia scheduler boot scan, console views, reload, clean shutdown — all verified.
- Bukkit/Spigot 1.8.8 API compatibility enforced at compile time (the core module is built against `spigot-api 1.8.8` with `--release 8`, so no newer API can be referenced accidentally).

## License

See [LICENSE](LICENSE) for the repository license. The compiled plugin JAR published in Releases is built from this exact source.
