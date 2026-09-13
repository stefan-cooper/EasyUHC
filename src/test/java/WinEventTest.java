import mocks.servers.RespawnPlayerServerMock;
import mocks.types.RespawnPlayerMock;
import net.kyori.adventure.text.format.NamedTextColor;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.scheduler.BukkitSchedulerMock;
import com.stefancooper.EasyUHC.Plugin;
import org.bukkit.*;
import org.junit.jupiter.api.*;
import utils.TestUtils;

import static com.stefancooper.EasyUHC.Defaults.*;
import static org.junit.jupiter.api.Assertions.*;


public class WinEventTest {

    private static RespawnPlayerServerMock server;
    private static Plugin plugin;
    private static World world;
    private static World nether;
    private static World end;

    @BeforeAll
    public static void load()
    {
        server = MockBukkit.mock(new RespawnPlayerServerMock());
        plugin = MockBukkit.load(Plugin.class);
        world = server.getWorld(WORLD_NAME);
        nether = server.getWorld(NETHER_WORLD_NAME);
        end = server.getWorld(END_WORLD_NAME);
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
    @DisplayName("When a team wins UHC, everyone sees title")
    void titleSent() {

        BukkitSchedulerMock schedule = server.getScheduler();

        RespawnPlayerMock winner = server.addPlayer("almer");
        RespawnPlayerMock loser1 = server.addPlayer("po");
        RespawnPlayerMock loser2 = server.addPlayer("lozz");

        PlayerMock admin = server.addPlayer();
        admin.setOp(true);

        int newX = 870;
        int newY = 64;
        int newZ = 7586;

        TestUtils.executeCommand(plugin, admin, "set",
                "world.border.initial.size=50",
                "world.border.final.size=10",
                "countdown.timer.length=5",
                "grace.period.timer=5",
                "world.border.grace.period=10",
                "world.border.shrinking.period=30",
                "difficulty=HARD",
                "team.red=almer",
                "team.yellow=po",
                "team.blue=lozz",
                String.format("world.spawn.x=%s", newX),
                String.format("world.spawn.y=%s", newY),
                String.format("world.spawn.z=%s", newZ)
        );

        assertNull(winner.nextTitle());
        assertNull(loser1.nextTitle());
        assertNull(loser2.nextTitle());

        TestUtils.executeCommand(plugin, admin, "start");

        schedule.performTicks(200);

        for (int titleCount = 0; titleCount < 8; titleCount++) {
            // 5, 4, 3, 2, 1, Start, End Grace Period
            winner.nextTitle();
            loser1.nextTitle();
            loser2.nextTitle();
        }

        assertNull(winner.nextTitle());
        assertNull(loser1.nextTitle());
        assertNull(loser2.nextTitle());

        loser1.damage(20);

        assertNull(winner.nextTitle());
        assertNull(loser1.nextTitle());
        assertNull(loser2.nextTitle());

        assertNotEquals(new Location(world, newX, newY, newZ), winner.getLocation());

        loser2.damage(20);

        assertEquals(NamedTextColor.GOLD + "Congratulations to Team Red!", winner.nextTitle());
        assertEquals(NamedTextColor.GOLD + "Congratulations to Team Red!", loser1.nextTitle());
        assertEquals(NamedTextColor.GOLD + "Congratulations to Team Red!", loser2.nextTitle());

        schedule.performTicks(100);

        // winner should still be in same location after winning
        assertNotEquals(new Location(world, newX, newY, newZ), winner.getLocation());

    }

    @Test
    @DisplayName("When a team wins UHC (3 players per team), everyone sees title")
    void titleSentManyMen() {

        BukkitSchedulerMock schedule = server.getScheduler();

        PlayerMock redWinner1 = server.addPlayer("stefan");
        PlayerMock redWinner2 = server.addPlayer("jawad");
        PlayerMock redWinner3 = server.addPlayer("reece");
        PlayerMock blueLoser1 = server.addPlayer("pavey");
        PlayerMock blueLoser2 = server.addPlayer("sean");
        PlayerMock blueLoser3 = server.addPlayer("ben");
        PlayerMock yellowLoser1 = server.addPlayer("luke");
        PlayerMock yellowLoser2 = server.addPlayer("arbaaz");
        PlayerMock yellowLoser3 = server.addPlayer("shurf");

        PlayerMock admin = server.addPlayer();
        admin.setOp(true);

        int newX = 870;
        int newY = 64;
        int newZ = 7586;

        TestUtils.executeCommand(plugin, admin, "set",
                "world.border.initial.size=50",
                "world.border.final.size=10",
                "countdown.timer.length=5",
                "grace.period.timer=5",
                "world.border.grace.period=10",
                "world.border.shrinking.period=30",
                "difficulty=HARD",
                "team.red=reece,stefan,jawad",
                "team.blue=pavey,sean,ben",
                "team.yellow=luke,arbaaz,shurf",
                String.format("world.spawn.x=%s", newX),
                String.format("world.spawn.y=%s", newY),
                String.format("world.spawn.z=%s", newZ)
        );

        assertNull(redWinner1.nextTitle());
        assertNull(redWinner2.nextTitle());
        assertNull(redWinner3.nextTitle());
        assertNull(blueLoser1.nextTitle());
        assertNull(blueLoser2.nextTitle());
        assertNull(blueLoser3.nextTitle());
        assertNull(yellowLoser1.nextTitle());
        assertNull(yellowLoser2.nextTitle());
        assertNull(yellowLoser3.nextTitle());

        TestUtils.executeCommand(plugin, admin, "start");

        schedule.performTicks(200);

        for (int titleCount = 0; titleCount < 9; titleCount++) {
            // 5, 4, 3, 2, 1, Start, End Grace Period
            redWinner1.nextTitle();
            redWinner2.nextTitle();
            redWinner3.nextTitle();
            blueLoser1.nextTitle();
            blueLoser2.nextTitle();
            blueLoser3.nextTitle();
            yellowLoser1.nextTitle();
            yellowLoser2.nextTitle();
            yellowLoser3.nextTitle();
        }

        blueLoser1.damage(20);

        assertNull(redWinner1.nextTitle());
        assertNull(redWinner2.nextTitle());
        assertNull(redWinner3.nextTitle());
        assertNull(blueLoser1.nextTitle());
        assertNull(blueLoser2.nextTitle());
        assertNull(blueLoser3.nextTitle());
        assertNull(yellowLoser1.nextTitle());
        assertNull(yellowLoser2.nextTitle());
        assertNull(yellowLoser3.nextTitle());

        assertNotEquals(new Location(world, newX, newY, newZ), redWinner1.getLocation());

        blueLoser2.damage(20);
        blueLoser3.damage(20);

        assertNull(redWinner1.nextTitle());
        assertNull(redWinner2.nextTitle());
        assertNull(redWinner3.nextTitle());
        assertNull(blueLoser1.nextTitle());
        assertNull(blueLoser2.nextTitle());
        assertNull(blueLoser3.nextTitle());
        assertNull(yellowLoser1.nextTitle());
        assertNull(yellowLoser2.nextTitle());
        assertNull(yellowLoser3.nextTitle());

        yellowLoser1.damage(20);
        yellowLoser2.damage(20);
        redWinner1.damage(20);

        assertNull(redWinner1.nextTitle());
        assertNull(redWinner2.nextTitle());
        assertNull(redWinner3.nextTitle());
        assertNull(blueLoser1.nextTitle());
        assertNull(blueLoser2.nextTitle());
        assertNull(blueLoser3.nextTitle());
        assertNull(yellowLoser1.nextTitle());
        assertNull(yellowLoser2.nextTitle());
        assertNull(yellowLoser3.nextTitle());

        yellowLoser3.damage(20);

        assertEquals(NamedTextColor.GOLD + "Congratulations to Team Red!", redWinner1.nextTitle());
        assertEquals(NamedTextColor.GOLD + "Congratulations to Team Red!", redWinner2.nextTitle());
        assertEquals(NamedTextColor.GOLD + "Congratulations to Team Red!", redWinner3.nextTitle());
        assertEquals(NamedTextColor.GOLD + "Congratulations to Team Red!", blueLoser1.nextTitle());
        assertEquals(NamedTextColor.GOLD + "Congratulations to Team Red!", blueLoser2.nextTitle());
        assertEquals(NamedTextColor.GOLD + "Congratulations to Team Red!", blueLoser3.nextTitle());
        assertEquals(NamedTextColor.GOLD + "Congratulations to Team Red!", yellowLoser1.nextTitle());
        assertEquals(NamedTextColor.GOLD + "Congratulations to Team Red!", yellowLoser2.nextTitle());
        assertEquals(NamedTextColor.GOLD + "Congratulations to Team Red!", yellowLoser3.nextTitle());

        schedule.performTicks(100);


        // winners should still be in same location after winning
        assertNotEquals(new Location(world, newX, newY, newZ), redWinner2.getLocation());
        assertNotEquals(new Location(world, newX, newY, newZ), redWinner3.getLocation());
    }
}
