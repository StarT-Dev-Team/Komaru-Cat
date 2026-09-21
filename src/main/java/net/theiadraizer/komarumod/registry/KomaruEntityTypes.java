package net.theiadraizer.komarumod.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType.Builder;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.theiadraizer.komarumod.KomaruMod;
import net.theiadraizer.komarumod.entity.KomaruEntity;

public class KomaruEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES;
    public static final RegistryObject<EntityType<KomaruEntity>> KOMARU;

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }

    static {
        ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, KomaruMod.MOD_ID);
        KOMARU = ENTITY_TYPES.register("komaru", () -> Builder
                .of(KomaruEntity::new, MobCategory.CREATURE)
                .sized(0.7F, 0.7F)
                .build(new ResourceLocation(KomaruMod.MOD_ID, "komaru").toString()));
    }
}
