# ⛏️ Forge Ore Calculator (completely vibe-coded, just needed this tool)

**Forge Ore Calculator** is a lightweight client-side utility mod for Minecraft (Fabric 1.21). It is designed for Tycoon/Economy servers that feature "Forge" mechanics where players must input specific ores to get a multiplier.

Instead of doing the math in your head or guessing which stacks to use, this mod scans your inventory and calculates the **mathematically perfect loadout** to maximize your returns.

## ✨ Features

* **Automatic Detection:** Instantly activates when you open a container named "Forge".
* **Smart Calculation:** Scans both your inventory and the chest slots to find every available ore.
* **Stack Optimization:** It doesn't just list the best ores; it calculates the best **Stacks of 64**.
    * *Example:* If you have 100 Diamonds, it knows to split them into `Diamond x64` (Slot 1) and `Diamond x36` (Slot 2).
* **Visual HUD:** Draws a clean overlay in the top-left corner of your screen showing:
    * The Top 5 best slots to fill.
    * Real Item Icons (prevents invisible text glitches).
    * Exact amounts and calculated value for each slot.
* **Range Calculator:** Displays the total multiplier range (e.g., `Total: 700x - 1400x`) to help you estimate rewards.
* **Chat Backup:** Press **SPACEBAR** while looking at the menu to print the calculation result directly to your chat (useful if the HUD is blocked).

## 📸 Screenshots

*(Upload your screenshots to your repository issues or a hosting site, and link them here!)*

## 🚀 Installation

1.  **Download** the latest release `.jar` file.
2.  Ensure you have **[Fabric Loader](https://fabricmc.net/)** and **[Fabric API](https://modrinth.com/mod/fabric-api)** installed for Minecraft 1.21.
3.  Place the `.jar` file into your `mods` folder:
    * **Windows:** `%appdata%/.minecraft/mods`
    * **Feather Client:** Open Feather -> Mods -> "Add Own Mod" -> Drag file in.
4.  Launch the game!

## 🎮 How to Use

1.  Join your server and open the **Forge** GUI.
2.  Look at the **Top-Left** of your screen. The calculator will appear automatically.
3.  Fill the Forge slots exactly as the list shows (`#1` goes in the first slot, etc.).
4.  Profit! 💰

## ⚙️ Configuration / Compatibility
* **Version:** Minecraft 1.21+ (Fabric)
* **Server Friendly:** This is a **Client-Side Only** mod. It does not send weird packets or move items automatically (no macros). It simply reads the screen and displays text.
    * *Note: Always check your specific server's rules regarding "calculator" or "utility" mods.*

## 🛠️ Building from Source

If you want to edit the code yourself:

```bash
# 1. Clone the repository
git clone [https://github.com/YOUR_USERNAME/forge-ore-calculator.git](https://github.com/YOUR_USERNAME/forge-ore-calculator.git)

# 2. Navigate to the folder
cd forge-ore-calculator

# 3. Build the mod
./gradlew build

# 4. Find the jar
# The output file will be in /build/libs/
