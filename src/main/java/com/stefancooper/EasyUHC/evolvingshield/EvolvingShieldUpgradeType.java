package com.stefancooper.EasyUHC.evolvingshield;

public enum EvolvingShieldUpgradeType {

    // Item Upgrades
    FOOD("food", "Extra food"),
    ARROWS("arrows", "Arrows"),
    ARROWS_SPECTRAL("arrows_spectral", "Spectral Arrows"),
    ARROWS_TIPPED("arrows_tipped", "Tipped Arrows"),
    BOOKS("books", "Books"),
    APPLES("apples", "Apples"),
    IRON("iron", "Iron ingots"),
    COAL("coal", "Coal"),
    // Kits
    REAPER_KIT("reaper_kit", "Reaper kit"),
    APOTHECARY_KIT("apothecary_kit", "Apothecary kit"),
    LIBRARIAN_KIT("librarian_kit", "Librarian kit"),
    PHOENIX_KIT("phoenix_kit", "Phoenix kit"),
    NETHER_EXPLORER_KIT("nether_explorer_kit", "Nether explorer kit"),
    // TNTs
    FAST_TNT("fast_tnt", "TNT with Quickboom IV"),
    BIG_TNT("big_tnt", "TNT with Blastwave IV"),
    MIXED_TNT("mixed_tnt", "TNT with Quickboom II & Blastwave II"),
    // HP
    ABSORPTION("absorption", "Absorption"),
    PLAYER_HEAD("player_head", "Player head"),
    REGEN("regen", "4 hearts of regeneration"),
    // Enchants
    KNOCKBACK("knockback", "Knockback for your shield"),
    THORNS("thorns", "Thorns for your shield"),
    // Buff Enchants
    SWIFTNESS("swiftness", "Swift Defense for your shield"),
    JUMP("jump", "Leap Guard for your shield"),
    STRENGTH("strength", "Counterforce for your shield"),
    // Debuff Enchants
    SLOWNESS("slowness", "Sapping Guard for your shield"),
    WEAKNESS("weakness", "Snare Guard for your shield"),
    // Elementals
    THUNDER("thunder"),
    WIND("wind"),
    FIRE("fire"),
    WATER("water"),
    // Evil jesters
    EFFECT_NAUSEA("e_nausea", "NAUSEA!"),
    EFFECT_BLINDNESS("e_blindness", "BLINDNESS!"),
    EFFECT_SLOWNESS("e_slowness", "SLOWNESS!"),
    EFFECT_MINING_FATIGUE("e_mining_fatigue", "MINING FATIGUE!"),

    // Misc
    JESTER("jester");


    private final String id;
    private final String jesterDescription;

    EvolvingShieldUpgradeType(final String id) {
        this.id = id;
        this.jesterDescription = "";
    }

    EvolvingShieldUpgradeType(final String id, final String jesterDescription) {
        this.id = id;
        this.jesterDescription = jesterDescription;
    }

    public String getId() {
        return id;
    }

    public String getJesterDescription() {
        return jesterDescription;
    }

    public static EvolvingShieldUpgradeType fromString(String type) {
        for (EvolvingShieldUpgradeType Key : EvolvingShieldUpgradeType.values()) {
            if (Key.id.equalsIgnoreCase(type)) {
                return Key;
            }
        }
        return null;
    }

}
