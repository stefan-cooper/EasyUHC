import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import com.stefancooper.EasyUHC.Plugin;
import com.stefancooper.EasyUHC.evolvingshield.Constants;
import com.stefancooper.EasyUHC.evolvingshield.EvolvingShield;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.inventory.InventoryViewMock;
import utils.TestUtils;

import java.util.List;
import java.util.Map;

import static com.stefancooper.EasyUHC.Defaults.WORLD_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EvolvingShieldTest {

    private static ServerMock server;
    private static Plugin plugin;
    private static World world;
    private static PlayerMock serverOp;

    @BeforeAll
    public static void load()
    {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(Plugin.class);
        world = server.getWorld(WORLD_NAME);
        serverOp = server.addPlayer();
        serverOp.setOp(true);
    }

    @BeforeEach
    public void cleanUp() {
        TestUtils.executeCommand(plugin, serverOp, "cancel");
        plugin.getUHCConfig().resetToDefaults();
    }

    @AfterAll
    public static void unload() {
        plugin.getUHCConfig().resetToDefaults();
        MockBukkit.unmock();
    }

    private void assertUpdatedXP(final ItemStack shield, final PlayerMock player, final int newXP, final boolean upgradeAvailable, final boolean hasHitThreshold, final Map<Enchantment, Integer> enchantments) {
        final List<Component> expectedLore = new java.util.ArrayList<>(List.of(
                Component.text(""),
                Component.text("Level this shield up by gaining EXP,"),
                Component.text("dealing damage to players and getting kills!"),
                Component.text("")
        ));

        if (hasHitThreshold) {
            expectedLore.addAll(List.of(
                    Component.text("Minecraft EXP limit reached: deal damage and kill", NamedTextColor.DARK_RED),
                    Component.text("players to continue levelling up this shield!", NamedTextColor.DARK_RED),
                    Component.text("")
            ));
        }
        if (upgradeAvailable) {
            expectedLore.addAll(List.of(
                    Component.text("SHIELD UPGRADE AVAILABLE!", NamedTextColor.GOLD),
                    Component.text("Shift-click the shield to upgrade", NamedTextColor.GOLD),
                    Component.text("")
            ));
            player.assertSoundHeard(Sound.BLOCK_NOTE_BLOCK_CHIME);
        }

        expectedLore.add(Component.text(String.format("Current XP: %s", newXP)));

        assertEquals(expectedLore, shield.getItemMeta().lore());
        assertEquals(enchantments, shield.getEnchantments());
        assertEquals(player.getName(), shield.getItemMeta().getPersistentDataContainer().get(plugin.getUHCConfig().getManagedResources().getKeys().getEvolvingShieldUserKey(), PersistentDataType.STRING));
        assertEquals(newXP, shield.getItemMeta().getPersistentDataContainer().get(plugin.getUHCConfig().getManagedResources().getKeys().getEvolvingShieldXPKey(), PersistentDataType.INTEGER));
    }

    private void addXPViaEvent(final Player player, final int addXP) {
        ExperienceOrb orb = world.spawn(player.getLocation(), ExperienceOrb.class);
        orb.setExperience(addXP);
        server.getPluginManager().callEvent(new PlayerPickupExperienceEvent(player, orb));
    }

    private void openUpgradeMenu(final PlayerMock player) {
        final InventoryViewMock invView = new InventoryViewMock(
                player,
                player.getInventory(),
                player.getInventory(),
                InventoryType.CRAFTING)
        {};

        server.getPluginManager().callEvent(new InventoryClickEvent(invView, InventoryType.SlotType.CRAFTING, 0, ClickType.SHIFT_LEFT, InventoryAction.NOTHING));
    }

    private void selectUpgradeInUpgradeMenu(final PlayerMock player, final String upgrade) {
        final InventoryViewMock invView = new InventoryViewMock(
                player,
                player.getInventory(),
                player.getInventory(),
                InventoryType.CRAFTING)
        {};
        final ItemStack selectedItem = new ItemStack(Material.CACTUS);
        final ItemMeta itemMeta = selectedItem.getItemMeta();
        itemMeta.getPersistentDataContainer()
                .set(plugin.getUHCConfig().getManagedResources().getKeys().getEvolvingShieldUpgradeTypeKey(), PersistentDataType.STRING, upgrade);
        selectedItem.setItemMeta(itemMeta);

        invView.setItem(10, selectedItem);

        server.getPluginManager().callEvent(new InventoryClickEvent(invView, InventoryType.SlotType.CONTAINER, 10, ClickType.LEFT, InventoryAction.NOTHING));
    }

    @Test
    @DisplayName("The shield evolves and chooses Knockback")
    void evolvingShieldsXPKnockbackTest() {
        PlayerMock admin = server.addPlayer();
        admin.setOp(true);

        TestUtils.executeCommand(plugin, admin, "set",
                "enable.evolving.shields=true",
                "evolving.shields.minecraft.exp.multiplier=1"
        );

        PlayerMock player1 = server.addPlayer();
        player1.setName("stefan");

        TestUtils.executeCommand(plugin, admin, "start");

        assertTrue(EvolvingShield.isEvolvingShield(plugin.getUHCConfig(), player1.getInventory().getItemInMainHand()));
        assertUpdatedXP(
                player1.getInventory().getItemInMainHand(),
                player1,
                0,
                false,
                false,
                Map.of()
        );

        addXPViaEvent(player1, 10);
        assertUpdatedXP(
                player1.getInventory().getItemInMainHand(),
                player1,
                10,
                false,
                false,
                Map.of()
        );

        addXPViaEvent(player1, 20);
        assertUpdatedXP(
                player1.getInventory().getItemInMainHand(),
                player1,
                EvolvingShield.STAGE_1,
                true,
                false,
                Map.of()
        );

        openUpgradeMenu(player1);
        selectUpgradeInUpgradeMenu(player1, Constants.KNOCKBACK);

        assertUpdatedXP(
                player1.getInventory().getItemInMainHand(),
                player1,
                EvolvingShield.STAGE_1,
                false,
                false,
                Map.of(
                        Enchantment.KNOCKBACK, 1
                )
        );

        TestUtils.executeCommand(plugin, admin, "cancel");
    }

    @Test
    @DisplayName("The shield evolves and chooses Thorns")
    void evolvingShieldsXPThornsTest() {
        PlayerMock admin = server.addPlayer();
        admin.setOp(true);

        TestUtils.executeCommand(plugin, admin, "set",
                "enable.evolving.shields=true",
                "evolving.shields.minecraft.exp.multiplier=1"
        );

        PlayerMock player1 = server.addPlayer();
        player1.setName("stefan");

        TestUtils.executeCommand(plugin, admin, "start");

        assertTrue(EvolvingShield.isEvolvingShield(plugin.getUHCConfig(), player1.getInventory().getItemInMainHand()));
        assertUpdatedXP(
                player1.getInventory().getItemInMainHand(),
                player1,
                0,
                false,
                false,
                Map.of()
        );

        addXPViaEvent(player1, 10);
        assertUpdatedXP(
                player1.getInventory().getItemInMainHand(),
                player1,
                10,
                false,
                false,
                Map.of()
        );

        addXPViaEvent(player1, 20);
        assertUpdatedXP(
                player1.getInventory().getItemInMainHand(),
                player1,
                EvolvingShield.STAGE_1,
                true,
                false,
                Map.of()
        );

        openUpgradeMenu(player1);
        selectUpgradeInUpgradeMenu(player1, Constants.THORNS);

        assertUpdatedXP(
                player1.getInventory().getItemInMainHand(),
                player1,
                EvolvingShield.STAGE_1,
                false,
                false,
                Map.of(
                        Enchantment.THORNS, 1
                )
        );

        TestUtils.executeCommand(plugin, admin, "cancel");
    }

    @Test
    @DisplayName("The shield evolves and hits the threshold for regular XP")
    void evolvingShieldsThresholdTest() {
        PlayerMock admin = server.addPlayer();
        admin.setOp(true);

        TestUtils.executeCommand(plugin, admin, "set",
                "enable.evolving.shields=true",
                "evolving.shields.minecraft.exp.multiplier=1",
                String.format("evolving.shields.exp.threshold=%s", EvolvingShield.STAGE_2)
        );

        PlayerMock player1 = server.addPlayer();
        player1.setName("stefan");

        TestUtils.executeCommand(plugin, admin, "start");

        assertTrue(EvolvingShield.isEvolvingShield(plugin.getUHCConfig(), player1.getInventory().getItemInMainHand()));
        assertUpdatedXP(
                player1.getInventory().getItemInMainHand(),
                player1,
                0,
                false,
                false,
                Map.of()
        );

        addXPViaEvent(player1, 10);
        assertUpdatedXP(
                player1.getInventory().getItemInMainHand(),
                player1,
                10,
                false,
                false,
                Map.of()
        );

        addXPViaEvent(player1, 20);
        assertUpdatedXP(
                player1.getInventory().getItemInMainHand(),
                player1,
                EvolvingShield.STAGE_1,
                true,
                false,
                Map.of()
        );

        openUpgradeMenu(player1);
        selectUpgradeInUpgradeMenu(player1, Constants.KNOCKBACK);

        assertUpdatedXP(
                player1.getInventory().getItemInMainHand(),
                player1,
                EvolvingShield.STAGE_1,
                false,
                false,
                Map.of(
                        Enchantment.KNOCKBACK, 1
                )
        );

        addXPViaEvent(player1, EvolvingShield.STAGE_2 - EvolvingShield.STAGE_1);

        assertUpdatedXP(
                player1.getInventory().getItemInMainHand(),
                player1,
                EvolvingShield.STAGE_2,
                true,
                true,
                Map.of(
                        Enchantment.KNOCKBACK, 1
                )
        );

        openUpgradeMenu(player1);
        selectUpgradeInUpgradeMenu(player1, Constants.THORNS);

        assertUpdatedXP(
                player1.getInventory().getItemInMainHand(),
                player1,
                EvolvingShield.STAGE_2,
                false,
                true,
                Map.of(
                        Enchantment.KNOCKBACK, 1,
                        Enchantment.THORNS, 1
                )
        );

        addXPViaEvent(player1, 1000);

        assertUpdatedXP(
                player1.getInventory().getItemInMainHand(),
                player1,
                EvolvingShield.STAGE_2,
                false,
                true,
                Map.of(
                        Enchantment.KNOCKBACK, 1,
                        Enchantment.THORNS, 1
                )
        );


        TestUtils.executeCommand(plugin, admin, "cancel");
    }

//    this test doesn't work because MockBukkit doesn't bootstrap the plugin like a paper plugin would
//    @Test
//    @DisplayName("The shield evolves and upgrades to a stage two")
//    void evolvingShieldsXPStageTwoTest() {
//        PlayerMock admin = server.addPlayer();
//        admin.setOp(true);
//
//        TestUtils.executeCommand(plugin, admin, "set",
//                "enable.evolving.shields=true"
//        );
//
//        PlayerMock player1 = server.addPlayer();
//        player1.setName("stefan");
//
//        TestUtils.executeCommand(plugin, admin, "start");
//
//        assertTrue(EvolvingShield.isEvolvingShield(plugin.getUHCConfig(), player1.getInventory().getItemInMainHand()));
//        assertUpdatedXP(
//                player1.getInventory().getItemInMainHand(),
//                player1,
//                0,
//                false,
//                Map.of()
//        );
//
//        addXPViaEvent(player1, 10);
//        assertUpdatedXP(
//                player1.getInventory().getItemInMainHand(),
//                player1,
//                10,
//                false,
//                Map.of()
//        );
//
//        addXPViaEvent(player1, 90);
//        assertUpdatedXP(
//                player1.getInventory().getItemInMainHand(),
//                player1,
//                EvolvingShield.STAGE_1,
//                true,
//                Map.of()
//        );
//
//        openUpgradeMenu(player1);
//        selectUpgradeInUpgradeMenu(player1, Constants.KNOCKBACK);
//
//        assertUpdatedXP(
//                player1.getInventory().getItemInMainHand(),
//                player1,
//                EvolvingShield.STAGE_1,
//                false,
//                Map.of(
//                        Enchantment.KNOCKBACK, 1
//                )
//        );
//
//        addXPViaEvent(player1, EvolvingShield.STAGE_2 - EvolvingShield.STAGE_1);
//
//        assertUpdatedXP(
//                player1.getInventory().getItemInMainHand(),
//                player1,
//                EvolvingShield.STAGE_2,
//                true,
//                Map.of(
//                        Enchantment.KNOCKBACK, 1
//                )
//        );
//
//        openUpgradeMenu(player1);
//        selectUpgradeInUpgradeMenu(player1, Constants.STRENGTH);
//
//        assertUpdatedXP(
//                player1.getInventory().getItemInMainHand(),
//                player1,
//                EvolvingShield.STAGE_2,
//                false,
//                Map.of(
//                        Enchantment.KNOCKBACK, 1,
//                        plugin.getUHCConfig().getManagedResources().getShieldStrengthEnchantment(), 1
//                )
//        );
//
//        TestUtils.executeCommand(plugin, admin, "cancel");
//    }
}
