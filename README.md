# Quickstack Backpacks Compat

Addon for **Forge 1.20.1** that adds compatibility between:
- **DropOff / Quickstack** (`quickstack`)
- **Sophisticated Backpacks** (`sophisticatedbackpacks`)

It adds **Quickstack** and **Dump** buttons to Sophisticated Backpacks' backpack screen, using DropOff logic to move items into nearby containers.

## Features
- Quickstack/Dump buttons on `sophisticatedbackpacks:backpack`
- Moves items from the backpack into nearby containers
- Optional config to include player inventory in the operation
- Per-backpack button position config (`item id` based)
- Position hot-reload when reopening the GUI (no client restart)

## Requirements
- Minecraft `1.20.1`
- Forge `47.x`
- Java `17`
- Runtime mods:
  - `quickstack`
  - `sophisticatedbackpacks`

## Installation (player)
1. Install Forge 1.20.1.
2. Put these mods in `mods/`:
   - `quickstack`
   - `sophisticatedbackpacks`
   - `quickstackbackpackscompat`
3. Start the game.

## Configuration

### 1) Addon client config (Forge TOML)
Generated file: `config/quickstackbackpackscompat-client.toml`
- `includePlayerInventory` (default: `false`)
  - `false`: only backpack items are moved
  - `true`: backpack + player inventory are moved (respecting DropOff `ignoreHotBar`)

### 2) Button positions (JSON)
File: `config/quickstackbackpackscompat_button_positions.json`

- Profiles by backpack `item id` (example: `sophisticatedbackpacks:iron_backpack`)
- Fallback: `profiles["*"]`
- Missing fields inherit from `defaults`
- Hot-reload when reopening the screen

Current default values:

```json
{
  "version": 1,
  "defaults": {
    "anchor": "STORAGE_TOP_RIGHT",
    "offsetX": -90,
    "offsetY": -110,
    "buttonSpacingX": 12
  },
  "profiles": {
    "*": {
      "offsetX": -90,
      "offsetY": -110,
      "buttonSpacingX": 12
    },
    "sophisticatedbackpacks:backpack": {
      "offsetX": -165,
      "offsetY": 53
    },
    "sophisticatedbackpacks:copper_backpack": {
      "offsetX": -165,
      "offsetY": 89
    },
    "sophisticatedbackpacks:iron_backpack": {
      "offsetX": -165,
      "offsetY": 107
    },
    "sophisticatedbackpacks:gold_backpack": {
      "offsetX": -165,
      "offsetY": 161
    },
    "sophisticatedbackpacks:diamond_backpack": {
      "offsetX": -190,
      "offsetY": 161
    },
    "sophisticatedbackpacks:netherite_backpack": {
      "offsetX": -190,
      "offsetY": 179
    }
  }
}
```

## Build (development)
```powershell
.\gradlew build -x test
```

Output jar:
- `build/libs/quickstackbackpackscompat-1.20.1-1.0.0.jar`

## Scope
- Does not change `C/X` hotkeys
- Does not change DropOff behavior in vanilla inventory
- Does not modify `DropOff` or `SophisticatedBackpacks` source code
