package net.theiadraizer.komarumod.events;

import net.minecraft.world.entity.EntityType;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.theiadraizer.komarumod.entity.ModEntityTypes;
import net.theiadraizer.komarumod.entity.custom.KomaruEntity;

public class ModEvents {
    public ModEvents() {
    }

    @EventBusSubscriber(
        modid = "komarumod"
    )
    public static class ForgeEvents {
        public ForgeEvents() {
        }

        @EventBusSubscriber(
            modid = "komarumod",
            bus = Bus.MOD
        )
        public static class ModEventBusEvents {
            public ModEventBusEvents() {
            }

            @SubscribeEvent
            public static void entityAttributeEvent(EntityAttributeCreationEvent event) {
                event.put((EntityType)ModEntityTypes.KOMARU.get(), KomaruEntity.setAttributes());
            }
        }
    }
}
