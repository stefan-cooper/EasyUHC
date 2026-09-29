import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import com.stefancooper.EasyUHC.Plugin;
import org.bukkit.World;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import utils.TestUtils;

import java.util.Arrays;

import static com.stefancooper.EasyUHC.Defaults.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ViewConfigTest {

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
    @DisplayName("Test view full config")
    void testPlayerGetConfig() {
        final BookMeta bookMeta = Mockito.mock(BookMeta.class);

        final ArgumentCaptor<Component[]> pagesCaptor =
                ArgumentCaptor.forClass(Component[].class);

        final  PlayerMock player = server.addPlayer();
        player.setOp(true);

        try (final MockedConstruction<ItemStack> mocked =
                     Mockito.mockConstruction(
                             ItemStack.class,
                             (mock, context) -> {
                                 when(mock.getItemMeta()).thenReturn(bookMeta);
                             })) {

            TestUtils.executeCommand(plugin, player, "view", "config");
            verify(bookMeta).addPages(pagesCaptor.capture());
            final Component[] pages = pagesCaptor.getValue();
            assertEquals(21, pages.length);
        }



//        player.assertSaid(String.format("""
//                        countdown.timer.length=%s
//                        difficulty=%s
//                        disable.debug.info=%s
//                        enable.performance.tracking=%s
//                        enable.timestamps=%s
//                        end.world.name=%s
//                        grace.period.timer=%s
//                        nether.world.name=%s
//                        on.death.action=%s
//                        world.border.center.x=%s
//                        world.border.center.z=%s
//                        world.border.final.size=%s
//                        world.border.grace.period=%s
//                        world.border.initial.size=%s
//                        world.border.shrinking.period=%s
//                        world.name=%s
//                        """, COUNTDOWN_TIMER_LENGTH, DIFFICULTY, DISABLE_DEBUG_INFO, ENABLE_PERFORMANCE_TRACKING, ENABLE_TIMESTAMPS, END_WORLD_NAME, GRACE_PERIOD_TIMER, NETHER_WORLD_NAME, ON_DEATH_ACTION, WORLD_BORDER_CENTER_X, WORLD_BORDER_CENTER_Z,
//                WORLD_BORDER_FINAL_SIZE, WORLD_BORDER_GRACE_PERIOD, WORLD_BORDER_INITIAL_SIZE, WORLD_BORDER_SHRINKING_PERIOD, WORLD_NAME
//                        )
//        );
    }

    @Test
    @DisplayName("Test view initial world border command")
    void testPlayerGetInitialWorldBorderSize() {
        PlayerMock player = server.addPlayer();
        player.setOp(true);
        TestUtils.executeCommand(plugin, player, "view", "world.border.initial.size");
        player.assertSaid("world.border.initial.size=" + WORLD_BORDER_INITIAL_SIZE);
    }

    @Test
    @DisplayName("Test view world border center x")
    void testPlayerGetWorldBorderCenterX() {
        PlayerMock player = server.addPlayer();
        player.setOp(true);
        TestUtils.executeCommand(plugin, player, "view", "world.border.center.x");
        player.assertSaid("world.border.center.x=" + WORLD_BORDER_CENTER_X);
    }

    @Test
    @DisplayName("Test view world border center z")
    void testPlayerGetWorldBorderCenterZ() {
        PlayerMock player = server.addPlayer();
        player.setOp(true);
        TestUtils.executeCommand(plugin, player, "view", "world.border.center.z");
        player.assertSaid("world.border.center.z=" + WORLD_BORDER_CENTER_Z);
    }


}
