package com.example; // MAKE SURE THIS MATCHES YOUR PACKAGE

public enum OreData {
    COAL("Coal Ore", 0.01),
    COPPER("Copper Ore", 0.03),
    IRON("Iron Ore", 0.05),
    ZINC("Zinc Ore", 0.1),
    EARTH("Earth Ore", 0.15),
    SILVER("Silver Ore", 0.2),
    GOLD("Gold Ore", 0.33),
    RUBY("Ruby Ore", 0.75),
    SAPPHIRE("Sapphire Ore", 1.5),
    DIAMOND("Diamond Ore", 2.5),
    EMERALD("Emerald Ore", 3.5),
    EYE_CORE("Eye Core", 5.0),
    MITHRIL("Mythril Ore", 10.0),
    OBSIDIAN("Obisidian", 30.0),
    URANIUM("Uranium Ore", 50.0),
    PLUTONIUM("Plutonium Ore", 75.0),
    ANTIMATTER("Antimatter Ore", 100.0),
    DIAMOND_CLUSTER("Diamond Cluster", 150.0),
    COSMIC("Cosmic Clutter", 200.0);

    public final String displayName;
    public final double multiplier;

    OreData(String displayName, double multiplier) {
        this.displayName = displayName;
        this.multiplier = multiplier;
    }

    public static OreData find(String itemName) {
        // "The Nuclear Option": Remove ANYTHING that isn't a Letter or Number.
        String cleanInput = itemName.replaceAll("(?i)§[0-9a-fk-orx]", "")
                .replaceAll("[^a-zA-Z0-9 ]", "").trim();

        for (OreData ore : values()) {
            String cleanOreName = ore.displayName.replaceAll("[^a-zA-Z0-9 ]", "");
            if (cleanInput.contains(cleanOreName)) {
                return ore;
            }
        }
        return null;
    }
}