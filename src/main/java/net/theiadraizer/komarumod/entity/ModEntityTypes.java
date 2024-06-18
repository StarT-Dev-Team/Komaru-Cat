package net.theiadraizer.komarumod.entity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType.Builder;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.theiadraizer.komarumod.entity.custom.KomaruEntity;

public class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES;
    public static final RegistryObject<EntityType<KomaruEntity>> KOMARU;

    public ModEntityTypes() {
    }

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }

    static {
        ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, "komarumod");
        KOMARU = ENTITY_TYPES.register("komaru", () -> Builder
                .of(KomaruEntity::new, MobCategory.CREATURE)
                .sized(0.7F, 0.7F)
                .build((new ResourceLocation("komarumod", "komaru")).toString()));
    }
}
