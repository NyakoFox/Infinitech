package gay.nyako.infinitech.block.fluid_tank;

import com.google.common.collect.Lists;
import gay.nyako.infinitech.InfinitechMod;
import gay.nyako.infinitech.storage.fluid.FluidInventory;
import gay.nyako.infinitech.storage.fluid.FluidSlot;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
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

    public float getFillPercent() {
        FluidSlot slot = getFluidSlot(0);
        return (float)slot.amount / slot.capacity;
    }

    public void updateLuminance() {
        FluidVariant variant = getStoredVariant();
        var luminance = variant.isBlank() ? 0 : variant.getFluid().getDefaultState().getBlockState().getLuminance();

        BlockState state = world.getBlockState(pos);
        if (state.get(FluidTankBlock.LUMINANCE).intValue() != luminance) {
            world.setBlockState(pos, state.with(FluidTankBlock.LUMINANCE, luminance));
        }
    }

    public FluidVariant getStoredVariant() {
        return getFluidSlot(0).fluid;
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
    public void markDirty() {
        super.markDirty();
        updateLuminance();
        ((ServerWorld) world).getChunkManager().markForUpdate(getPos());
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound nbt = new NbtCompound();
        writeNbt(nbt);
        return nbt;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        FluidInventory.readNbt(nbt, fluidSlots);
        super.readNbt(nbt);
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        FluidInventory.writeNbt(nbt, fluidSlots);
        super.writeNbt(nbt);
    }
}
