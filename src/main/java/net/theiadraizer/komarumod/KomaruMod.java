package net.theiadraizer.komarumod;

import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.theiadraizer.komarumod.entity.ModEntityTypes;
import net.theiadraizer.komarumod.entity.client.KomaruRenderer;
import net.theiadraizer.komarumod.item.ModItems;
import net.theiadraizer.komarumod.painting.ModPaintings;
import org.slf4j.Logger;
import software.bernie.geckolib.GeckoLib;

@Mod("komarumod")
public class KomaruMod {
    public static final String MOD_ID = "komarumod";
    private static final Logger LOGGER = LogUtils.getLogger();

    public KomaruMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.register(modEventBus);
        ModEntityTypes.register(modEventBus);
        ModPaintings.register(modEventBus);
        GeckoLib.initialize();
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            SpawnPlacements.register((EntityType)ModEntityTypes.KOMARU.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules);
        });
    }

    @Mod.EventBusSubscriber(
            modid = "komarumod",
            bus = Mod.EventBusSubscriber.Bus.MOD
    )
    public static class ClientModEvents {
        public ClientModEvents() {
        }

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            EntityRenderers.register((EntityType)ModEntityTypes.KOMARU.get(), KomaruRenderer::new);
        }
    }
}
