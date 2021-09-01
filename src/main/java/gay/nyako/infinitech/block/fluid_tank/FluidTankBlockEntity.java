package gay.nyako.infinitech.block.fluid_tank;

import com.google.common.collect.Lists;
import gay.nyako.infinitech.InfinitechMod;
import gay.nyako.infinitech.storage.FluidInventory;
import gay.nyako.infinitech.storage.FluidSlot;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FluidTankBlockEntity extends BlockEntity implements FluidInventory {
    private final List<FluidSlot> fluidSlots;

    public FluidTankBlockEntity(BlockPos pos, BlockState state) {
        super(InfinitechMod.FLUID_TANK_BLOCK_ENTITY, pos, state);
        var capacity = ((FluidTankBlock)state.getBlock()).capacity;
        fluidSlots = Lists.newArrayList(FluidSlot.blank(capacity));
    }

    @Override
    public List<FluidSlot> getFluidSlots() {
        return fluidSlots;
    }

    @Override
    public int[] getAvailableFluidSlots(Direction side) {
        return new int[]{ 0 };
    }

    @Override
    public boolean canInsert(int slot, FluidVariant fluid, @Nullable Direction dir) {
        return true;
    }

    @Override
    public boolean canExtract(int slot, FluidVariant fluid, Direction dir) {
        return true;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        FluidInventory.readNbt(nbt, fluidSlots);
        super.readNbt(nbt);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        FluidInventory.writeNbt(nbt, fluidSlots);
        return super.writeNbt(nbt);
    }
}
