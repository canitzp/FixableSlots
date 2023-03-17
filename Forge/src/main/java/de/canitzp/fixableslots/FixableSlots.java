package de.canitzp.fixableslots;

import de.canitzp.fixablelsots.FSEvents;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
@Mod("fixableslots")
public class FixableSlots {

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event){
        FSEvents.onWorldLoad((Level) event.getWorld());
    }

    @SubscribeEvent
    private static void onWorldUnload(WorldEvent.Unload event){
        FSEvents.onWorldUnload((Level) event.getWorld());
    }
}
