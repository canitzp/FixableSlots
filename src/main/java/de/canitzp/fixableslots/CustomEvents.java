package de.canitzp.fixableslots;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;

public class CustomEvents {

    public static final Event<PlayerLoggedIn> PLAYER_LOGGED_IN_EVENT = EventFactory.createArrayBacked(PlayerLoggedIn.class, callbacks -> (connection, player) -> {
        for (PlayerLoggedIn callback : callbacks) {
            callback.onPlayerLoggedIn(connection, player);
        }
    });

    public interface PlayerLoggedIn {
        void onPlayerLoggedIn(Connection connection, ServerPlayer player);
    }

}
