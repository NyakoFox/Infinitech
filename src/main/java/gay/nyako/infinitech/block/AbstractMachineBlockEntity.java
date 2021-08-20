package gay.nyako.infinitech.block;

import dev.technici4n.fasttransferlib.api.Simulation;
import dev.technici4n.fasttransferlib.api.energy.EnergyIo;
import dev.technici4n.fasttransferlib.api.energy.EnergyPreconditions;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.HashMap;

public abstract class AbstractMachineBlockEntity extends BlockEntity implements EnergyIo {
    public double energy = 0;
    public double capacity;
    public double transferRate = 1_000_000_000;
    public boolean canInsert = true;
    public boolean canExtract = false;
    public HashMap<Direction, SideTypes> sides = new HashMap<>(); // make enums in a bit

    public enum SideTypes {
        UNSET,
        INPUT,
        OUTPUT,
        BOTH,
        OFF
    }

    protected AbstractMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, double capacity, double transferRate) {
        super(type, pos, state);
        this.capacity = capacity;
        this.transferRate = transferRate;
        sides.put(Direction.NORTH, SideTypes.UNSET);
        sides.put(Direction.SOUTH, SideTypes.UNSET);
        sides.put(Direction.EAST,  SideTypes.UNSET);
        sides.put(Direction.WEST,  SideTypes.UNSET);
        sides.put(Direction.UP,    SideTypes.UNSET);
        sides.put(Direction.DOWN,  SideTypes.UNSET);
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
    }

    public NbtCompound writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putDouble("Energy", this.energy);
        return nbt;
    }
}
