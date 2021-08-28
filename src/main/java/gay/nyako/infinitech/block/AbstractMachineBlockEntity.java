package gay.nyako.infinitech.block;

import dev.technici4n.fasttransferlib.api.Simulation;
import dev.technici4n.fasttransferlib.api.energy.EnergyIo;
import dev.technici4n.fasttransferlib.api.energy.EnergyPreconditions;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.HashMap;

public abstract class AbstractMachineBlockEntity extends BlockEntity implements BlockEntityClientSerializable, EnergyIo {
    public double energy = 0;
    public double capacity;
    public double transferRate = 1_000_000_000;
    public boolean canInsert = true;
    public boolean canExtract = false;
    public HashMap<MachineUtil.Sides, MachineUtil.SideTypes> sides = new HashMap<>(); // make enums in a bit

    protected AbstractMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, double capacity, double transferRate) {
        super(type, pos, state);
        this.capacity = capacity;
        this.transferRate = transferRate;
        sides.put(MachineUtil.Sides.FRONT,  MachineUtil.SideTypes.UNSET);
        sides.put(MachineUtil.Sides.BACK,   MachineUtil.SideTypes.UNSET);
        sides.put(MachineUtil.Sides.LEFT,   MachineUtil.SideTypes.UNSET);
        sides.put(MachineUtil.Sides.RIGHT,  MachineUtil.SideTypes.UNSET);
        sides.put(MachineUtil.Sides.TOP,    MachineUtil.SideTypes.UNSET);
        sides.put(MachineUtil.Sides.BOTTOM, MachineUtil.SideTypes.UNSET);
    }

    protected AbstractMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, double capacity) {
        super(type, pos, state);
        this.capacity = capacity;
    }

    @Override
    public double getEnergy() {
        return energy;
    }

    @Override
    public double getEnergyCapacity() {
        return capacity;
    }

    @Override
    public boolean supportsInsertion() {
        return canInsert;
    }

    @Override
    public double insert(double maxAmount, Simulation simulation) {
        EnergyPreconditions.notNegative(maxAmount);
        double amountInserted = Math.min(maxAmount, capacity - energy);

        if (amountInserted > 1e-9) {
            if (simulation.isActing()) {
                energy += amountInserted;
                markDirty();
            }

            return maxAmount - amountInserted;
        }

        return maxAmount;
    }

    @Override
    public boolean supportsExtraction() {
        return canExtract;
    }

    @Override
    public double extract(double maxAmount, Simulation simulation) {
        EnergyPreconditions.notNegative(maxAmount);
        double amountExtracted = Math.min(maxAmount, energy);

        if (amountExtracted > 1e-9) {
            if (simulation.isActing()) {
                energy -= amountExtracted;
                markDirty();
            }

            return amountExtracted;
        }

        return 0;
    }

    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.energy = nbt.getDouble("Energy");

        NbtCompound directionCompound = nbt.getCompound("SideConfiguration");
        sides.put(MachineUtil.Sides.FRONT,  MachineUtil.SideTypes.values()[directionCompound.getInt("FRONT" )]);
        sides.put(MachineUtil.Sides.BACK,   MachineUtil.SideTypes.values()[directionCompound.getInt("BACK"  )]);
        sides.put(MachineUtil.Sides.LEFT,   MachineUtil.SideTypes.values()[directionCompound.getInt("LEFT"  )]);
        sides.put(MachineUtil.Sides.RIGHT,  MachineUtil.SideTypes.values()[directionCompound.getInt("RIGHT" )]);
        sides.put(MachineUtil.Sides.TOP,    MachineUtil.SideTypes.values()[directionCompound.getInt("TOP"   )]);
        sides.put(MachineUtil.Sides.BOTTOM, MachineUtil.SideTypes.values()[directionCompound.getInt("BOTTOM")]);
    }

    public NbtCompound writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putDouble("Energy", this.energy);

        NbtCompound directionCompound = new NbtCompound();
        directionCompound.putInt("FRONT",  sides.get(MachineUtil.Sides.FRONT ).ordinal());
        directionCompound.putInt("BACK",   sides.get(MachineUtil.Sides.BACK  ).ordinal());
        directionCompound.putInt("LEFT",   sides.get(MachineUtil.Sides.LEFT  ).ordinal());
        directionCompound.putInt("RIGHT",  sides.get(MachineUtil.Sides.RIGHT ).ordinal());
        directionCompound.putInt("TOP",    sides.get(MachineUtil.Sides.TOP   ).ordinal());
        directionCompound.putInt("BOTTOM", sides.get(MachineUtil.Sides.BOTTOM).ordinal());

        nbt.put("SideConfiguration", directionCompound);

        return nbt;
    }

    @Override
    public void fromClientTag(NbtCompound tag) {
        NbtCompound directionCompound = tag.getCompound("SideConfiguration");
        sides.put(MachineUtil.Sides.FRONT,  MachineUtil.SideTypes.values()[directionCompound.getInt("FRONT" )]);
        sides.put(MachineUtil.Sides.BACK,   MachineUtil.SideTypes.values()[directionCompound.getInt("BACK"  )]);
        sides.put(MachineUtil.Sides.LEFT,   MachineUtil.SideTypes.values()[directionCompound.getInt("LEFT"  )]);
        sides.put(MachineUtil.Sides.RIGHT,  MachineUtil.SideTypes.values()[directionCompound.getInt("RIGHT" )]);
        sides.put(MachineUtil.Sides.TOP,    MachineUtil.SideTypes.values()[directionCompound.getInt("TOP"   )]);
        sides.put(MachineUtil.Sides.BOTTOM, MachineUtil.SideTypes.values()[directionCompound.getInt("BOTTOM")]);
    }

    @Override
    public NbtCompound toClientTag(NbtCompound tag) {
        NbtCompound directionCompound = new NbtCompound();
        directionCompound.putInt("FRONT",  sides.get(MachineUtil.Sides.FRONT ).ordinal());
        directionCompound.putInt("BACK",   sides.get(MachineUtil.Sides.BACK  ).ordinal());
        directionCompound.putInt("LEFT",   sides.get(MachineUtil.Sides.LEFT  ).ordinal());
        directionCompound.putInt("RIGHT",  sides.get(MachineUtil.Sides.RIGHT ).ordinal());
        directionCompound.putInt("TOP",    sides.get(MachineUtil.Sides.TOP   ).ordinal());
        directionCompound.putInt("BOTTOM", sides.get(MachineUtil.Sides.BOTTOM).ordinal());

        tag.put("SideConfiguration", directionCompound);
        return tag;
    }

    /*
        Can something insert to this direction?
     */
    public boolean canTransfer(Direction direction) {
        MachineUtil.Sides side = MachineUtil.DirectionToSide(getCachedState().get(Properties.FACING), direction);
        return (sides.get(side) != MachineUtil.SideTypes.OFF);
    }

    // Attempt transfers based on side
    public void attemptSideTransfers(InventoryStorage storage) {
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
