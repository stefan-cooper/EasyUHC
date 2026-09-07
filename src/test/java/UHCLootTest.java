import com.stefancooper.EasyUHC.base.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.block.state.ChestStateMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.scheduler.BukkitSchedulerMock;
import com.stefancooper.EasyUHC.Plugin;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.world.WorldMock;
import utils.TestUtils;

import java.util.Arrays;
import java.util.List;

import static com.stefancooper.EasyUHC.Defaults.NETHER_WORLD_NAME;
import static com.stefancooper.EasyUHC.Defaults.WORLD_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class UHCLootTest {

    private static ServerMock server;
    private static Plugin plugin;
    private static World world;
    private static World nether;
    private static PlayerMock admin;

    @BeforeAll
    public static void load() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(Plugin.class);
        world = server.getWorld(WORLD_NAME);
        nether = server.getWorld(NETHER_WORLD_NAME);
        admin = server.addPlayer();
        admin.setOp(true);
    }

    @BeforeEach
    public void cleanUp() {
        TestUtils.executeCommand(plugin, admin, "start");
        plugin.getUHCConfig().resetToDefaults();
    }

    @AfterAll
    public static void unload() {
        plugin.getUHCConfig().resetToDefaults();
        MockBukkit.unmock();
    }

    private List<ItemStack> getLatestChestContents(ChestStateMock chest) {
        return Arrays.stream(chest.getBlockInventory().getStorageContents()).filter(item -> item != null && item.getType() != Material.AIR).toList();
    }

    @Test
    void lootChestTestHighTierLootMessage() {
        BukkitSchedulerMock schedule = server.getScheduler();
        PlayerMock admin = server.addPlayer();
        PlayerMock netherPlayer = server.addPlayer();

        admin.setOp(true);
        String x = "750,1000";
        String z = "250,500";
        int lootFrequency = 5; // 100 ticks

        // set world spawn
        TestUtils.executeCommand(plugin, admin, "set",
                String.format("countdown.timer.length=%s", "3"),
                String.format("loot.chest.x.range=%s", x),
                String.format("loot.chest.z.range=%s", z),
                String.format("loot.chest.frequency=%s", lootFrequency),
                String.format("loot.chest.enabled=%s", "true"),
                String.format("loot.chest.high.loot.odds=%s", "100")
        );

        TestUtils.executeCommand(plugin, admin, "start");

        schedule.performOneTick();
        admin.assertSaid(Component.text("UHC: Countdown starting now. Don't forget to record your POV if you can. GLHF!", Style.style(NamedTextColor.GRAY, TextDecoration.ITALIC)));
        netherPlayer.assertSaid(Component.text("UHC: Countdown starting now. Don't forget to record your POV if you can. GLHF!", Style.style(NamedTextColor.GRAY, TextDecoration.ITALIC)));

        schedule.performTicks(Utils.secondsToTicks(3));

        admin.assertSaid(Component.text("UHC: High tier loot item(s) have spawned in the overworld loot chest!", Style.style(NamedTextColor.GOLD, TextDecoration.BOLD)));
        admin.assertNoMoreSaid();

        netherPlayer.assertSaid(Component.text("UHC: High tier loot item(s) have spawned in the overworld loot chest!", Style.style(NamedTextColor.GOLD, TextDecoration.BOLD)));
        netherPlayer.assertNoMoreSaid();

        netherPlayer.teleport(nether.getSpawnLocation());
        schedule.performTicks(Utils.secondsToTicks(lootFrequency));

        admin.assertSaid(Component.text("UHC: High tier loot item(s) have spawned in the overworld loot chest!", Style.style(NamedTextColor.GOLD, TextDecoration.BOLD)));
        admin.assertNoMoreSaid();

        netherPlayer.assertSaid(Component.text("UHC: High tier loot item(s) have spawned in the nether loot chest! Players in the overworld do not see this message", Style.style(NamedTextColor.GOLD, TextDecoration.BOLD)));
        netherPlayer.assertNoMoreSaid();

        netherPlayer.teleport(world.getSpawnLocation());
        schedule.performTicks(Utils.secondsToTicks(lootFrequency));

        admin.assertSaid(Component.text("UHC: High tier loot item(s) have spawned in the overworld loot chest!", Style.style(NamedTextColor.GOLD, TextDecoration.BOLD)));
        admin.assertNoMoreSaid();

        netherPlayer.assertSaid(Component.text("UHC: High tier loot item(s) have spawned in the overworld loot chest!", Style.style(NamedTextColor.GOLD, TextDecoration.BOLD)));
        netherPlayer.assertNoMoreSaid();
    }

    @Test
    void dynamicLootChestTest() {
        BukkitSchedulerMock schedule = server.getScheduler();
        PlayerMock admin = server.addPlayer();
        admin.setOp(true);
        String x = "750,1000";
        String z = "250,500";
        int lootFrequency = 5; // 100 ticks

        // set world spawn
        TestUtils.executeCommand(plugin, admin, "set",
                String.format("countdown.timer.length=%s", "5"),
                String.format("loot.chest.x.range=%s", x),
                String.format("loot.chest.z.range=%s", z),
                String.format("loot.chest.frequency=%s", lootFrequency),
                String.format("loot.chest.enabled=%s", "true"),
                String.format("loot.chest.high.loot.odds=%s", "0")
        );

        TestUtils.executeCommand(plugin, admin, "start");

        schedule.performOneTick();
        schedule.performTicks(Utils.secondsToTicks(5));

        Block firstGenerationChest = plugin.getUHCConfig().getManagedResources().getDynamicLootChestLocation();
        Block netherFirstGenerationChest = plugin.getUHCConfig().getManagedResources().getDynamicNetherLootChestLocation();
        assertEquals(Material.CHEST, firstGenerationChest.getType());
        assertEquals(Material.CHEST, netherFirstGenerationChest.getType());

        // start uhc (so the world spawn should now be ignored)
        final List<ItemStack> firstGeneration = getLatestChestContents((ChestStateMock) world.getBlockAt(firstGenerationChest.getLocation()).getState());
        final List<ItemStack> netherFirstGeneration = getLatestChestContents((ChestStateMock) nether.getBlockAt(netherFirstGenerationChest.getLocation()).getState());
        assertNotEquals(0, firstGeneration.size());
        assertNotEquals(0, netherFirstGeneration.size());

        schedule.performTicks(Utils.secondsToTicks(5));

        Block secondGenerationChest = plugin.getUHCConfig().getManagedResources().getDynamicLootChestLocation();
        Block netherSecondGenerationChest = plugin.getUHCConfig().getManagedResources().getDynamicNetherLootChestLocation();
        assertEquals(Material.AIR, firstGenerationChest.getType());
        assertEquals(Material.AIR, netherFirstGenerationChest.getType());
        assertEquals(Material.CHEST, secondGenerationChest.getType());
        assertEquals(Material.CHEST, netherSecondGenerationChest.getType());

        final List<ItemStack> secondGeneration = getLatestChestContents((ChestStateMock) world.getBlockAt(secondGenerationChest.getLocation()).getState());
        final List<ItemStack> netherSecondGeneration = getLatestChestContents((ChestStateMock) nether.getBlockAt(netherSecondGenerationChest.getLocation()).getState());

        assertNotEquals(0, secondGeneration.size());
        assertNotEquals(0, netherSecondGeneration.size());
        assertNotEquals(firstGeneration, secondGeneration);
        assertNotEquals(netherFirstGeneration, netherSecondGeneration);
    }
}
