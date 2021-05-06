package de.canitzp.fixableslots.mixin;

import de.canitzp.fixableslots.CustomEvents;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class PlayerListMixin {

    @Inject(method = "placeNewPlayer", at = @At("TAIL"))
    private void onPlayerLoggedIn(Connection connection, ServerPlayer serverPlayer, CallbackInfo ci){
        CustomEvents.PLAYER_LOGGED_IN_EVENT.invoker().onPlayerLoggedIn(connection, serverPlayer);
    }

}
