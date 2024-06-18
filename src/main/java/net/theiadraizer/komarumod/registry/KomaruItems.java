package net.theiadraizer.komarumod.registry;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.theiadraizer.komarumod.KomaruMod;

@Mod.EventBusSubscriber(
        modid = KomaruMod.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public class KomaruItems {
    public static final DeferredRegister<Item> ITEMS;
    public static final RegistryObject<Item> KOMARU_POWDER;
    public static final RegistryObject<Item> KOMARU_SPAWN_EGG;

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    static {
        ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "komarumod");
        KOMARU_POWDER = ITEMS.register("komaru_powder", () -> new Item((new Item.Properties()))); //.tab(CreativeModeTab.TAB_MISC));
        KOMARU_SPAWN_EGG = ITEMS.register("komaru_spawn_egg", () -> new ForgeSpawnEggItem(KomaruEntityTypes.KOMARU, 2274113, 1667886, new Item.Properties()));
    }

    @SubscribeEvent
    public static void onCreativeTabBuild(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(KOMARU_SPAWN_EGG);
        }

        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(KOMARU_POWDER);
        }
    }
}
