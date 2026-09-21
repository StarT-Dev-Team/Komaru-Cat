package net.theiadraizer.komarumod;

import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.theiadraizer.komarumod.client.renderer.entity.KomaruRenderer;
import net.theiadraizer.komarumod.registry.KomaruEntityTypes;
import net.theiadraizer.komarumod.registry.KomaruItems;
import net.theiadraizer.komarumod.registry.KomaruPaintingVariants;
import org.slf4j.Logger;
import software.bernie.geckolib.GeckoLib;

@Mod(KomaruMod.MOD_ID)
public class KomaruMod {
    public static final String MOD_ID = "komarumod";
    private static final Logger LOGGER = LogUtils.getLogger();

    public KomaruMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        KomaruItems.register(modEventBus);
        KomaruEntityTypes.register(modEventBus);
        KomaruPaintingVariants.register(modEventBus);
        GeckoLib.initialize();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Mod.EventBusSubscriber(
            modid = MOD_ID,
            bus = Mod.EventBusSubscriber.Bus.MOD,
            value = Dist.CLIENT
    )
    public static class ClientModEvents {
        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(KomaruEntityTypes.KOMARU.get(), KomaruRenderer::new);
        }
    }
}
