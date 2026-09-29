package mocks.servers;

import org.mockbukkit.mockbukkit.AsyncCatcher;
import org.mockbukkit.mockbukkit.ServerMock;
import mocks.types.RespawnPlayerMock;
import org.jetbrains.annotations.NotNull;
import java.util.UUID;

public class RespawnPlayerServerMock extends ServerMock  {

    @Override
    public @NotNull RespawnPlayerMock addPlayer()
    {
        AsyncCatcher.catchOp("player add");
        RespawnPlayerMock player = new RespawnPlayerMock(this, UUID.randomUUID().toString(), UUID.randomUUID());
        addPlayer(player);
        return player;
    }

    @Override
    public @NotNull RespawnPlayerMock addPlayer(@NotNull String name)
    {
        AsyncCatcher.catchOp("player add");
        RespawnPlayerMock player = new RespawnPlayerMock(this, name, UUID.randomUUID());
        addPlayer(player);
        return player;
    }
}
