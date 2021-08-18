package gay.nyako.infinitech.block.power_bank;

import dev.technici4n.fasttransferlib.api.Simulation;
import dev.technici4n.fasttransferlib.api.energy.EnergyIo;
import dev.technici4n.fasttransferlib.api.energy.EnergyPreconditions;
import dev.technici4n.fasttransferlib.api.energy.base.SimpleEnergyIo;
import gay.nyako.infinitech.InfinitechMod;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class PowerBankBlockEntity extends BlockEntity implements EnergyIo {
    private final double maxPower = 2_000_000; // 2 mil sounds good
    private final double transferRate = 10_000; // 10k per tick...
    private double power = 0;

    public PowerBankBlockEntity(BlockPos pos, BlockState state) {
        super(InfinitechMod.POWER_BANK_BLOCK_ENTITY, pos, state);
    }

    @Override
    public double getEnergy() {
        return power;
    }

    @Override
    public double getEnergyCapacity() {
        return maxPower;
    }

    @Override
    public boolean supportsInsertion() {
        return true;
    }

    @Override
    public boolean supportsExtraction() {
        return true;
    }

    @Override
    public double insert(double maxAmount, Simulation simulation) {
        EnergyPreconditions.notNegative(maxAmount);
        double amountInserted = Math.min(maxAmount, maxPower - power);

        if (amountInserted > 1e-9) {
            if (simulation.isActing()) {
                power += amountInserted;
                markDirty();
            }

            return maxAmount - amountInserted;
        }

        return maxAmount;
    }

    @Override
    public double extract(double maxAmount, Simulation simulation) {
        EnergyPreconditions.notNegative(maxAmount);
        double amountExtracted = Math.min(maxAmount, power);

        if (amountExtracted > 1e-9) {
            if (simulation.isActing()) {
                power -= amountExtracted;
                markDirty();
            }

            return amountExtracted;
        }

        return 0;
    }
}
