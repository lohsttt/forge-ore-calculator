package com.example;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class ForgeOreCalculatorClient implements ClientModInitializer {

	// 0xB0 = ~70% Opacity
	private static final int BACKGROUND_COLOR = 0xB0000000;

	private boolean isForgeOpen = false;
	private List<StackEntry> topStacks = new ArrayList<>();
	private double totalMultiplier = 0.0;
	private boolean spacePressed = false;
	private int emptyTicks = 0;

	@Override
	public void onInitializeClient() {
		System.out.println("FORGE CALCULATOR (1.21.8 COMPATIBLE) LOADED!");

		// 1. LOGIC LOOP
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null || client.currentScreen == null) {
				isForgeOpen = false;
				emptyTicks = 0;
				return;
			}

			if (!isOnAfkableServer(client)) {
				isForgeOpen = false;
				return;
			}

			if (client.currentScreen instanceof HandledScreen<?> screen) {
				String cleanTitle = screen.getTitle().getString().toLowerCase().replaceAll("[^a-z0-9]", "");

				if (cleanTitle.contains("forge")) {
					isForgeOpen = true;
					calculate(screen.getScreenHandler());

					if (topStacks.isEmpty()) {
						emptyTicks++;
					} else {
						emptyTicks = 0;
					}

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
					emptyTicks = 0;
				}
			} else {
				isForgeOpen = false;
				emptyTicks = 0;
			}
		});

		// 2. RENDER LOOP
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (screen instanceof HandledScreen) {
				ScreenEvents.afterRender(screen).register((screen1, drawContext, mouseX, mouseY, tickDelta) -> {
					if (!isForgeOpen) return;

					if (topStacks.isEmpty() && emptyTicks > 10) return;

					int startX = 5;
					int startY = 5;
					int listSize = topStacks.isEmpty() ? 1 : topStacks.size();
					int boxHeight = 20 + (listSize * 20) + 20;

					drawContext.fill(startX - 2, startY - 2, startX + 220, startY + boxHeight, BACKGROUND_COLOR);

					// FIX: Use drawText(..., true) instead of drawTextWithShadow
					drawContext.drawText(client.textRenderer, Text.literal("§6§lBEST COMBINATION"), startX + 5, startY + 2, 0xFFFFFFFF, true);
					startY += 15;

					if (topStacks.isEmpty()) {
						drawContext.drawText(client.textRenderer, Text.literal("§7Scanning..."), startX + 5, startY, 0xFFFFFFFF, true);
					} else {
						int slotNum = 1;
						for (StackEntry entry : topStacks) {
							if (entry.sampleStack != null) {
								drawContext.drawItem(entry.sampleStack, startX + 2, startY);
							}
							String text = String.format("§e#%d: §f%s x%d §7= §a%.2fx", slotNum, entry.name, entry.amount, entry.stackValue);
							// FIX: Use drawText(..., true)
							drawContext.drawText(client.textRenderer, Text.literal(text), startX + 22, startY + 4, 0xFFFFFFFF, true);
							startY += 20;
							slotNum++;
						}
					}

					startY += 5;
					String rangeText = String.format("§aTotal: %.2fx - %.2fx", totalMultiplier / 2.0, totalMultiplier);
					drawContext.drawText(client.textRenderer, Text.literal(rangeText), startX + 5, startY, 0xFFFFFFFF, true);
				});
			}
		});
	}

	private boolean isOnAfkableServer(MinecraftClient client) {
		if (client.world == null) return false;
		Scoreboard scoreboard = client.world.getScoreboard();
		if (scoreboard == null) return false;
		ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
		if (objective == null) return false;
		String title = objective.getDisplayName().getString().toLowerCase();
		return title.contains("afkable");
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
		this.topStacks = allPossibleStacks.stream().limit(5).toList();
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
