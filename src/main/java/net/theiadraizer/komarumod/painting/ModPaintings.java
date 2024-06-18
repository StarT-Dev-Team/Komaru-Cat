package net.theiadraizer.komarumod.painting;

import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModPaintings {
    public static final DeferredRegister<PaintingVariant> PAINTING_VARIANTS;
    public static final RegistryObject<PaintingVariant> MEDUSA;
    public static final RegistryObject<PaintingVariant> KOMARU_COMPRESSED;
    public static final RegistryObject<PaintingVariant> KOMARU_CHONKY;
    public static final RegistryObject<PaintingVariant> KOMARU_TONGUE;
    public static final RegistryObject<PaintingVariant> KOMARU_HAT;
    public static final RegistryObject<PaintingVariant> KOMARU_SLEEP;
    public static final RegistryObject<PaintingVariant> KOMARU_GOD;
    public static final RegistryObject<PaintingVariant> KOMARU_FACE;

    public ModPaintings() {
    }

    public static void register(IEventBus eventBus) {
        PAINTING_VARIANTS.register(eventBus);
    }

    static {
        PAINTING_VARIANTS = DeferredRegister.create(ForgeRegistries.PAINTING_VARIANTS, "komarumod");
        MEDUSA = PAINTING_VARIANTS.register("medusa", () -> new PaintingVariant(64, 64));
        KOMARU_COMPRESSED = PAINTING_VARIANTS.register("komaru_compressed", () -> new PaintingVariant(16, 32));
        KOMARU_CHONKY = PAINTING_VARIANTS.register("komaru_chonky", () -> new PaintingVariant(64, 64));
        KOMARU_TONGUE = PAINTING_VARIANTS.register("komaru_dress", () -> new PaintingVariant(16, 16));
        KOMARU_HAT = PAINTING_VARIANTS.register("komaru_hat", () -> new PaintingVariant(16, 32));
        KOMARU_SLEEP = PAINTING_VARIANTS.register("komaru_sleep", () -> new PaintingVariant(32, 32));
        KOMARU_GOD = PAINTING_VARIANTS.register("komaru_god", () -> new PaintingVariant(64, 64));
        KOMARU_FACE = PAINTING_VARIANTS.register("komaru_face", () -> new PaintingVariant(16, 16));
    }
}
