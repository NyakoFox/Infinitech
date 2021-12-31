package gay.nyako.infinitech;

import gay.nyako.infinitech.block.fluid_tank.FluidTankModel;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelProviderException;
import net.fabricmc.fabric.api.client.model.ModelResourceProvider;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class InfinitechModelProvider implements ModelResourceProvider {
    public static final FluidTankModel FLUID_TANK_MODEL = new FluidTankModel();
    public static final Identifier FLUID_TANK_MODEL_BLOCK = new Identifier(InfinitechMod.MOD_ID, "block/fluid_tank");
    public static final Identifier FLUID_TANK_MODEL_ITEM = new Identifier(InfinitechMod.MOD_ID, "item/fluid_tank");

    @Override
    public @Nullable
    UnbakedModel loadModelResource(Identifier resourceId, ModelProviderContext context) throws ModelProviderException {
        if (resourceId.equals(FLUID_TANK_MODEL_BLOCK)) {
            return FLUID_TANK_MODEL;
        } else {
            return null;
        }
    }
}
