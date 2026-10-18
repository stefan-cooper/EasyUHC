import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.scheduler.BukkitSchedulerMock;
import com.stefancooper.EasyUHC.Plugin;
import java.util.Arrays;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import utils.TestUtils;

import static com.stefancooper.EasyUHC.Defaults.WORLD_NAME;

public class LateStartTest {

    private static ServerMock server;
    private static Plugin plugin;
    private static World world;

    @BeforeAll
    public static void load()
    {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(Plugin.class);
        world = server.getWorld(WORLD_NAME);
    }

    @BeforeEach
    public void cleanUp() {
        plugin.getUHCConfig().resetToDefaults();
    }

    @AfterAll
    public static void unload() {
        plugin.getUHCConfig().resetToDefaults();
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("When late start is run on a late joiner, stats are reset")
    void lateJoiner() {
        PlayerMock admin = server.addPlayer();
        admin.setOp(true);

        // initial player
        PlayerMock player1 = server.addPlayer("stefan");
        player1.setHealth(10);
        player1.giveExp(100);
        player1.setLevel(12);
        player1.getInventory().setItem(1, ItemStack.of(Material.DIAMOND_SWORD));
        player1.setFoodLevel(3);
        player1.setMaxHealth(12);

        world.dropItem(new Location(world, 0, 100, 0), ItemStack.of(Material.DIAMOND_SWORD));

        Assertions.assertEquals(1, world.getEntities().stream().filter(entity -> entity.getType().equals(EntityType.ITEM)).toList().size());

        TestUtils.executeCommand(plugin, admin, "set",
                "team.red=stefan,sean"
        );

        // uhc starts
        TestUtils.executeCommand(plugin, admin, "start", "force");

        Assertions.assertEquals(0, world.getEntities().stream().filter(entity -> entity.getType().equals(EntityType.ITEM)).toList().size());

        // check that active players start as usual
        server.getOnlinePlayers().forEach(player -> {
            Assertions.assertEquals(20.0, player.getHealth());
            Assertions.assertEquals(20.0, player.getFoodLevel());
            Assertions.assertEquals(20, player.getMaxHealth());
            Assertions.assertEquals(0, player.getExp());
            Assertions.assertEquals(0, player.getLevel());
            Assertions.assertEquals(0, Arrays.stream(player.getInventory().getContents()).filter(item -> item != null && item.getType() != Material.AIR).toList().size());
            Assertions.assertEquals(GameMode.SURVIVAL, player.getGameMode());
        });

        // player 1 starts playing etc
        player1.setHealth(10);
        player1.giveExp(100);
        player1.setLevel(12);
        player1.getInventory().setItem(1, ItemStack.of(Material.DIAMOND_SWORD));
        player1.setFoodLevel(3);

        // player 2 joins server late
        PlayerMock player2 = server.addPlayer("sean");
        player2.setHealth(10);
        player2.setLevel(12);
        player2.getInventory().setItem(1, ItemStack.of(Material.DIAMOND_SWORD));
        player2.setFoodLevel(3);

        Assertions.assertEquals(10.0, player2.getHealth());
        Assertions.assertEquals(3.0, player2.getFoodLevel());
        Assertions.assertEquals(12, player2.getLevel());
        Assertions.assertEquals(1, Arrays.stream(player2.getInventory().getContents()).filter(item -> item != null && item.getType() != Material.AIR).toList().size());
        Assertions.assertEquals(GameMode.SURVIVAL, player2.getGameMode());

        // late start is execute for late joiner
        TestUtils.executeCommand(plugin, admin, "latestart", "sean");

        // check that late joiner has the usual stats reset
        Assertions.assertEquals(20.0, player2.getHealth());
        Assertions.assertEquals(20.0, player2.getFoodLevel());
        Assertions.assertEquals(0, player2.getLevel());
        Assertions.assertEquals(0, Arrays.stream(player2.getInventory().getContents()).filter(item -> item != null && item.getType() != Material.AIR).toList().size());
        Assertions.assertEquals(GameMode.SURVIVAL, player2.getGameMode());

        // check that the original player is unaffected
        Assertions.assertEquals(10.0, player1.getHealth());
        Assertions.assertEquals(3.0, player1.getFoodLevel());
        Assertions.assertEquals(12, player1.getLevel());
        Assertions.assertEquals(1, Arrays.stream(player1.getInventory().getContents()).filter(item -> item != null && item.getType() != Material.AIR).toList().size());
        Assertions.assertEquals(GameMode.SURVIVAL, player1.getGameMode());

        TestUtils.executeCommand(plugin, admin, "cancel");
    }

    @Test
    @DisplayName("When late start is run on a late joiner, stats are reset")
    void lateJoinerTeleport() {
        BukkitSchedulerMock schedule = server.getScheduler();
        PlayerMock admin = server.addPlayer();
        admin.setOp(true);

        // initial player
        PlayerMock player1 = server.addPlayer("jawad");
        player1.setLocation(new Location(world, 1250, 100, 1250));

        schedule.performOneTick();

        TestUtils.executeCommand(plugin, admin, "set",
                "team.red=jawad,pavey"
        );

        // player 2 joins server late
        PlayerMock player2 = server.addPlayer("pavey");
        player2.setLocation(new Location(world, 0, 100, 0));

        // late start is execute for late joiner
        TestUtils.executeCommand(plugin, admin, "latestart", "pavey");

        // check that late joiner has the usual stats reset
        Assertions.assertEquals(new Location(world, 1250, 100, 1250), player1.getLocation());
        Assertions.assertEquals(new Location(world, 1250, 100, 1250), player2.getLocation());

        TestUtils.executeCommand(plugin, admin, "cancel");
    }
}
