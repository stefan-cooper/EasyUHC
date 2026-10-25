import com.stefancooper.EasyUHC.Plugin;
import mocks.servers.RespawnPlayerServerMock;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.*;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.scheduler.BukkitSchedulerMock;
import org.mockbukkit.mockbukkit.simulate.entity.LivingEntitySimulation;
import utils.TestUtils;

import java.util.Arrays;
import java.util.Objects;

import static com.stefancooper.EasyUHC.Defaults.*;
import static org.junit.jupiter.api.Assertions.*;


public class SharedInventoryTest {

    private static RespawnPlayerServerMock server;
    private static Plugin plugin;
    private static World world;
    private static World nether;
    private static World end;
    private static PlayerMock serverOp;

    @BeforeAll
    public static void load()
    {
        server = MockBukkit.mock(new RespawnPlayerServerMock());
        plugin = MockBukkit.load(Plugin.class);
        world = server.getWorld(WORLD_NAME);
        nether = server.getWorld(NETHER_WORLD_NAME);
        end = server.getWorld(END_WORLD_NAME);
        serverOp = server.addPlayer();
        serverOp.setOp(true);
    }

    @BeforeEach
    public void cleanUp() {
        TestUtils.executeCommand(plugin, serverOp, "cancel");
        plugin.getUHCConfig().resetToDefaults();
        server.setPlayers(0);
    }

    @AfterAll
    public static void unload() {
        plugin.getUHCConfig().resetToDefaults();
        MockBukkit.unmock();
    }

    private void assertThatSharedHealthIsNotActivated(final PlayerMock teamRed1, final PlayerMock teamRed2, final PlayerMock teamBlue1, final PlayerMock teamBlue2) {
        final LivingEntitySimulation simulateTeamRed1 = new LivingEntitySimulation(teamRed1);
        final LivingEntitySimulation simulateTeamRed2 = new LivingEntitySimulation(teamRed2);
        final LivingEntitySimulation simulateTeamBlue1 = new LivingEntitySimulation(teamBlue1);
        final LivingEntitySimulation simulateTeamBlue2 = new LivingEntitySimulation(teamBlue2);

        assertEquals(20.0, teamRed1.getHealth());
        assertEquals(20.0, teamRed2.getHealth());
        assertEquals(20.0, teamBlue1.getHealth());
        assertEquals(20.0, teamBlue2.getHealth());

        simulateTeamRed1.simulateDamage(2.0, teamBlue1);

        assertEquals(18.0, teamRed1.getHealth());
        assertEquals(20.0, teamRed2.getHealth());
        assertEquals(20.0, teamBlue1.getHealth());
        assertEquals(20.0, teamBlue2.getHealth());

        simulateTeamRed2.simulateDamage(8.0, teamBlue1);

        assertEquals(18.0, teamRed1.getHealth());
        assertEquals(12.0, teamRed2.getHealth());
        assertEquals(20.0, teamBlue1.getHealth());
        assertEquals(20.0, teamBlue2.getHealth());

        simulateTeamBlue1.simulateDamage(3.0, teamRed1);

        assertEquals(18.0, teamRed1.getHealth());
        assertEquals(12.0, teamRed2.getHealth());
        assertEquals(17.0, teamBlue1.getHealth());
        assertEquals(20.0, teamBlue2.getHealth());

        simulateTeamBlue2.simulateDamage(8.0, teamRed2);

        assertEquals(18.0, teamRed1.getHealth());
        assertEquals(12.0, teamRed2.getHealth());
        assertEquals(17.0, teamBlue1.getHealth());
        assertEquals(12.0, teamBlue2.getHealth());

        teamRed1.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 10, 1));

        assertTrue(teamRed1.hasPotionEffect(PotionEffectType.ABSORPTION));
        assertFalse(teamRed2.hasPotionEffect(PotionEffectType.ABSORPTION));
        assertFalse(teamBlue1.hasPotionEffect(PotionEffectType.ABSORPTION));
        assertFalse(teamBlue2.hasPotionEffect(PotionEffectType.ABSORPTION));

        teamBlue1.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 10, 1));

        assertFalse(teamRed1.hasPotionEffect(PotionEffectType.REGENERATION));
        assertFalse(teamRed2.hasPotionEffect(PotionEffectType.REGENERATION));
        assertTrue(teamBlue1.hasPotionEffect(PotionEffectType.REGENERATION));
        assertFalse(teamBlue2.hasPotionEffect(PotionEffectType.REGENERATION));

        teamBlue1.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 10, 1));

        assertFalse(teamRed1.hasPotionEffect(PotionEffectType.JUMP_BOOST));
        assertFalse(teamRed2.hasPotionEffect(PotionEffectType.JUMP_BOOST));
        assertTrue(teamBlue1.hasPotionEffect(PotionEffectType.JUMP_BOOST));
        assertFalse(teamBlue2.hasPotionEffect(PotionEffectType.JUMP_BOOST));
    }

//    can't get this working yet
//    @Test
    @DisplayName("When start is ran, and shared inventory is enabled, they share their inventory")
    void sharedInventoryWorksAsExpectedWhenEnabled() {
        BukkitSchedulerMock schedule = server.getScheduler();

        PlayerMock admin = server.addPlayer();
        admin.setOp(true);

        final PlayerMock teamRed1 = server.addPlayer("stefan");
        final PlayerMock teamRed2 = server.addPlayer("jawad");
        final PlayerMock teamBlue1 = server.addPlayer("shurf");
        final PlayerMock teamBlue2 = server.addPlayer("sean");

        TestUtils.executeCommand(plugin, admin, "set",
                "team.red=stefan,jawad",
                "team.blue=shurf,sean",
                "enable.shared.team.inventory=true"
        );

        TestUtils.executeCommand(plugin, admin, "start");

        schedule.performTicks(1000);

        assertThatSharedHealthIsNotActivated(teamRed1, teamRed2, teamBlue1, teamBlue2);

        Item item = world.dropItem(
                teamRed1.getLocation(),
                new ItemStack(Material.DIAMOND)
        );

        EntityPickupItemEvent event = new EntityPickupItemEvent(
                teamRed1,
                item,
                0
        );

        server.getPluginManager().callEvent(event);

        System.out.println("Player inventory: " +
                teamRed1.getInventory().contains(Material.DIAMOND));

        schedule.performTicks(100);

        assertInventoriesEqual(teamRed1, teamRed2);
        assertInventoriesNotEqual(teamRed1, teamBlue1);
        assertInventoriesNotEqual(teamRed1, teamBlue2);
        assertInventoriesNotEqual(teamRed2, teamBlue1);
        assertInventoriesNotEqual(teamRed2, teamBlue2);

        TestUtils.executeCommand(plugin, admin, "cancel");
    }

    private void assertInventoriesEqual(final PlayerMock player1, final PlayerMock player2) {
        assertEquals(Arrays.stream(player1.getInventory().getContents()).filter(Objects::nonNull).map(i -> i.getType()).toList(), Arrays.stream(player2.getInventory().getContents()).filter(Objects::nonNull).map(i -> i.getType()).toList());
    }

    private void assertInventoriesNotEqual(final PlayerMock player1, final PlayerMock player2) {
        assertNotEquals(Arrays.stream(player1.getInventory().getContents()).filter(Objects::nonNull).map(i -> i.getType()).toList(), Arrays.stream(player2.getInventory().getContents()).filter(Objects::nonNull).map(i -> i.getType()).toList());
    }
    //    can't get this working yet
//    @Test
    @DisplayName("When start is ran, and shared health is disabled, they do not share the damage")
    void sharedHealthWorksAsExpectedWhenDisabled() throws InterruptedException {
        BukkitSchedulerMock schedule = server.getScheduler();

        PlayerMock admin = server.addPlayer();
        admin.setOp(true);

        final PlayerMock teamRed1 = server.addPlayer("stefan");
        final PlayerMock teamRed2 = server.addPlayer("jawad");
        final PlayerMock teamBlue1 = server.addPlayer("shurf");
        final PlayerMock teamBlue2 = server.addPlayer("sean");

        TestUtils.executeCommand(plugin, admin, "set",
                "team.red=stefan,jawad",
                "team.blue=shurf,sean",
                "enable.shared.team.health=false",
                "shared.team.health=50"
        );

        TestUtils.executeCommand(plugin, admin, "start");

        schedule.performTicks(1000);

        assertThatSharedHealthIsNotActivated(teamRed1, teamRed2, teamBlue1, teamBlue2);

        TestUtils.executeCommand(plugin, admin, "cancel");
    }
}
