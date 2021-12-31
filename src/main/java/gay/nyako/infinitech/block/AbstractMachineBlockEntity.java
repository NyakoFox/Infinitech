package gay.nyako.infinitech.block;

import gay.nyako.infinitech.storage.energy.MachineEnergyStorage;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public abstract class AbstractMachineBlockEntity extends BlockEntity {
    public long energy = 0;
    public long capacity;
    public long transferRate = 1_000_000_000;
    public boolean canInsert = true;
    public boolean canExtract = false;
    public MachineEnergyStorage energyStorage;
    public HashMap<MachineUtil.Sides, MachineUtil.SideTypes> sides = new HashMap<>();

    protected AbstractMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, long capacity, long transferRate) {
        super(type, pos, state);
        this.capacity = capacity;
        this.transferRate = transferRate;
        this.energyStorage = new MachineEnergyStorage(this);
        sides.put(MachineUtil.Sides.FRONT,  MachineUtil.SideTypes.UNSET);
        sides.put(MachineUtil.Sides.BACK,   MachineUtil.SideTypes.UNSET);
        sides.put(MachineUtil.Sides.LEFT,   MachineUtil.SideTypes.UNSET);
        sides.put(MachineUtil.Sides.RIGHT,  MachineUtil.SideTypes.UNSET);
        sides.put(MachineUtil.Sides.TOP,    MachineUtil.SideTypes.UNSET);
        sides.put(MachineUtil.Sides.BOTTOM, MachineUtil.SideTypes.UNSET);
    }

    protected AbstractMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, long capacity) {
        this(type, pos, state, capacity, 1_000_000_000);
    }

    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.energy = nbt.getLong("Energy");

        NbtCompound directionCompound = nbt.getCompound("SideConfiguration");
        sides.put(MachineUtil.Sides.FRONT,  MachineUtil.SideTypes.values()[directionCompound.getInt("FRONT" )]);
        sides.put(MachineUtil.Sides.BACK,   MachineUtil.SideTypes.values()[directionCompound.getInt("BACK"  )]);
        sides.put(MachineUtil.Sides.LEFT,   MachineUtil.SideTypes.values()[directionCompound.getInt("LEFT"  )]);
        sides.put(MachineUtil.Sides.RIGHT,  MachineUtil.SideTypes.values()[directionCompound.getInt("RIGHT" )]);
        sides.put(MachineUtil.Sides.TOP,    MachineUtil.SideTypes.values()[directionCompound.getInt("TOP"   )]);
        sides.put(MachineUtil.Sides.BOTTOM, MachineUtil.SideTypes.values()[directionCompound.getInt("BOTTOM")]);
    }

    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putLong("Energy", this.energy);

        NbtCompound directionCompound = new NbtCompound();
        directionCompound.putInt("FRONT",  sides.get(MachineUtil.Sides.FRONT ).ordinal());
        directionCompound.putInt("BACK",   sides.get(MachineUtil.Sides.BACK  ).ordinal());
        directionCompound.putInt("LEFT",   sides.get(MachineUtil.Sides.LEFT  ).ordinal());
        directionCompound.putInt("RIGHT",  sides.get(MachineUtil.Sides.RIGHT ).ordinal());
        directionCompound.putInt("TOP",    sides.get(MachineUtil.Sides.TOP   ).ordinal());
        directionCompound.putInt("BOTTOM", sides.get(MachineUtil.Sides.BOTTOM).ordinal());

        nbt.put("SideConfiguration", directionCompound);
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
    public void markDirty() {
        super.markDirty();
        try {
            sync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sync() {
        if (world.isClient()) {
            System.out.println("don't run sync() on the client!!!!!!! what are u doing!!!!!");
            return;
        }
        ((ServerWorld) world).getChunkManager().markForUpdate(getPos());
    }

    /*
        Can something insert to this direction?
     */
    public boolean canTransfer(Direction direction) {
        MachineUtil.Sides side = MachineUtil.DirectionToSide(getCachedState().get(Properties.FACING), direction);
        return (sides.get(side) != MachineUtil.SideTypes.OFF);
    }

    // Is a side disabled?
    public boolean isSideDisabled(Direction direction) {
        Direction baseDir = getCachedState().get(Properties.HORIZONTAL_FACING);
        MachineUtil.Sides side = MachineUtil.DirectionToSide(baseDir, direction);
        MachineUtil.SideTypes sideType = sides.get(side);
        return (sideType == MachineUtil.SideTypes.OFF);
    }

    // Attempt transfers based on side
    public void attemptSideTransfers(InventoryStorage storage) {
        if (world.isClient()) return;
        Direction baseDir = getCachedState().get(Properties.HORIZONTAL_FACING);
        for (MachineUtil.Sides side : MachineUtil.Sides.values()) {
            Direction direction = MachineUtil.SideToRelativeDirection(side, baseDir);
            MachineUtil.SideTypes sideType = sides.get(side);

            if (sideType == MachineUtil.SideTypes.INPUT || sideType == MachineUtil.SideTypes.BOTH) {
                // Attempt to input!
                Storage<ItemVariant> front = ItemStorage.SIDED.find(world, pos.offset(direction), direction.getOpposite());
                long output = StorageUtil.move(front, storage, item -> true, 1, null);
            }
            if (sideType == MachineUtil.SideTypes.OUTPUT || sideType == MachineUtil.SideTypes.BOTH) {
                // Attempt to output
                Storage<ItemVariant> back = ItemStorage.SIDED.find(world, pos.offset(direction), direction.getOpposite());
                StorageUtil.move(storage, back, item -> true, 1, null);
            }
        }
    }
}
