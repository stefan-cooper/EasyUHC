import mocks.servers.RespawnPlayerServerMock;
import mocks.types.RespawnPlayerMock;
import org.junit.jupiter.api.AfterEach;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import com.stefancooper.EasyUHC.Plugin;
import com.stefancooper.EasyUHC.base.Utils;
import org.bukkit.World;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import utils.TestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.stefancooper.EasyUHC.Defaults.WORLD_NAME;

/**
 * Message for later me - If you've come here looking for tests related to testing timestamps
 *                        for achievements/advancements, they do not exist. There was an attempt
 *                        to write these but MockBukkit as of today (16/04/25) does not support
 *                        anything related to advancements in their API. Unless that changes or
 *                        we contribute it ourselves, this will have to remain untested.
 */

public class TimestampTest {

    private static RespawnPlayerServerMock server;
    private static Plugin plugin;
    private static World world;
    private static PlayerMock admin;

    @BeforeEach
    public void setup() {
        server = MockBukkit.mock(new RespawnPlayerServerMock());
        plugin = MockBukkit.load(Plugin.class);
        world = server.getWorld(WORLD_NAME);
        admin = server.addPlayer();
        admin.setOp(true);
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    private void assertFileContainsText(String match, boolean shouldFind) throws IOException {
        List<String> contents = Files.readAllLines(Path.of(Utils.getResourceLocation("timestamps.txt")));
        String allContents = contents.stream().reduce("", (acc, curr) -> String.format("%s\n%s", acc, curr));
        if (shouldFind) {
            Assertions.assertTrue(allContents.contains(match));
        } else {
            Assertions.assertFalse(allContents.contains(match));
        }
    }

    private void assertTotalLines(int size) throws IOException {
        List<String> contents = Files.readAllLines(Path.of(Utils.getResourceLocation("timestamps.txt")));
        Assertions.assertEquals(size, contents.size());
    }

    @Test
    @DisplayName("When start is run, a timestamp is added")
    void startTimestamp() throws IOException {
        TestUtils.executeCommand(plugin, admin, "set", "enable.timestamps=true");
        TestUtils.executeCommand(plugin, admin, "start");
        assertFileContainsText("UHC Started", true);
        assertTotalLines(1);
    }

    @Test
    @DisplayName("When start is run, and a player dies, the timestamps is updated")
    void deathTimestamp() throws IOException {
        RespawnPlayerMock player1 = server.addPlayer("jawad");
        RespawnPlayerMock player2 = server.addPlayer("stefan");
        RespawnPlayerMock player3 = server.addPlayer("sean");
        player1.setName("jawad");
        player1.setDisplayName("jawad");
        player2.setName("stefan");
        player2.setDisplayName("stefan");
        player3.setName("sean");
        player3.setDisplayName("sean");

        TestUtils.executeCommand(plugin, admin, "set", "enable.timestamps=true");
        TestUtils.executeCommand(plugin, admin, "start");

        player1.damage(100);

        assertFileContainsText("jawad dies | Death message: \"jawad got killed\"", true);
        assertTotalLines(2); // uhc started and stefan dies

        player3.simulateDamage(100, player2);

        assertFileContainsText("sean killed by stefan | Death message: \"sean was slain by stefan\"", true);
        assertTotalLines(3); // uhc started and stefan dies

    }

    @Test
    @DisplayName("When start is run, a timestamp is added. When start is run again, the old timestamps are wiped.")
    void timestampsResetOnStart() throws IOException {
        RespawnPlayerMock player = server.addPlayer("stefan");
        player.setDisplayName("stefan");
        player.setName("stefan");

        TestUtils.executeCommand(plugin, admin, "set", "enable.timestamps=true");
        TestUtils.executeCommand(plugin, admin, "start");

        assertFileContainsText("UHC Started", true);
        assertTotalLines(1);

        player.damage(100);

        assertFileContainsText("stefan dies", true);
        assertTotalLines(2); // uhc started and stefan dies

        TestUtils.executeCommand(plugin, admin, "start");

        assertFileContainsText("UHC Started", true);
        assertFileContainsText("stefan dies", false);
        assertTotalLines(1);
    }
}
