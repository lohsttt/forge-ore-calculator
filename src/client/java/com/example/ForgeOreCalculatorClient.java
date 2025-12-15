package com.example;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class ForgeOreCalculatorClient implements ClientModInitializer {

	private boolean isForgeOpen = false;
	private List<StackEntry> topStacks = new ArrayList<>();
	private double totalMultiplier = 0.0;
	private boolean spacePressed = false;

	@Override
	public void onInitializeClient() {
		System.out.println("FORGE CALCULATOR (FINAL V2) LOADED!");

		// 1. LOGIC LOOP
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null || client.currentScreen == null) {
				isForgeOpen = false;
				return;
			}

			if (client.currentScreen instanceof HandledScreen<?> screen) {
				String cleanTitle = screen.getTitle().getString().toLowerCase().replaceAll("[^a-z0-9]", "");

				if (cleanTitle.contains("forge")) {
					isForgeOpen = true;
					calculate(screen.getScreenHandler());

					// Spacebar Backup (Updated to match new Range format)
					if (GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS) {
						if (!spacePressed) {
							String range = String.format("%.2fx - %.2fx", totalMultiplier / 2.0, totalMultiplier);
							client.player.sendMessage(Text.literal("§6[ForgeCalc] Range: §a" + range), false);
							spacePressed = true;
						}
					} else {
						spacePressed = false;
					}

				} else {
					isForgeOpen = false;
				}
			} else {
				isForgeOpen = false;
			}
		});

		// 2. RENDER LOOP
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (screen instanceof HandledScreen) {
				ScreenEvents.afterRender(screen).register((screen1, drawContext, mouseX, mouseY, tickDelta) -> {
					if (!isForgeOpen) return;

					// Draw at Top Left (5, 5)
					int startX = 5;
					int startY = 5;

					// Background Box (Darker and Solid)
					int boxHeight = 20 + (topStacks.size() * 20) + 20;
					// Slightly wider box (180) to fit the new extra text
					drawContext.fill(startX - 2, startY - 2, startX + 180, startY + boxHeight, 0xFF000000);

					// Header (Updated Title)
					drawContext.drawTextWithShadow(client.textRenderer, Text.literal("§6§lBEST COMBINATION"), startX + 5, startY + 2, 0xFFFFFFFF);
					startY += 15;

					if (topStacks.isEmpty()) {
						drawContext.drawTextWithShadow(client.textRenderer, Text.literal("§7Scanning..."), startX + 5, startY, 0xFFFFFFFF);
					} else {
						int slotNum = 1;
						for (StackEntry entry : topStacks) {

							// A. DRAW ITEM ICON
							if (entry.sampleStack != null) {
								drawContext.drawItem(entry.sampleStack, startX + 2, startY);
							}

							// B. DRAW TEXT (Updated Format: xAmount = Value)
							// Example: #1: Obsidian x20 = 600.00x
							String text = String.format("§e#%d: §f%s x%d §7= §a%.2fx", slotNum, entry.name, entry.amount, entry.stackValue);
							drawContext.drawTextWithShadow(client.textRenderer, Text.literal(text), startX + 22, startY + 4, 0xFFFFFFFF);

							startY += 20;
							slotNum++;
						}
					}

					// Total Range (Half - Full)
					startY += 5;
					String rangeText = String.format("§aTotal: %.2fx - %.2fx", totalMultiplier / 2.0, totalMultiplier);
					drawContext.drawTextWithShadow(client.textRenderer, Text.literal(rangeText), startX + 5, startY, 0xFFFFFFFF);
				});
			}
		});
	}

	private void calculate(ScreenHandler handler) {
		Map<OreData, Integer> totalCounts = new HashMap<>();
		Map<OreData, ItemStack> iconCache = new HashMap<>();

		for (Slot slot : handler.slots) {
			if (slot.hasStack()) {
				ItemStack stack = slot.getStack();
				String name = stack.getName().getString();
				OreData ore = OreData.find(name);
				if (ore != null) {
					totalCounts.put(ore, totalCounts.getOrDefault(ore, 0) + stack.getCount());
					iconCache.putIfAbsent(ore, stack);
				}
			}
		}

		List<StackEntry> allPossibleStacks = new ArrayList<>();
		for (Map.Entry<OreData, Integer> entry : totalCounts.entrySet()) {
			OreData ore = entry.getKey();
			int remainingAmount = entry.getValue();
			ItemStack sample = iconCache.get(ore);

			while (remainingAmount > 0) {
				int stackSize = Math.min(64, remainingAmount);
				double stackValue = stackSize * ore.multiplier;
				allPossibleStacks.add(new StackEntry(ore.displayName, stackSize, stackValue, sample));
				remainingAmount -= stackSize;
			}
		}

		allPossibleStacks.sort((a, b) -> Double.compare(b.stackValue, a.stackValue));

		this.topStacks = allPossibleStacks.stream()
				.limit(5)
				.toList();

		this.totalMultiplier = topStacks.stream().mapToDouble(r -> r.stackValue).sum();
	}

	private static class StackEntry {
		String name;
		int amount;
		double stackValue;
		ItemStack sampleStack;

		public StackEntry(String name, int amount, double stackValue, ItemStack sampleStack) {
			this.name = name;
			this.amount = amount;
			this.stackValue = stackValue;
			this.sampleStack = sampleStack;
		}
	}
}