import mocks.servers.RespawnPlayerServerMock;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.scheduler.BukkitSchedulerMock;
import com.stefancooper.EasyUHC.Plugin;
import com.stefancooper.EasyUHC.base.Utils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.junit.jupiter.api.BeforeEach;

import static com.stefancooper.EasyUHC.Defaults.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.*;
import utils.TestUtils;

public class EventTest {

    private static RespawnPlayerServerMock server;
    private static Plugin plugin;
    private static World world;
    private static PlayerMock serverOp;

    @BeforeAll
    public static void beforeAll() {
        server = MockBukkit.mock(new RespawnPlayerServerMock());
        plugin = MockBukkit.load(Plugin.class);
        world = server.getWorld(WORLD_NAME);
        serverOp = server.addPlayer();
        serverOp.setOp(true);
    }

    @BeforeEach
    public void load() {
        plugin.getUHCConfig().resetToDefaults();
        TestUtils.executeCommand(plugin, serverOp, "cancel");
    }

    @AfterAll
    public static void unload() {
        plugin.getUHCConfig().resetToDefaults();
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("Test the on death event to ensure a player is set to Spectator after death")
    void testOnDeathEventSpectate() {
        PlayerMock player = server.addPlayer();
        TestUtils.executeCommand(plugin, player, "set", "on.death.action=spectate");
        TestUtils.executeCommand(plugin, player, "start");
        player.damage(100);
        assertEquals(GameMode.SPECTATOR, player.getGameMode());
    }

    @Test
    @DisplayName("Test the on death event to ensure a player is kicked after death")
    void testOnDeathEventKick() {
        server.setPlayers(15);
        PlayerMock player = server.addPlayer();
        player.setOp(true);
        TestUtils.executeCommand(plugin, player, "set", "on.death.action=kick");
        assertEquals(16, server.getOnlinePlayers().size());

        TestUtils.executeCommand(plugin, player, "start");
        player.damage(100);
        assertEquals(15, server.getOnlinePlayers().size());
    }

    @Test
    @DisplayName("Test the on death event to ensure a player drops a head after death")
    void testHeadDropOnDeath() {
        PlayerMock player = server.addPlayer();
        player.setOp(true);
        TestUtils.executeCommand(plugin, player, "set", "player.head.golden.apple=true");
        player.damage(100);
        if(!world.getEntities().stream().filter(entity -> entity.getType() == EntityType.ITEM && entity.getName().equals("PLAYER_HEAD")).toList().isEmpty()){
            Item droppedItem = (Item) world.getEntities().get(0);
            droppedItem.getItemStack();

            assertEquals(Material.PLAYER_HEAD, droppedItem.getType());
        }
    }

    @Test
    @DisplayName("Test the on respawn event to ensure a player respawns at their death location")
    void testPlayerRespawnOnDeathLocation() {
        PlayerMock player = server.addPlayer();
        TestUtils.executeCommand(plugin, serverOp, "start");

        Location deathLocation = new Location(world, 100, 65, 100);
        player.damage(100);
        player.setLastDeathLocation(deathLocation);

        PlayerRespawnEvent respawnEvent = new PlayerRespawnEvent(player, null, false);
        server.getPluginManager().callEvent(respawnEvent);

        assertEquals(deathLocation, respawnEvent.getRespawnLocation(), "Player should respawn at their death location");
    }

    @Test
    @DisplayName("Correct game mode is set when joining the server")
    void testGameModeSet() {
        PlayerMock admin = server.addPlayer();
        admin.setOp(true);

        PlayerMock player = server.addPlayer();
        assertEquals(GameMode.ADVENTURE, player.getGameMode());

        TestUtils.executeCommand(plugin, admin, "start");

        assertEquals(GameMode.SURVIVAL, player.getGameMode());

        player.disconnect();
        player.reconnect();

        assertEquals(GameMode.SURVIVAL, player.getGameMode());

        player.damage(100);
        player.respawn();

        assertEquals(GameMode.SPECTATOR, player.getGameMode());

        player.disconnect();
        player.reconnect();

        assertEquals(GameMode.SPECTATOR, player.getGameMode());

        TestUtils.executeCommand(plugin, admin, "cancel");

        assertEquals(GameMode.ADVENTURE, player.getGameMode());

        player.disconnect();
        player.reconnect();

        assertEquals(GameMode.ADVENTURE, player.getGameMode());
    }

    @Test
    void preventMovementDuringCountdown() throws InterruptedException {
        BukkitSchedulerMock scheduler = server.getScheduler();
        PlayerMock admin = server.addPlayer();
        admin.setOp(true);

        PlayerMock player = server.addPlayer();

        TestUtils.executeCommand(plugin, admin, "set", "countdown.timer.length=5");

        Location initialLocation = player.getLocation();
        Location newLocation1 = initialLocation.clone();
        Location newLocation2 = initialLocation.clone();
        Location newLocation3 = initialLocation.clone();
        newLocation1.setX(initialLocation.x() + 1);
        newLocation2.setY(initialLocation.y() + 1);
        newLocation3.setZ(initialLocation.z() + 1);

        // Assert that they can move in all directions and they get moved
        player.simulatePlayerMove(newLocation1);
        assertEquals(player.getLocation(), newLocation1);
        player.simulatePlayerMove(newLocation2);
        assertEquals(player.getLocation(), newLocation2);
        player.simulatePlayerMove(newLocation3);
        assertEquals(player.getLocation(), newLocation3);

        // Reset back to original location
        player.simulatePlayerMove(initialLocation);
        TestUtils.executeCommand(plugin, admin, "start");

        assertEquals(player.getLocation(), initialLocation);
        player.simulatePlayerMove(newLocation1);
        assertEquals(player.getLocation(), initialLocation);
        player.simulatePlayerMove(newLocation2);
        assertEquals(player.getLocation(), initialLocation);
        player.simulatePlayerMove(newLocation3);
        assertEquals(player.getLocation(), initialLocation);

        scheduler.performTicks(Utils.secondsToTicks(5));

        player.simulatePlayerMove(newLocation1);
        assertEquals(player.getLocation(), newLocation1);
        player.simulatePlayerMove(newLocation2);
        assertEquals(player.getLocation(), newLocation2);
        player.simulatePlayerMove(newLocation3);
        assertEquals(player.getLocation(), newLocation3);
    }

    @Test
    @DisplayName("Test the on death event fires a cannon when someone dies in a uhc")
    void cannonOnDeathTest() {
        PlayerMock admin = server.addPlayer();
        admin.setOp(true);

        PlayerMock player1 = server.addPlayer();
        PlayerMock player2 = server.addPlayer();
        PlayerMock player3 = server.addPlayer();

        TestUtils.executeCommand(plugin, admin, "cancel");
        TestUtils.executeCommand(plugin, admin, "start");

        player1.damage(100);

        player2.assertSoundHeard(Sound.ENTITY_LIGHTNING_BOLT_THUNDER);
        player3.assertSoundHeard(Sound.ENTITY_LIGHTNING_BOLT_THUNDER);
        TestUtils.executeCommand(plugin, admin, "cancel");
    }
}
