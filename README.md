# ArSimpleMachines
A lightweight addon mod that expands Advanced Rocketry’s early‑game and mid‑game automation with simple, intuitive multiblock machines.  
Designed for modpacks and players who want AR‑style machinery without the complexity of full tech mods.

---

## 🚀 Features

ArSimpleMachines currently adds three core machines:

- **Rolling Machine** – metal forming & tank production
- **Lathe** – rod and shaft machining
- **Gas Charge Pad** – oxygen/hydrogen refilling for AR space suits

All machines follow the Advanced Rocketry aesthetic:
- AR‑style multiblock structures
- Clean, minimal GUIs
- Smooth animations
- FE + fluid integration
- Simple, code‑driven recipe system

---

## 🧱 Machines

---

## 🔧 Rolling Machine (Multiblock)

A compact AR‑style multiblock that processes metals into plates and specialized components.

### Supports:
- **Metal Plate Production**
    - Aluminum Ingot → Aluminum Plate
    - Titanium Ingot → Titanium Plate
- **Component Manufacturing**
    - Aluminum Plate → Portable Pressure Tank (Aluminum)

### GUI Features:
- Input / Output slot frames
- Progress bar
- Power bar
- Fluid bar
- Labels for all slots
- Smooth roller + press animation

### Multiblock Structure


Legend:
- **C** – Rolling Machine Controller
- **I** – Item Input Block
- **O** – Item Output Block
- **E** – Energy Input Block
- **F** – Fluid Input Block
- **R** – Motor Block
- **S** – Structure Blocks

---

## 🔧 Lathe (Multiblock)

A precision machining multiblock designed to shape raw materials into rods, shafts, and cylindrical components used throughout Advanced Rocketry and ArSimpleMachines.

### Supports:
- **Rod Production**
    - Titanium Plate → Titanium Rod
    - Aluminum Plate → Aluminum Rod
- **Custom rod recipes** (via code registry)

### GUI Features:
- Input / Output slots
- Progress bar
- Power bar
- Animated spindle rotation
- Animated carriage travel synced to recipe progress

### Uses:
- Pressure tank components
- Machine parts
- Structural rocket components
- General AR crafting

Recipes are defined in `LatheRecipeRegistry`.

---

## 🔧 Gas Charge Pad

A standalone utility block used to refill Advanced Rocketry space suits with breathable gases.

### Supports:
- **Hydrogen and Oxygen refilling** for AR space suits
- **Bucket input** (manual gas filling)
- **Pipe input** (automated gas supply)
- **Internal gas tank** with GUI
- **Automatic suit detection** when the player stands on the pad

### GUI Displays:
- Gas type
- Gas amount
- Maximum capacity
- Fill progress

Perfect for early‑game oxygen refilling before building full AR life support systems.

---

## ⚙️ Rolling Machine Recipes

### Aluminum Ingot → Aluminum Plate
- Input: `immersiveengineering:ingot_aluminum`
- Output: `immersiveengineering:plate_aluminum`
- Time: 200 ticks
- Energy: 20 FE/t
- Fluid: 100 mB water

### Aluminum Plate → Portable Pressure Tank (Aluminum)
- Input: `immersiveengineering:plate_aluminum`
- Output: `adv_rocketry:portable_pressure_tank_aluminum`
- Time: 200 ticks
- Energy: 20 FE/t
- Fluid: 100 mB water

### Titanium Ingot → Titanium Plate
- Input: `arsimplemachines:titanium_ingot`
- Output: `arsimplemachines:titanium_plate`
- Time: 300 ticks
- Energy: 40 FE/t
- Fluid: 250 mB water

Recipes are defined in code via `RollingRecipeRegistry`.

---

## 🖥️ GUI Summary

### Rolling Machine GUI:
- Input slot
- Output slot
- Progress bar
- Power bar
- Fluid bar
- Roller + press animation

### Lathe GUI:
- Input slot
- Output slot
- Progress bar
- Power bar
- Spindle + carriage animation

### Gas Charge Pad GUI:
- Gas type
- Gas amount
- Capacity
- Fill progress

---

## 📦 Dependencies

This mod requires:
- **Minecraft 1.21.1**
- **NeoForge 21.1.x**
- **Advanced Rocketry (ARLib)**
- **Immersive Engineering** (for aluminum materials)

---

## 🔧 Configuration & Extensibility

The recipe system is designed to support:
- Tag‑based inputs (e.g., `forge:ingots/aluminum`)
- JSON recipe loading (planned)
- JEI integration (planned)
- Additional machines using the same framework

---

## 🛠️ Future Plans

- Steel plate + tank recipes
- JSON‑based recipe system
- JEI plugin
- Additional AR‑style machines
- Custom textures for progress bars and fluid tanks
- Gas Charge Pad upgrades (multi‑gas support, faster fill rates)
- All machines support for more ingot types

---

## 🤝 Contributions

Pull requests are welcome!  
If you’d like to add new machines, recipes, or integrations, feel free to open an issue or PR.
