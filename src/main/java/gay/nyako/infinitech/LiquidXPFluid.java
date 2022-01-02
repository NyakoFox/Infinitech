package gay.nyako.infinitech;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;

import java.util.Optional;

public abstract class LiquidXPFluid extends WaterLikeFluid {
    @Override
    public Fluid getStill() {
        return InfinitechMod.STILL_LIQUID_XP;
    }

    @Override
    public Fluid getFlowing() {
        return InfinitechMod.FLOWING_LIQUID_XP;
    }

    @Override
    public Item getBucketItem() {
        return InfinitechMod.LIQUID_XP_BUCKET;
    }

    @Override
    protected BlockState toBlockState(FluidState fluidState) {
        return InfinitechMod.LIQUID_XP.getDefaultState().with(Properties.LEVEL_15, getBlockStateLevel(fluidState));
    }

    @Override
    public Optional<SoundEvent> getBucketFillSound() {
        return Optional.of(SoundEvents.ITEM_BUCKET_FILL);
    }


    public static class Flowing extends LiquidXPFluid {
        @Override
        protected void appendProperties(StateManager.Builder<Fluid, FluidState> builder) {
            super.appendProperties(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getLevel(FluidState fluidState) {
            return fluidState.get(LEVEL);
        }

        @Override
        public boolean isStill(FluidState fluidState) {
            return false;
        }
    }

    public static class Still extends LiquidXPFluid {
        @Override
        public int getLevel(FluidState fluidState) {
            return 8;
        }

        @Override
        public boolean isStill(FluidState fluidState) {
            return true;
        }
    }
}
