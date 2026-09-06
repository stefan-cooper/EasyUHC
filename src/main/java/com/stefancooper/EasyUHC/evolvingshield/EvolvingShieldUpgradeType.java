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
    KNOCKBACK("knockback", "Knockback added or upgraded to your shield"),
    THORNS("thorns", "Thorns added or upgraded to your shield"),
    // Buff Enchants
    SWIFTNESS("swiftness", "Swift Defense added to your shield"),
    JUMP("jump", "Leap Guard added to your shield"),
    STRENGTH("strength", "Counterforce added to your shield"),
    // Debuff Enchants
    SLOWNESS("slowness", "Sapping Guard added to your shield"),
    WEAKNESS("weakness", "Snare Guard added to your shield"),
    // Elementals
    THUNDER("thunder"),
    WIND("wind"),
    FIRE("fire"),
    WATER("water"),
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
