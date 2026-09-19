import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.inventory.InventoryMock;
import com.stefancooper.EasyUHC.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.scoreboard.Scoreboard;
import org.junit.jupiter.api.*;
import utils.TestUtils;

import static com.stefancooper.EasyUHC.Defaults.WORLD_NAME;
import static com.stefancooper.EasyUHC.base.Constants.PLAYER_HEAD;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SetConfigTest {

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
    @DisplayName("Test set world border center x")
    void testPlayerSetWorldBorderCenterX() {
        PlayerMock player = server.addPlayer();
        player.setOp(true);
//        BukkitSchedulerMock schedule = server.getScheduler();
//        schedule.performOneTick();
//        server.getCommandMap().register("uhc", new UHCCommand(plugin.getUHCConfig()));
//        server.dispatchCommand(player, "uhc set world.border.center.x=25");
        TestUtils.executeCommand(plugin, player, "set", "world.border.center.x=25");
        Assertions.assertEquals(Double.parseDouble("25"), world.getWorldBorder().getCenter().getX());
    }

    @Test
    @DisplayName("Test set world border center z")
    void testPlayerSetWorldBorderCenterZ() {
        PlayerMock player = server.addPlayer();
        player.setOp(true);
        TestUtils.executeCommand(plugin, player, "set", "world.border.center.z=25");
        Assertions.assertEquals(Double.parseDouble("25"), world.getWorldBorder().getCenter().getZ());
    }

    @Test
    @DisplayName("Test set teams test")
    void testPlayerSetTeams() {
        PlayerMock admin = server.addPlayer();
        admin.setOp(true);

        PlayerMock stefan = server.addPlayer();
        stefan.setName("stefan");
        PlayerMock jawad = server.addPlayer();
        jawad.setName("jawad");
        PlayerMock shurf = server.addPlayer();
        shurf.setName("shurf");
        PlayerMock sean = server.addPlayer();
        sean.setName("sean");
        PlayerMock pavey = server.addPlayer();
        pavey.setName("pavey");
        PlayerMock luke = server.addPlayer();
        luke.setName("luke");
        PlayerMock tim = server.addPlayer();
        tim.setName("tim");

        TestUtils.executeCommand(plugin, admin, "set", "team.red=stefan,jawad");
        TestUtils.executeCommand(plugin, admin, "set", "team.blue=shurf");
        TestUtils.executeCommand(plugin, admin, "set", "team.green=sean");
        TestUtils.executeCommand(plugin, admin, "set", "team.orange=pavey");
        TestUtils.executeCommand(plugin, admin, "set", "team.pink=luke");
        TestUtils.executeCommand(plugin, admin, "set", "team.purple=tim");

        Scoreboard scoreboard = admin.getScoreboard();

        assertTrue(scoreboard.getEntityTeam(stefan).getName().equals("Red"));
        assertTrue(scoreboard.getEntityTeam(jawad).getName().equals("Red"));
        assertTrue(scoreboard.getEntityTeam(shurf).getName().equals("Blue"));
        assertTrue(scoreboard.getEntityTeam(sean).getName().equals("Green"));
        assertTrue(scoreboard.getEntityTeam(pavey).getName().equals("Orange"));
        assertTrue(scoreboard.getEntityTeam(luke).getName().equals("Pink"));
        assertTrue(scoreboard.getEntityTeam(tim).getName().equals("Purple"));

        TestUtils.executeCommand(plugin, admin, "set", "team.yellow=jawad");
        assertTrue(scoreboard.getEntityTeam(stefan).getName().equals("Red"));
        assertTrue(scoreboard.getEntityTeam(jawad).getName().equals("Yellow"));
    }

    @Test
    @DisplayName("Test that the recipe for creating a golden apple from a player head works")
    void testPlayerCanCraftGoldenAppleFromPlayerHead() {
        PlayerMock player = server.addPlayer();
        player.setOp(true);
        TestUtils.executeCommand(plugin, player, "set", "player.head.golden.apple=true");
        ItemStack apple = new ItemStack(Material.GOLDEN_APPLE);
        NamespacedKey key = new NamespacedKey(plugin, PLAYER_HEAD);
        ShapedRecipe recipe = (ShapedRecipe) Bukkit.getRecipe(key);
        Assertions.assertNotNull(recipe, "Recipe should be registered.");

        // Simulate crafting inventory (3x3 grid with player head in the center (center = 4))
        InventoryMock craftingInventory = server.createInventory(null, 9, "Crafting");
        craftingInventory.setItem(4, new ItemStack(Material.PLAYER_HEAD));

        assertTrue(isMatchingRecipe(craftingInventory, recipe), "Inventory should match the recipe.");
        Assertions.assertEquals(apple.getType(), recipe.getResult().getType(), "Crafted item should be a golden apple.");
    }

    // Helper method to check if crafting grid matches the recipe
    private boolean isMatchingRecipe(InventoryMock inventory, ShapedRecipe recipe) {
        String[] shape = recipe.getShape();                                         // Get the recipe's shape (array of strings where each row is a line of the crafting grid)
        for (int row = 0; row < shape.length; row++) {                              // Loop through each row in the recipe's shape
            for (int col = 0; col < shape[row].length(); col++) {                   // Loop through each character (ingredient) in the current row
                int slotIndex = row * 3 + col;                                      // Convert 2D grid to flat index
                char ingredientChar = shape[row].charAt(col);                       // Get the ingredient character from the recipe's shape (e.g., 'X' for PLAYER_HEAD)
                ItemStack expected = recipe.getIngredientMap().get(ingredientChar); // Get the expected ItemStack for this ingredient character (e.g., Material.PLAYER_HEAD)
                ItemStack actual = inventory.getItem(slotIndex);                    // Get the actual ItemStack from the simulated crafting grid (inventory)

                if (ingredientChar == ' ' && actual != null) return false;          // Check if the recipe expects an empty slot but the inventory has an item there
                if (expected != null && (actual == null || expected.getType() != actual.getType())) return false; // Check if the recipe expects an ingredient but the actual inventory slot is null or doesn't match
            }
        }
        return true;
    }
}
